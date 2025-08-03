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

package net.fabricmc.fabric.mixin.resource.loader.client;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.option.GameOptions;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtSizeTracker;
import net.minecraft.nbt.NbtString;
import net.minecraft.resource.ResourcePack;
import net.minecraft.resource.ResourcePackProfile;

import net.fabricmc.fabric.impl.resource.loader.FabricResourcePackProfile;
import net.fabricmc.fabric.impl.resource.loader.ModNioResourcePack;
import net.fabricmc.fabric.impl.resource.loader.ModResourcePackCreator;
import net.fabricmc.loader.api.FabricLoader;

@Mixin(GameOptions.class)
public class GameOptionsMixin {
	@Shadow
	public List<String> resourcePacks;

	@Shadow
	@Final
	static Logger LOGGER;

	@Inject(method = "load", at = @At("RETURN"))
	private void onLoad(CallbackInfo ci) {
		// Track built-in resource packs if they are enabled by default.
		// - If there is NO value with matching resource pack id, add it to the enabled packs and the tracker file.
		// - If there is a matching value and pack id, do not add it to the enabled packs and let
		//   the options value decides if it is enabled or not.
		// - If there is a value without matching pack id (e.g. because the mod is removed),
		//   remove it from the tracker file so that it would be enabled again if added back later.

		Path dataDir = FabricLoader.getInstance().getGameDir().resolve("data");

		if (Files.notExists(dataDir)) {
			try {
				Files.createDirectories(dataDir);
			} catch (IOException e) {
				LOGGER.warn("[Fabric Resource Loader] Could not create data directory: " + dataDir.toAbsolutePath());
			}
		}

		Path trackerFile = dataDir.resolve("fabricDefaultResourcePacks.dat");
		Set<String> trackedPacks = new HashSet<>();

		if (Files.exists(trackerFile)) {
			try {
				NbtCompound data = NbtIo.readCompressed(trackerFile, NbtSizeTracker.ofUnlimitedBytes());
				NbtList values = data.getList("values").orElseThrow();

				for (int i = 0; i < values.size(); i++) {
					trackedPacks.add(values.getString(i).orElseThrow());
				}
			} catch (IOException e) {
				LOGGER.warn("[Fabric Resource Loader] Could not read " + trackerFile.toAbsolutePath(), e);
			}
		}

		Set<String> removedPacks = new HashSet<>(trackedPacks);
		Set<String> resourcePacks = new LinkedHashSet<>(this.resourcePacks);

		List<ResourcePackProfile> profiles = new ArrayList<>();
		ModResourcePackCreator.CLIENT_RESOURCE_PACK_PROVIDER.register(profiles::add);

		for (ResourcePackProfile profile : profiles) {
			// Always add "Fabric Mods" pack to enabled resource packs.
			if (profile.getId().equals(ModResourcePackCreator.FABRIC)) {
				resourcePacks.add(profile.getId());
				continue;
			}

			try (ResourcePack pack = profile.createResourcePack()) {
				if (pack instanceof ModNioResourcePack builtinPack && builtinPack.getActivationType().isEnabledByDefault()) {
					if (trackedPacks.add(builtinPack.getId())) {
						resourcePacks.add(profile.getId());
					} else {
						removedPacks.remove(builtinPack.getId());
					}
				}
			}
		}

		try {
			NbtList values = new NbtList();

			for (String id : trackedPacks) {
				if (!removedPacks.contains(id)) {
					values.add(NbtString.of(id));
				}
			}

			NbtCompound nbt = new NbtCompound();
			nbt.put("values", values);
			NbtIo.writeCompressed(nbt, trackerFile);
		} catch (IOException e) {
			LOGGER.warn("[Fabric Resource Loader] Could not write to " + trackerFile.toAbsolutePath(), e);
		}

		this.resourcePacks = new ArrayList<>(resourcePacks);
	}

	@WrapOperation(method = "refreshResourcePacks", at = @At(value = "INVOKE", target = "Lnet/minecraft/resource/ResourcePackProfile;isPinned()Z"))
	private boolean excludeInternalResourcePacksFromRefreshCheck(ResourcePackProfile instance, Operation<Boolean> original) {
		// Treat Fabric hidden resource packs as pinned during the check for changed resource packs so that they won't count as changed when refreshing resource packs
		return original.call(instance) || ((FabricResourcePackProfile) instance).fabric_isHidden();
	}
}
