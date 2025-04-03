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

import java.util.List;
import java.util.function.Function;

import net.minecraft.class_10961;

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
import net.minecraft.world.level.storage.LevelStorage;

import net.fabricmc.fabric.impl.resource.loader.BuiltinModResourcePackSource;
import net.fabricmc.fabric.impl.resource.loader.FabricOriginalKnownPacksGetter;
import net.fabricmc.fabric.impl.resource.loader.ModNioResourcePack;

@Mixin(class_10961.class)
public class GameInstanceMixin implements FabricOriginalKnownPacksGetter {
	@Unique
	private List<VersionedIdentifier> fabric_originalKnownPacks;

	@Inject(method = "<init>", at = @At("TAIL"))
	private void init(MinecraftServer minecraftServer, ResourcePackManager resourcePackManager, SaveLoader saveLoader, LevelStorage.Session session, Function function, CallbackInfo ci) {
		this.fabric_originalKnownPacks = saveLoader.resourceManager().streamResourcePacks().flatMap(pack -> pack.getInfo().knownPackInfo().stream()).toList();
	}

	@Override
	public List<VersionedIdentifier> fabric_getOriginalKnownPacks() {
		return this.fabric_originalKnownPacks;
	}
}
