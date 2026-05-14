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

package net.fabricmc.fabric.mixin.datagen;

import com.llamalad7.mixinextras.sugar.Local;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import net.minecraft.data.server.tag.TagProvider;
import net.minecraft.registry.tag.TagBuilder;

import net.fabricmc.fabric.impl.datagen.TagBuilderHooks;
import net.fabricmc.fabric.impl.tag.TagFileHooks;

@Mixin(TagProvider.class)
public class TagProviderMixin<T> {
	@ModifyArg(method = "method_27046", at = @At(value = "INVOKE", target = "Lnet/minecraft/data/DataProvider;writeCodecToPath(Lnet/minecraft/data/DataWriter;Lnet/minecraft/registry/RegistryWrapper$WrapperLookup;Lcom/mojang/serialization/Codec;Ljava/lang/Object;Ljava/nio/file/Path;)Ljava/util/concurrent/CompletableFuture;"), index = 3)
	private T addRemove(T value, @Local TagBuilder builder) {
		((TagFileHooks) value).fabric_setRemove(((TagBuilderHooks) builder).fabric_getRemove());
		return value;
	}

	@ModifyArg(method = "method_27046", at = @At(value = "INVOKE", target = "Lnet/minecraft/registry/tag/TagFile;<init>(Ljava/util/List;Z)V"), index = 1)
	private boolean addReplaced(boolean replaced, @Local TagBuilder builder) {
		return ((TagBuilderHooks) builder).fabric_isReplaced();
	}
}
