/*
 * Copyright (c) 2016, 2017, 2018, 2019 FabricMC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.fabricmc.fabric.impl.resource.loader;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import com.google.common.base.Charsets;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;
import org.apache.commons.io.IOUtils;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.minecraft.SharedConstants;
import net.minecraft.resource.DataConfiguration;
import net.minecraft.resource.DataPackSettings;
import net.minecraft.resource.PackVersion;
import net.minecraft.resource.ResourcePack;
import net.minecraft.resource.ResourcePackManager;
import net.minecraft.resource.ResourcePackProfile;
import net.minecraft.resource.ResourceType;
import net.minecraft.resource.VanillaDataPackProvider;
import net.minecraft.resource.featuretoggle.FeatureFlags;
import net.minecraft.resource.metadata.PackResourceMetadata;
import net.minecraft.text.Text;
import net.minecraft.util.dynamic.Range;
import net.minecraft.util.path.SymlinkFinder;

import net.fabricmc.fabric.api.resource.ModResourcePack;
import net.fabricmc.fabric.api.resource.ResourcePackActivationType;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.metadata.CustomValue;
import net.fabricmc.loader.api.metadata.ModMetadata;

/**
 * Internal utilities for managing resource packs.
 */
public final class ModResourcePackUtil {
	public static final Gson GSON = new Gson();
	private static final Logger LOGGER = LoggerFactory.getLogger(ModResourcePackUtil.class);
	private static final String LOAD_ORDER_KEY = "fabric:resource_load_order";

	private ModResourcePackUtil() {
	}

	/**
	 * Returns a list of mod resource packs.
	 *
	 * @param type    the type of resource
	 * @param subPath the resource pack sub path directory in mods, may be {@code null}
	 */
	public static List<ModResourcePack> getModResourcePacks(FabricLoader fabricLoader, ResourceType type, @Nullable String subPath) {
		ModResourcePackSorter sorter = new ModResourcePackSorter();

		Collection<ModContainer> containers = fabricLoader.getAllMods();
		List<String> allIds = containers.stream().map(ModContainer::getMetadata).map(ModMetadata::getId).toList();

		for (ModContainer container : containers) {
			ModMetadata metadata = container.getMetadata();
			String id = metadata.getId();

			if (metadata.getType().equals("builtin")) {
				continue;
			}

			ModResourcePack pack = ModNioResourcePack.create(id, container, subPath, type, ResourcePackActivationType.ALWAYS_ENABLED, true);

			if (pack == null) {
				continue;
			}

			sorter.addPack(pack);

			CustomValue loadOrder = metadata.getCustomValue(LOAD_ORDER_KEY);
			if (loadOrder == null) continue;

			if (loadOrder.getType() == CustomValue.CvType.OBJECT) {
				CustomValue.CvObject object = loadOrder.getAsObject();

				addLoadOrdering(object, allIds, sorter, Order.BEFORE, id);
				addLoadOrdering(object, allIds, sorter, Order.AFTER, id);
			} else {
				LOGGER.error("[Fabric] Resource load order should be an object");
			}
		}

		return sorter.getPacks();
	}

	public static void addLoadOrdering(CustomValue.CvObject object, List<String> allIds, ModResourcePackSorter sorter, Order order, String currentId) {
		List<String> modIds = new ArrayList<>();

		CustomValue array = object.get(order.jsonKey);
		if (array == null) return;

		switch (array.getType()) {
		case STRING -> modIds.add(array.getAsString());
		case ARRAY -> {
			for (CustomValue id : array.getAsArray()) {
				if (id.getType() == CustomValue.CvType.STRING) {
					modIds.add(id.getAsString());
				}
			}
		}
		default -> {
			LOGGER.error("[Fabric] {} should be a string or an array", order.jsonKey);
			return;
		}
		}

		modIds.stream().filter(allIds::contains).forEach(modId -> sorter.addLoadOrdering(modId, currentId, order));
	}

	public static void refreshAutoEnabledPacks(List<ResourcePackProfile> enabledProfiles, Map<String, ResourcePackProfile> allProfiles) {
		LOGGER.debug("[Fabric] Starting internal pack sorting with: {}", enabledProfiles.stream().map(ResourcePackProfile::getId).toList());
		enabledProfiles.removeIf(profile -> ((FabricResourcePackProfile) profile).fabric_isHidden());
		LOGGER.debug("[Fabric] Removed all internal packs, result: {}", enabledProfiles.stream().map(ResourcePackProfile::getId).toList());
		ListIterator<ResourcePackProfile> it = enabledProfiles.listIterator();
		Set<String> seen = new LinkedHashSet<>();

		while (it.hasNext()) {
			ResourcePackProfile profile = it.next();
			seen.add(profile.getId());

			for (ResourcePackProfile p : allProfiles.values()) {
				FabricResourcePackProfile fp = (FabricResourcePackProfile) p;

				if (fp.fabric_isHidden() && fp.fabric_parentsEnabled(seen) && seen.add(p.getId())) {
					it.add(p);
					LOGGER.debug("[Fabric] cur @ {}, auto-enabled {}, currently enabled: {}", profile.getId(), p.getId(), seen);
				}
			}
		}

		LOGGER.debug("[Fabric] Final sorting result: {}", enabledProfiles.stream().map(ResourcePackProfile::getId).toList());
	}

	public static boolean containsDefault(String filename, boolean modBundled) {
		return "pack.mcmeta".equals(filename) || (modBundled && "pack.png".equals(filename));
	}

