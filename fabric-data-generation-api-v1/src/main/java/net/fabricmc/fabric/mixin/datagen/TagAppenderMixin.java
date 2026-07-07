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

import java.util.Arrays;
import java.util.Collection;
import java.util.function.Function;
import java.util.stream.Stream;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import net.minecraft.data.tags.TagAppender;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagBuilder;
import net.minecraft.tags.TagKey;

import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagAppender;
import net.fabricmc.fabric.impl.datagen.TagBuilderHooks;

/**
 * Extends TagAppender to support setting the {@code replace} and {@code fabric:remove} fields.
 */
@SuppressWarnings({"rawtypes", "unchecked"})
@Mixin(TagAppender.class)
interface TagAppenderMixin<E, T> extends FabricTagAppender<E, T> {
	@Mixin(targets = "net.minecraft.data.tags.TagAppender$1")
	abstract class TagAppender1Mixin<T> implements TagAppenderMixin<ResourceKey<T>, T> {
		// the builder param
		@Shadow
		@Final
		TagBuilder val$builder;

		@Override
		public TagAppender<ResourceKey<T>, T> setReplace(boolean replace) {
			((TagBuilderHooks) this.val$builder).fabric_setReplace(replace);
			return (TagAppender<ResourceKey<T>, T>) this;
		}

		@Override
		public TagAppender<ResourceKey<T>, T> forceAddTag(TagKey<T> tag) {
			((TagBuilderHooks) this.val$builder).fabric_forceAddTag(tag.location());
			return (TagAppender<ResourceKey<T>, T>) this;
		}

		@Override
		public TagAppender<ResourceKey<T>, T> remove(ResourceKey<T> element) {
			((TagBuilderHooks) this.val$builder).fabric_removeElement(element.identifier());
			return (TagAppender<ResourceKey<T>, T>) this;
		}

		@Override
		public TagAppender<ResourceKey<T>, T> remove(final ResourceKey<T>... elements) {
			return removeAll(Arrays.stream(elements));
		}

		@Override
		public TagAppender<ResourceKey<T>, T> removeAll(final Collection<ResourceKey<T>> elements) {
			elements.forEach(element -> ((TagBuilderHooks) this.val$builder).fabric_removeElement(element.identifier()));
			return (TagAppender<ResourceKey<T>, T>) this;
		}

		@Override
		public TagAppender<ResourceKey<T>, T> removeAll(final Stream<ResourceKey<T>> elements) {
			elements.forEach(element -> ((TagBuilderHooks) this.val$builder).fabric_removeElement(element.identifier()));
			return (TagAppender<ResourceKey<T>, T>) this;
		}

		@Override
		public TagAppender<ResourceKey<T>, T> removeTag(TagKey<T> tag) {
			((TagBuilderHooks) this.val$builder).fabric_removeTag(tag.location());
			return (TagAppender<ResourceKey<T>, T>) this;
		}
	}

	@Mixin(targets = "net.minecraft.data.tags.TagAppender$2")
	abstract class TagAppender2Mixin<U, E, T> implements TagAppenderMixin<U, T> {
		// TagAppender.this
		@Shadow
		@Final
		TagAppender val$original;

		@Shadow
		@Final
		Function<U, E> val$converter;

		@Override
		public TagAppender<U, T> setReplace(boolean replace) {
			this.val$original.setReplace(replace);
			return (TagAppender<U, T>) this;
		}

		@Override
		public TagAppender<U, T> forceAddTag(TagKey<T> tag) {
			this.val$original.forceAddTag(tag);
			return (TagAppender<U, T>) this;
		}

		@Override
		public TagAppender<U, T> remove(U element) {
			val$original.remove(val$converter.apply(element));
			return (TagAppender<U, T>) this;
		}

		@Override
		public TagAppender<U, T> remove(final U... elements) {
			return removeAll(Arrays.stream(elements));
		}

		@Override
		public TagAppender<U, T> removeAll(final Collection<U> elements) {
			elements.forEach(element -> val$original.remove(val$converter.apply(element)));
			return (TagAppender<U, T>) this;
		}

		@Override
		public TagAppender<U, T> removeAll(final Stream<U> elements) {
			elements.forEach(element -> val$original.remove(val$converter.apply(element)));
			return (TagAppender<U, T>) this;
		}

		@Override
		public TagAppender<U, T> removeTag(TagKey<T> tag) {
			val$original.removeTag(tag);
			return (TagAppender<U, T>) this;
		}

		@WrapOperation(
				method = "addOptional",
				at = @At(value = "INVOKE", target = "Lnet/minecraft/data/tags/TagAppender;add(Ljava/lang/Object;)Lnet/minecraft/data/tags/TagAppender;")
		)
		private TagAppender<E, T> fixAddOptional(TagAppender instance, E e, Operation<TagAppender<E, T>> original) {
			return instance.addOptional(e);
		}
	}
}
