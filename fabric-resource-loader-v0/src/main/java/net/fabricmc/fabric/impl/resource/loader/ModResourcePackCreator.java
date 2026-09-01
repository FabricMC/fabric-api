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

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;

import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.VisibleForTesting;

import net.minecraft.resource.ResourcePackInfo;
import net.minecraft.resource.ResourcePackPosition;
import net.minecraft.resource.ResourcePackProfile;
import net.minecraft.resource.ResourcePackProvider;
import net.minecraft.resource.ResourcePackSource;
import net.minecraft.resource.ResourceType;
import net.minecraft.text.Text;

import net.fabricmc.fabric.api.resource.ModResourcePack;
import net.fabricmc.loader.api.FabricLoader;

/**
 * Represents a resource pack provider for mods and built-in mods resource packs.
 */
public class ModResourcePackCreator implements ResourcePackProvider {
	/**
	 * The ID of the root resource pack profile for bundled packs.
	 */
	public static final String FABRIC = "fabric";
	private static final String PROGRAMMER_ART = "programmer_art";
	private static final String HIGH_CONTRAST = "high_contrast";
	public static final Set<String> POST_CHANGE_HANDLE_REQUIRED = Set.of(FABRIC, PROGRAMMER_ART, HIGH_CONTRAST);
	@VisibleForTesting
	public static final Predicate<Set<String>> BASE_PARENT = enabled -> enabled.contains(FABRIC);
	@VisibleForTesting
	public static final Predicate<Set<String>> PROGRAMMER_ART_PARENT = enabled -> enabled.contains(FABRIC) && enabled.contains(PROGRAMMER_ART);
	@VisibleForTesting
	public static final Predicate<Set<String>> HIGH_CONTRAST_PARENT = enabled -> enabled.contains(FABRIC) && enabled.contains(HIGH_CONTRAST);
	/**
	 * This can be used to check if a pack profile is for mod-provided packs.
	 */
	public static final ResourcePackSource RESOURCE_PACK_SOURCE = new ResourcePackSource() {
		@Override
		public Text decorate(Text packName) {
			return Text.translatable("pack.nameAndSource", packName, Text.translatable("pack.source.fabricmod"));
		}

		@Override
		public boolean canBeEnabledLater() {
			return true;
		}
	};
	public static final ModResourcePackCreator CLIENT_RESOURCE_PACK_PROVIDER = new ModResourcePackCreator(ResourceType.CLIENT_RESOURCES);
	/**
	 * The maximum number of known data packs requested from the client, including vanilla data packs.
	 */
	public static final int MAX_KNOWN_PACKS = Integer.getInteger("fabric-resource-loader-v0:maxKnownPacks", 1024);

	private final ResourceType type;
	private final ResourcePackPosition activationInfo;
	private final boolean forClientDataPackManager;

	public ModResourcePackCreator(ResourceType type) {
		this(type, false);
	}

	protected ModResourcePackCreator(ResourceType type, boolean forClientDataPackManager) {
		this.type = type;
		this.activationInfo = new ResourcePackPosition(!forClientDataPackManager, ResourcePackProfile.InsertionPosition.TOP, false);
		this.forClientDataPackManager = forClientDataPackManager;
	}

	/**
	 * Registers the resource packs.
	 *
	 * @param consumer The resource pack profile consumer.
	 */
	@Override
	public void register(Consumer<ResourcePackProfile> consumer) {
		/*
			Register order rule in this provider:
			1. Mod resource packs
			2. Mod built-in resource packs

			Register order rule globally:
			1. Default and Vanilla built-in resource packs
			2. Mod resource packs
			3. Mod built-in resource packs
			4. User resource packs
		 */

		ResourcePackInfo metadata = new ResourcePackInfo(
				FABRIC,
				Text.translatable("pack.name.fabricMods"),
				RESOURCE_PACK_SOURCE,
				Optional.empty()
		);

		consumer.accept(ResourcePackProfile.create(
				metadata,
				new PlaceholderResourcePack.Factory(this.type, metadata),
				this.type,
				this.activationInfo
		));

		// Build a list of mod resource packs.
		registerModPack(consumer, null, BASE_PARENT);

		if (this.type == ResourceType.CLIENT_RESOURCES) {
			// Programmer Art/High Contrast data packs can never be enabled.
			registerModPack(consumer, PROGRAMMER_ART, PROGRAMMER_ART_PARENT);
			registerModPack(consumer, HIGH_CONTRAST, HIGH_CONTRAST_PARENT);
		}

		// Register all built-in resource packs provided by mods.
		ResourceManagerHelperImpl.registerBuiltinResourcePacks(this.type, consumer);
	}

	private void registerModPack(Consumer<ResourcePackProfile> consumer, @Nullable String subPath, Predicate<Set<String>> parents) {
		List<ModResourcePack> packs = ModResourcePackUtil.getModResourcePacks(FabricLoader.getInstance(), this.type, subPath);

		for (ModResourcePack pack : packs) {
			ResourcePackProfile profile = ResourcePackProfile.create(
					pack.getInfo(),
					new ModResourcePackFactory(pack),
					this.type,
					this.activationInfo
			);

			if (profile != null) {
				if (!forClientDataPackManager) {
					((FabricResourcePackProfile) profile).fabric_setParentsPredicate(parents);
				}

				consumer.accept(profile);
			}
		}
	}
}
