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
import com.mojang.datafixers.util.Either;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.WritableRegistry;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.tags.TagEntry;
import net.minecraft.tags.TagFile;
import net.minecraft.tags.TagKey;
import net.minecraft.tags.TagLoader;

import net.fabricmc.fabric.impl.tag.TagRemovalInternals;

@Mixin(TagLoader.class)
public class TagLoaderMixin {
	@Inject(method = "load", at = @At(value = "INVOKE", target = "Ljava/util/List;clear()V"))
	private void removeTagRemovalReferenceOnReplace(ResourceManager resourceManager, CallbackInfoReturnable<Map<Identifier, List<TagLoader.EntryWithSource>>> cir, @Local(name = "id") Identifier id) {
		TagRemovalInternals.removeTagRemovalReference(id);
	}

	@Inject(method = "load", at = @At(value = "INVOKE", target = "Ljava/util/List;forEach(Ljava/util/function/Consumer;)V", shift = At.Shift.AFTER))
	private void loadRemoveEntries(ResourceManager resourceManager, CallbackInfoReturnable<Map<Identifier, List<TagLoader.EntryWithSource>>> cir, @Local(name = "id") Identifier id, @Local(name = "parsedContents") TagFile parsedContents, @Local(name = "sourceId") String sourceId) {
		for (TagEntry entry : parsedContents.remove()) {
			TagLoader.EntryWithSource entryWithSource = new TagLoader.EntryWithSource(entry, sourceId);
			TagRemovalInternals.addRemoveEntry(id, entryWithSource);
		}

		TagRemovalInternals.addTagSource(id, sourceId);
	}

	@WrapOperation(method = "lambda$build$1", at = @At(value = "INVOKE", target = "Lnet/minecraft/tags/TagLoader;tryBuildTag(Lnet/minecraft/tags/TagEntry$Lookup;Ljava/util/List;)Lcom/mojang/datafixers/util/Either;"))
	private <T> Either<List<TagLoader.EntryWithSource>, List<T>> scopeIdToTryBuildTag(TagLoader<T> instance, TagEntry.Lookup<T> lookup, List<TagLoader.EntryWithSource> entries, Operation<Either<List<TagLoader.EntryWithSource>, List<T>>> original, @Local(argsOnly = true) Identifier id) {
		// List is merged here to make
		return ScopedValue.where(TagRemovalInternals.TAG_ID_SCOPED_VALUE, id)
				.call(() -> original.call(instance, lookup, TagRemovalInternals.mergeAddedAndRemovedEntries(id, entries)));
	}

	@WrapOperation(method = "tryBuildTag", at = @At(value = "INVOKE", target = "Lnet/minecraft/tags/TagEntry;build(Lnet/minecraft/tags/TagEntry$Lookup;Ljava/util/function/Consumer;)Z"))
	private <T> boolean removeEntriesFromTags(TagEntry instance, TagEntry.Lookup<T> lookup, Consumer<T> output, Operation<Boolean> original, @Local(name = "values") SequencedSet<T> values, @Local(name = "entry") TagLoader.EntryWithSource entry) {
		if (TagRemovalInternals.isEntryRemove(entry)) {
			instance.build(lookup, values::remove);
			return true;
		}

		return original.call(instance, lookup, output);
	}

	@Inject(method = "build", at = @At("RETURN"))
	private <T> void removeTagRemovalReferencesWhenFinished(Map<Identifier, List<TagLoader.EntryWithSource>> builders, CallbackInfoReturnable<Map<Identifier, List<T>>> cir) {
		TagRemovalInternals.removeTagRemovalReferences();
	}

	// Fixes a likely vanilla bug causing loot table tags to not get loaded.
	@WrapOperation(method = "loadTagsForRegistry(Lnet/minecraft/server/packs/resources/ResourceManager;Lnet/minecraft/core/WritableRegistry;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/tags/TagLoader;loadTagsForRegistry(Lnet/minecraft/server/packs/resources/ResourceManager;Lnet/minecraft/resources/ResourceKey;Lnet/minecraft/tags/TagLoader$ElementLookup;)Ljava/util/Map;"))
	private static <T> Map<TagKey<T>, List<Holder<T>>> loadTagsForRegistry(ResourceManager manager, ResourceKey<? extends Registry<T>> registryKey, TagLoader.ElementLookup<Holder<T>> lookup, Operation<Map<TagKey<T>, List<Holder<T>>>> original, @Local(argsOnly = true) WritableRegistry<T> registry) {
		Map<TagKey<T>, List<Holder<T>>> tags = original.call(manager, registryKey, lookup);
		registry.bindTags(tags);
		return tags;
	}
}