	public static InputStream getDefaultIcon() throws IOException {
		Optional<Path> loaderIconPath = FabricLoader.getInstance().getModContainer("fabric-resource-loader-v0")
				.flatMap(resourceLoaderContainer -> resourceLoaderContainer.getMetadata().getIconPath(512).flatMap(resourceLoaderContainer::findPath));

		if (loaderIconPath.isPresent()) {
			return Files.newInputStream(loaderIconPath.get());
		}

		// Should never happen in practice
		return null;
	}

	public static InputStream openDefault(ModContainer container, ResourceType type, String filename) throws IOException {
		switch (filename) {
		case "pack.mcmeta":
			String description = Objects.requireNonNullElse(container.getMetadata().getId(), "");
			String metadata = serializeMetadata(SharedConstants.getGameVersion().packVersion(type), description, type);
			return IOUtils.toInputStream(metadata, Charsets.UTF_8);
		case "pack.png":
			Optional<Path> path = container.getMetadata().getIconPath(512).flatMap(container::findPath);

			if (path.isPresent()) {
				return Files.newInputStream(path.get());
			} else {
				return getDefaultIcon();
			}
		default:
			return null;
		}
	}

	public static PackResourceMetadata getMetadataPack(PackVersion packVersion, Text description) {
		return new PackResourceMetadata(description, new Range<>(packVersion));
	}

	public static JsonObject getMetadataPackJson(PackVersion packVersion, Text description, ResourceType resourceType) {
		return PackResourceMetadata.createCodec(resourceType)
				.encodeStart(JsonOps.INSTANCE, getMetadataPack(packVersion, description))
				.getOrThrow()
				.getAsJsonObject();
	}

	public static String serializeMetadata(PackVersion packVersion, String description, ResourceType resourceType) {
		// This seems to be still manually deserialized
		JsonObject pack = getMetadataPackJson(packVersion, Text.literal(description), resourceType);
		JsonObject metadata = new JsonObject();
		metadata.add("pack", pack);
		return GSON.toJson(metadata);
	}

	public static Text getName(ModMetadata info) {
		if (info.getId() != null) {
			return Text.literal(info.getId());
		} else {
			return Text.translatable("pack.name.fabricMod", info.getId());
		}
	}

	/**
	 * Creates the default data pack settings that replaces
	 * {@code DataPackSettings.SAFE_MODE} used in vanilla.
	 * @return the default data pack settings
	 */
	public static DataConfiguration createDefaultDataConfiguration() {
		ModResourcePackCreator modResourcePackCreator = new ModResourcePackCreator(ResourceType.SERVER_DATA);
		List<ResourcePackProfile> moddedResourcePacks = new ArrayList<>();
		modResourcePackCreator.register(moddedResourcePacks::add);

		List<String> enabled = new ArrayList<>(DataPackSettings.SAFE_MODE.getEnabled());
		List<String> disabled = new ArrayList<>(DataPackSettings.SAFE_MODE.getDisabled());

		// This ensures that any built-in registered data packs by mods which needs to be enabled by default are
		// as the data pack screen automatically put any data pack as disabled except the Default data pack.
		for (ResourcePackProfile profile : moddedResourcePacks) {
			if (profile.getSource() == ModResourcePackCreator.RESOURCE_PACK_SOURCE) {
				enabled.add(profile.getId());
				continue;
			}

			try (ResourcePack pack = profile.createResourcePack()) {
				if (pack instanceof ModNioResourcePack && ((ModNioResourcePack) pack).getActivationType().isEnabledByDefault()) {
					enabled.add(profile.getId());
				} else {
					disabled.add(profile.getId());
				}
			}
		}

		return new DataConfiguration(
				new DataPackSettings(enabled, disabled),
				FeatureFlags.DEFAULT_ENABLED_FEATURES
		);
	}

	/**
	 * Vanilla enables all available datapacks automatically in TestServer#create, but it does so in alphabetical order,
	 * which means the Vanilla pack has higher precedence than modded, breaking our tests.
	 * To fix this, we move all modded pack profiles to the end of the list.
	 */
	public static DataPackSettings createTestServerSettings(List<String> enabled, List<String> disabled) {
		// Collect modded profiles
		Set<String> moddedProfiles = new HashSet<>();
		ModResourcePackCreator modResourcePackCreator = new ModResourcePackCreator(ResourceType.SERVER_DATA);
		modResourcePackCreator.register(profile -> moddedProfiles.add(profile.getId()));

		// Remove them from the enabled list
		List<String> moveToTheEnd = new ArrayList<>();

		for (Iterator<String> it = enabled.iterator(); it.hasNext();) {
			String profile = it.next();

			if (moddedProfiles.contains(profile)) {
				moveToTheEnd.add(profile);
				it.remove();
			}
		}

		// Add back at the end
		enabled.addAll(moveToTheEnd);

		return new DataPackSettings(enabled, disabled);
	}

	/**
	 * Creates the ResourcePackManager used by the ClientDataPackManager and replaces
	 * {@code VanillaDataPackProvider.createClientManager} used by vanilla.
	 */
	public static ResourcePackManager createClientManager() {
		return new ResourcePackManager(new VanillaDataPackProvider(new SymlinkFinder((path) -> true)), new ModResourcePackCreator(ResourceType.SERVER_DATA, true));
	}

	public enum Order {
		BEFORE("before"),
		AFTER("after");

		private final String jsonKey;

		Order(String jsonKey) {
			this.jsonKey = jsonKey;
		}
	}
}
