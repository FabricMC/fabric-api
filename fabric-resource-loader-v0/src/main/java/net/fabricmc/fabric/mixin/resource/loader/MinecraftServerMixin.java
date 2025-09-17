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

package net.fabricmc.fabric.mixin.resource.loader;

import java.net.Proxy;
import java.util.List;

import com.mojang.datafixers.DataFixer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.registry.VersionedIdentifier;
import net.minecraft.resource.ResourcePack;
import net.minecraft.resource.ResourcePackManager;
import net.minecraft.resource.ResourcePackProfile;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.SaveLoader;
import net.minecraft.util.ApiServices;
import net.minecraft.world.chunk.ChunkLoadProgress;
import net.minecraft.world.level.storage.LevelStorage;

import net.fabricmc.fabric.impl.resource.loader.BuiltinModResourcePackSource;
import net.fabricmc.fabric.impl.resource.loader.FabricOriginalKnownPacksGetter;
import net.fabricmc.fabric.impl.resource.loader.ModNioResourcePack;

@Mixin(MinecraftServer.class)
public class MinecraftServerMixin implements FabricOriginalKnownPacksGetter {
	@Unique
	private List<VersionedIdentifier> fabric_originalKnownPacks;

	@Inject(method = "<init>", at = @At("TAIL"))
	private void init(Thread serverThread, LevelStorage.Session session, ResourcePackManager dataPackManager, SaveLoader saveLoader, Proxy proxy, DataFixer dataFixer, ApiServices apiServices, ChunkLoadProgress chunkLoadProgress, CallbackInfo ci) {
		this.fabric_originalKnownPacks = saveLoader.resourceManager().streamResourcePacks().flatMap(pack -> pack.getInfo().knownPackInfo().stream()).toList();
	}

	@Redirect(method = "loadDataPacks(Lnet/minecraft/resource/ResourcePackManager;Lnet/minecraft/resource/DataConfiguration;ZZ)Lnet/minecraft/resource/DataConfiguration;", at = @At(value = "INVOKE", target = "Ljava/util/List;contains(Ljava/lang/Object;)Z"))
	private static boolean onCheckDisabled(List<String> list, Object o, ResourcePackManager resourcePackManager) {
		String profileId = (String) o;
		boolean contains = list.contains(profileId);

		if (contains) {
			return true;
		}

		ResourcePackProfile profile = resourcePackManager.getProfile(profileId);

		if (profile.getSource() instanceof BuiltinModResourcePackSource) {
			try (ResourcePack pack = profile.createResourcePack()) {
				// Prevents automatic load for built-in data packs provided by mods.
				return pack instanceof ModNioResourcePack modPack && !modPack.getActivationType().isEnabledByDefault();
			}
		}

		return false;
	}

	@Override
	public List<VersionedIdentifier> fabric_getOriginalKnownPacks() {
		return this.fabric_originalKnownPacks;
	}
}
