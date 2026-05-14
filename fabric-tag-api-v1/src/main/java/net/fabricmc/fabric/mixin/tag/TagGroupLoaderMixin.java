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

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import com.google.common.collect.ImmutableSet;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.datafixers.util.Either;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.registry.tag.TagEntry;
import net.minecraft.registry.tag.TagFile;
import net.minecraft.registry.tag.TagGroupLoader;
import net.minecraft.resource.DependencyTracker;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;

import net.fabricmc.fabric.impl.tag.TagRemovalInternals;

@Mixin(TagGroupLoader.class)
public class TagGroupLoaderMixin {
	private static final ThreadLocal<LinkedHashSet<?>> fabric$currentValues = ThreadLocal.withInitial(LinkedHashSet::new);

	@Inject(method = "loadTags", at = @At(value = "INVOKE", target = "Ljava/util/List;forEach(Ljava/util/function/Consumer;)V", shift = At.Shift.AFTER))
	private void loadRemoveEntries(ResourceManager resourceManager, CallbackInfoReturnable<Map<Identifier, List<TagGroupLoader.TrackedEntry>>> cir, @Local(ordinal = 0) Identifier id, @Local TagFile parsedContents, @Local String sourceId) {
		Identifier normalizedId = TagRemovalInternals.normalizeTagResourceId(id);

		for (TagEntry entry : parsedContents.remove()) {
			TagGroupLoader.TrackedEntry entryWithSource = new TagGroupLoader.TrackedEntry(entry, sourceId);
			TagRemovalInternals.addRemoveEntry(normalizedId, entryWithSource);
		}

		TagRemovalInternals.addTagSource(normalizedId, sourceId);
	}

	@WrapOperation(method = "buildGroup", at = @At(value = "INVOKE", target = "Lnet/minecraft/resource/DependencyTracker;traverse(Ljava/util/function/BiConsumer;)V"))
	private void scopeIdAroundOrderByDependencies(DependencyTracker<Identifier, TagGroupLoader.TagDependencies> sorter, BiConsumer<Identifier, TagGroupLoader.TagDependencies> consumer, Operation<Void> original) {
		original.call(sorter, (BiConsumer<Identifier, TagGroupLoader.TagDependencies>) (id, contents) -> {
			TagRemovalInternals.setTagId(id);

			try {
				consumer.accept(id, contents);
			} finally {
				TagRemovalInternals.clearTagId();
			}
		});
	}

	@ModifyArg(method = "buildGroup", at = @At(value = "INVOKE", target = "Ljava/util/Map;forEach(Ljava/util/function/BiConsumer;)V"))
	private BiConsumer<Identifier, List<TagGroupLoader.TrackedEntry>> addTagRemovalReferencesToDependencyTracker(BiConsumer<Identifier, List<TagGroupLoader.TrackedEntry>> forEach) {
		return (id, entries) -> {
			TagRemovalInternals.mergeAddedAndRemovedEntries(id, entries);
			forEach.accept(id, entries);
		};
	}

	@WrapOperation(method = "resolveAll", at = @At(value = "INVOKE", target = "Lnet/minecraft/registry/tag/TagEntry;resolve(Lnet/minecraft/registry/tag/TagEntry$ValueGetter;Ljava/util/function/Consumer;)Z"))
	private <T> boolean removeEntriesFromTags(TagEntry instance, TagEntry.ValueGetter<T> lookup, Consumer<T> output, Operation<Boolean> original, @Local TagGroupLoader.TrackedEntry entry) {
		final LinkedHashSet<T> values = (LinkedHashSet<T>) fabric$currentValues.get();

		if (TagRemovalInternals.isEntryRemove(entry)) {
			instance.resolve(lookup, values::remove);
			return true;
		}

		return original.call(instance, lookup, (Consumer<T>) (values::add));
	}

	@Inject(method = "resolveAll", at = @At("HEAD"))
	private void initCurrentValues(TagEntry.ValueGetter<?> valueGetter, List<TagGroupLoader.TrackedEntry> entries, CallbackInfoReturnable<Either<Collection<TagGroupLoader.TrackedEntry>, Collection<?>>> cir) {
		fabric$currentValues.get().clear();
	}

	@Inject(method = "resolveAll", at = @At("RETURN"), cancellable = true)
	private void replaceRightReturn(TagEntry.ValueGetter<?> valueGetter, List<TagGroupLoader.TrackedEntry> entries, CallbackInfoReturnable<Either<Collection<TagGroupLoader.TrackedEntry>, Collection<?>>> cir) {
		Either<Collection<TagGroupLoader.TrackedEntry>, Collection<?>> returnValue = cir.getReturnValue();

		if (returnValue != null && returnValue.right().isPresent()) {
			cir.setReturnValue(Either.right(ImmutableSet.copyOf(fabric$currentValues.get())));
		}

		fabric$currentValues.get().clear();
	}

	@Inject(method = "buildGroup", at = @At("RETURN"))
	private <T> void removeTagRemovalReferencesWhenFinished(Map<Identifier, List<TagGroupLoader.TrackedEntry>> builders, CallbackInfoReturnable<Map<Identifier, List<T>>> cir) {
		TagRemovalInternals.removeTagRemovalReferences();
	}
}
