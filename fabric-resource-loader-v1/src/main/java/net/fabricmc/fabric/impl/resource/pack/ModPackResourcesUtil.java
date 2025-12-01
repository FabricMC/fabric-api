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

package net.fabricmc.fabric.impl.resource.pack;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
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

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;
import org.apache.commons.io.IOUtils;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.minecraft.SharedConstants;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.metadata.pack.PackFormat;
import net.minecraft.server.packs.metadata.pack.PackMetadataSection;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.server.packs.repository.ServerPacksSource;
import net.minecraft.util.InclusiveRange;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.DataPackConfig;
import net.minecraft.world.level.WorldDataConfiguration;
import net.minecraft.world.level.validation.DirectoryValidator;

import net.fabricmc.fabric.api.resource.v1.pack.ModPackResources;
import net.fabricmc.fabric.api.resource.v1.pack.PackActivationType;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.metadata.CustomValue;
import net.fabricmc.loader.api.metadata.ModMetadata;

/**
 * Internal utilities for managing resource packs.
 */
public final class ModPackResourcesUtil {
	public static final Gson GSON = new Gson();
	private static final Logger LOGGER = LoggerFactory.getLogger(ModPackResourcesUtil.class);
	private static final String LOAD_ORDER_KEY = "fabric:resource_load_order";

	private ModPackResourcesUtil() {
	}

