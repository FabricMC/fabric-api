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

package net.fabricmc.fabric.mixin.loot;

import java.util.Map;

import com.google.gson.JsonElement;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DynamicOps;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.world.level.storage.loot.LootDataType;

import net.fabricmc.fabric.impl.loot.LootUtil;

@Mixin(SimpleJsonResourceReloadListener.class)
public class SimpleJsonResourceReloadListenerMixin {
	@Inject(method = "scanDirectory(Lnet/minecraft/server/packs/resources/ResourceManager;Lnet/minecraft/resources/FileToIdConverter;Lcom/mojang/serialization/DynamicOps;Lcom/mojang/serialization/Codec;Ljava/util/Map;)V", at = @At(value = "INVOKE_ASSIGN", target = "Lnet/minecraft/resources/FileToIdConverter;fileToId(Lnet/minecraft/resources/Identifier;)Lnet/minecraft/resources/Identifier;", shift = At.Shift.AFTER))
	private static <T> void fillSourceMap(ResourceManager manager, FileToIdConverter resourceFinder, DynamicOps<JsonElement> ops, Codec<T> codec, Map<Identifier, T> result, CallbackInfo ci, @Local Map.Entry<Identifier, Resource> entry, @Local(ordinal = 1) Identifier id) {
		final String dirName = ((FileToIdConverterAccessor) resourceFinder).getDirectoryName();
		if (!LootDataType.TABLE.registryKey().identifier().getPath().equals(dirName)) return;

		LootUtil.SOURCES.get().put(id, LootUtil.determineSource(entry.getValue()));
	}
}
