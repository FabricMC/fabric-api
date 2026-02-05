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

package net.fabricmc.fabric.mixin.tag;

import java.util.List;
import java.util.Map;
import java.util.SequencedSet;
import java.util.function.Consumer;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import net.fabricmc.fabric.api.tag.v1.FabricTagEntry;

import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.WritableRegistry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.tags.TagEntry;
import net.minecraft.tags.TagKey;
import net.minecraft.tags.TagLoader;

@Mixin(TagLoader.class)
public class TagLoaderMixin {
	@WrapOperation(
			method = "tryBuildTag",
			at = @At(value = "INVOKE", target = "Lnet/minecraft/tags/TagEntry;build(Lnet/minecraft/tags/TagEntry$Lookup;Ljava/util/function/Consumer;)Z")
	)
	private <T> boolean swapRemovalIdConsumer(TagEntry instance, TagEntry.Lookup<T> valueGetter, Consumer<T> idConsumer, Operation<Boolean> original, @Local SequencedSet<T> sequencedSet) {
		return original.call(
				instance,
				valueGetter,
				((FabricTagEntry) instance).isRemoved()
						? (Consumer<T>) sequencedSet::remove
						: idConsumer
		);
	}

	// Fixes a likely vanilla bug causing loot table tags to not get loaded.
	@WrapOperation(method = "loadTagsForRegistry(Lnet/minecraft/server/packs/resources/ResourceManager;Lnet/minecraft/core/WritableRegistry;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/tags/TagLoader;loadTagsForRegistry(Lnet/minecraft/server/packs/resources/ResourceManager;Lnet/minecraft/resources/ResourceKey;Lnet/minecraft/tags/TagLoader$ElementLookup;)Ljava/util/Map;"))
	private static <T> Map<TagKey<T>, List<Holder<T>>> loadTagsForRegistry(ResourceManager manager, ResourceKey<? extends Registry<T>> registryKey, TagLoader.ElementLookup<Holder<T>> lookup, Operation<Map<TagKey<T>, List<Holder<T>>>> original, @Local(argsOnly = true) WritableRegistry<T> registry) {
		Map<TagKey<T>, List<Holder<T>>> tags = original.call(manager, registryKey, lookup);
		registry.bindTags(tags);
		return tags;
	}
}
