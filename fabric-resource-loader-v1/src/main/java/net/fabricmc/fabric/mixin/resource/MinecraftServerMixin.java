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

package net.fabricmc.fabric.mixin.resource;

import java.net.Proxy;
import java.util.List;

import com.mojang.datafixers.DataFixer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.Services;
import net.minecraft.server.WorldStem;
import net.minecraft.server.level.progress.LevelLoadListener;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.repository.KnownPack;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.world.level.storage.LevelStorageSource;

import net.fabricmc.fabric.api.resource.v1.DataResourceStore;
import net.fabricmc.fabric.impl.resource.FabricDataResourceStoreHolder;
import net.fabricmc.fabric.impl.resource.pack.BuiltinModResourcePackSource;
import net.fabricmc.fabric.impl.resource.pack.FabricOriginalKnownPacksGetter;
import net.fabricmc.fabric.impl.resource.pack.ModNioPackResources;

@Mixin(MinecraftServer.class)
public class MinecraftServerMixin implements DataResourceStore, FabricOriginalKnownPacksGetter {
	@Unique
	private List<KnownPack> originalKnownPacks;

	@Shadow
	private MinecraftServer.ReloadableResources resources;

	@SuppressWarnings("AddedMixinMembersNamePattern")
	@Override
	public <T> T getOrThrow(Key<T> key) {
		return ((FabricDataResourceStoreHolder) this.resources.managers())
				.fabric$getDataResourceStore()
				.getOrThrow(key);
	}

	@Inject(method = "<init>", at = @At("TAIL"))
	private void init(Thread serverThread, LevelStorageSource.LevelStorageAccess session, PackRepository dataPackManager, WorldStem saveLoader, Proxy proxy, DataFixer dataFixer, Services apiServices, LevelLoadListener chunkLoadProgress, CallbackInfo ci) {
		this.originalKnownPacks = saveLoader.resourceManager().listPacks().flatMap(pack -> pack.location().knownPackInfo().stream()).toList();
	}

	@Redirect(method = "configurePackRepository(Lnet/minecraft/server/packs/repository/PackRepository;Lnet/minecraft/world/level/WorldDataConfiguration;ZZ)Lnet/minecraft/world/level/WorldDataConfiguration;", at = @At(value = "INVOKE", target = "Ljava/util/List;contains(Ljava/lang/Object;)Z"))
	private static boolean onCheckDisabled(List<String> list, Object o, PackRepository resourcePackManager) {
		String profileId = (String) o;
		boolean contains = list.contains(profileId);

		if (contains) {
			return true;
		}

		Pack profile = resourcePackManager.getPack(profileId);

		if (profile.getPackSource() instanceof BuiltinModResourcePackSource) {
			try (PackResources pack = profile.open()) {
				// Prevents automatic load for built-in data packs provided by mods.
				return pack instanceof ModNioPackResources modPack && !modPack.getActivationType().isEnabledByDefault();
			}
		}

		return false;
	}

	@Override
	public List<KnownPack> fabric$getOriginalKnownPacks() {
		return this.originalKnownPacks;
	}
}