	/**
	 * Returns a list of mod resource packs.
	 *
	 * @param type    the type of resource
	 * @param subPath the resource pack sub path directory in mods, may be {@code null}
	 */
	public static List<ModPackResources> getModResourcePacks(FabricLoader fabricLoader, PackType type, @Nullable String subPath) {
		var sorter = new ModPackResourcesSorter();

		Collection<ModContainer> containers = fabricLoader.getAllMods();
		List<String> allIds = containers.stream().map(ModContainer::getMetadata).map(ModMetadata::getId).toList();

		for (ModContainer container : containers) {
			ModMetadata metadata = container.getMetadata();
			String id = metadata.getId();

			if (metadata.getType().equals("builtin")) {
				continue;
			}

			ModPackResources pack = ModNioPackResources.create(id, container, subPath, type, PackActivationType.ALWAYS_ENABLED, true);

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

	public static void addLoadOrdering(CustomValue.CvObject object, List<String> allIds, ModPackResourcesSorter sorter, Order order, String currentId) {
		var modIds = new ArrayList<String>();

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

	public static void refreshAutoEnabledPacks(List<Pack> enabledProfiles, Map<String, Pack> allProfiles) {
		LOGGER.debug("[Fabric] Starting internal pack sorting with: {}", enabledProfiles.stream().map(Pack::getId).toList());
		enabledProfiles.removeIf(profile -> ((FabricPack) profile).fabric$isHidden());
		LOGGER.debug("[Fabric] Removed all internal packs, result: {}", enabledProfiles.stream().map(Pack::getId).toList());
		ListIterator<Pack> it = enabledProfiles.listIterator();
		Set<String> seen = new LinkedHashSet<>();

		while (it.hasNext()) {
			Pack profile = it.next();
			seen.add(profile.getId());

			for (Pack p : allProfiles.values()) {
				FabricPack fp = (FabricPack) p;

				if (fp.fabric$isHidden() && fp.fabric$parentsEnabled(seen) && seen.add(p.getId())) {
					it.add(p);
					LOGGER.debug("[Fabric] cur @ {}, auto-enabled {}, currently enabled: {}", profile.getId(), p.getId(), seen);
				}
			}
		}

		LOGGER.debug("[Fabric] Final sorting result: {}", enabledProfiles.stream().map(Pack::getId).toList());
	}

	public static boolean containsDefault(String filename, boolean modBundled) {
		return "pack.mcmeta".equals(filename) || (modBundled && "pack.png".equals(filename));
	}

	public static InputStream getDefaultIcon() throws IOException {
		Optional<Path> loaderIconPath = FabricLoader.getInstance().getModContainer("fabric-resource-loader-v1")
				.flatMap(resourceLoaderContainer -> resourceLoaderContainer.getMetadata().getIconPath(512).flatMap(resourceLoaderContainer::findPath));

		if (loaderIconPath.isPresent()) {
			return Files.newInputStream(loaderIconPath.get());
		}

		// Should never happen in practice
		return null;
	}

	public static InputStream openDefault(ModContainer container, PackType type, String filename) throws IOException {
		switch (filename) {
		case "pack.mcmeta":
			String description = Objects.requireNonNullElse(container.getMetadata().getId(), "");
			String metadata = serializeMetadata(SharedConstants.getCurrentVersion().packVersion(type), description, type);
			return IOUtils.toInputStream(metadata, StandardCharsets.UTF_8);
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

	public static PackMetadataSection getMetadataPack(PackFormat packVersion, Component description) {
		return new PackMetadataSection(description, new InclusiveRange<>(packVersion));
	}

	public static JsonObject getMetadataPackJson(PackFormat packVersion, Component description, PackType type) {
		return PackMetadataSection.codecForPackType(type)
				.encodeStart(JsonOps.INSTANCE, getMetadataPack(packVersion, description))
				.getOrThrow()
				.getAsJsonObject();
	}

	public static String serializeMetadata(PackFormat packVersion, String description, PackType type) {
		// This seems to be still manually deserialized
		JsonObject pack = getMetadataPackJson(packVersion, Component.literal(description), type);
		var metadata = new JsonObject();
		metadata.add("pack", pack);
		return GSON.toJson(metadata);
	}

	public static Component getName(ModMetadata info) {
		if (info.getId() != null) {
			return Component.literal(info.getId());
		} else {
			return Component.translatable("pack.name.fabricMod", info.getId());
		}
	}

	/**
	 * Creates the default data pack settings that replaces
	 * {@code DataPackSettings.SAFE_MODE} used in vanilla.
	 * @return the default data pack settings
	 */
	public static WorldDataConfiguration createDefaultDataConfiguration() {
		var modResourcePackCreator = new ModResourcePackCreator(PackType.SERVER_DATA);
		var moddedResourcePacks = new ArrayList<Pack>();
		modResourcePackCreator.loadPacks(moddedResourcePacks::add);

		var enabled = new ArrayList<String>(DataPackConfig.DEFAULT.getEnabled());
		var disabled = new ArrayList<String>(DataPackConfig.DEFAULT.getDisabled());

		// This ensures that any built-in registered data packs by mods which needs to be enabled by default are
		// as the data pack screen automatically put any data pack as disabled except the Default data pack.
		for (Pack profile : moddedResourcePacks) {
			if (profile.getPackSource() == ModResourcePackCreator.RESOURCE_PACK_SOURCE) {
				enabled.add(profile.getId());
				continue;
			}

			try (PackResources pack = profile.open()) {
				if (pack instanceof ModNioPackResources nioPack && nioPack.getActivationType().isEnabledByDefault()) {
					enabled.add(profile.getId());
				} else {
					disabled.add(profile.getId());
				}
			}
		}

		return new WorldDataConfiguration(
				new DataPackConfig(enabled, disabled),
				FeatureFlags.DEFAULT_FLAGS
		);
	}

	/**
	 * Vanilla enables all available datapacks automatically in TestServer#create, but it does so in alphabetical order,
	 * which means the Vanilla pack has higher precedence than modded, breaking our tests.
	 * To fix this, we move all modded pack profiles to the end of the list.
	 */
	public static DataPackConfig createTestServerSettings(List<String> enabled, List<String> disabled) {
		// Collect modded profiles
		var moddedProfiles = new HashSet<String>();
		var modResourcePackCreator = new ModResourcePackCreator(PackType.SERVER_DATA);
		modResourcePackCreator.loadPacks(profile -> moddedProfiles.add(profile.getId()));

		// Remove them from the enabled list
		var moveToTheEnd = new ArrayList<String>();

		for (Iterator<String> it = enabled.iterator(); it.hasNext(); ) {
			String profile = it.next();

			if (moddedProfiles.contains(profile)) {
				moveToTheEnd.add(profile);
				it.remove();
			}
		}

		// Add back at the end
		enabled.addAll(moveToTheEnd);

		return new DataPackConfig(enabled, disabled);
	}

	/**
	 * Creates the ResourcePackManager used by the ClientDataPackManager and replaces
	 * {@code VanillaDataPackProvider.createClientManager} used by vanilla.
	 */
	public static PackRepository createClientManager() {
		return new PackRepository(
				new ServerPacksSource(new DirectoryValidator((path) -> true)),
				new ModResourcePackCreator(PackType.SERVER_DATA, true)
		);
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
