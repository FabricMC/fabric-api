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

import java.util.SequencedSet;
import java.util.function.Consumer;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import net.minecraft.registry.tag.TagEntry;
import net.minecraft.registry.tag.TagGroupLoader;

import net.fabricmc.fabric.api.tag.v1.FabricTagEntry;

@Mixin(TagGroupLoader.class)
public class TagGroupLoaderMixin {
	@WrapOperation(
			method = "resolveAll",
			at = @At(value = "INVOKE", target = "Lnet/minecraft/registry/tag/TagEntry;resolve(Lnet/minecraft/registry/tag/TagEntry$ValueGetter;Ljava/util/function/Consumer;)Z")
	)
	private <T> boolean swapRemovalIdConsumer(TagEntry instance, TagEntry.ValueGetter<T> valueGetter, Consumer<T> idConsumer, Operation<Boolean> original, @Local SequencedSet<T> sequencedSet) {
		return original.call(
				instance,
				valueGetter,
				((FabricTagEntry) instance).isRemoved()
				? (Consumer<T>) sequencedSet::remove
				: idConsumer
		);
	}
}
