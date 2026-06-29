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
import java.util.stream.Stream;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import net.minecraft.data.tags.TagAppender;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagBuilder;
import net.minecraft.tags.TagKey;

import net.fabricmc.fabric.api.datagen.v1.provider.FabricProvidedTagBuilder;
import net.fabricmc.fabric.impl.datagen.TagBuilderHooks;

/**
 * Extends ProvidedTagBuilder to support setting the {@code replace} and {@code fabric:remove} fields.
 */
@SuppressWarnings({"rawtypes", "unchecked"})
@Mixin(TagAppender.class)
interface TagAppenderMixin<E, T> extends FabricProvidedTagBuilder<E, T> {
	@Mixin(targets = "net.minecraft.data.tags.TagAppender$1")
	abstract class ProvidedTagBuilder1Mixin<E, T> implements TagAppenderMixin<E, T> {
		// the builder param
		@Shadow
		@Final
		TagBuilder val$builder;

		@Override
		public TagAppender setReplace(boolean replace) {
			((TagBuilderHooks) this.val$builder).fabric_setReplace(replace);
			return (TagAppender<E, T>) this;
		}

		@Override
		public TagAppender<E, T> forceAddTag(TagKey<T> tag) {
			((TagBuilderHooks) this.val$builder).fabric_forceAddTag(tag.location());
			return (TagAppender<E, T>) this;
		}

		@Override
		public TagAppender remove(ResourceKey<T> element) {
			((TagBuilderHooks) this.val$builder).fabric_removeElement(element.identifier());
			return (TagAppender<E, T>) this;
		}

		@Override
		public TagAppender remove(final ResourceKey<T>... elements) {
			return removeAll(Arrays.stream(elements));
		}

		@Override
		public TagAppender removeAll(final Collection<ResourceKey<T>> elements) {
			elements.forEach(element -> ((TagBuilderHooks) this.val$builder).fabric_removeElement(element.identifier()));
			return (TagAppender<E, T>) this;
		}

		@Override
		public TagAppender removeAll(final Stream<ResourceKey<T>> elements) {
			elements.forEach(element -> ((TagBuilderHooks) this.val$builder).fabric_removeElement(element.identifier()));
			return (TagAppender<E, T>) this;
		}

		@Override
		public TagAppender removeTag(TagKey<T> tag) {
			((TagBuilderHooks) this.val$builder).fabric_removeTag(tag.location());
			return (TagAppender<E, T>) this;
		}
	}

	@Mixin(targets = "net.minecraft.data.tags.TagAppender$2")
	abstract class ProvidedTagBuilder2Mixin<E, T> implements TagAppenderMixin<E, T> {
		// ProvidedTagBuilder.this
		@Shadow
		@Final
		TagAppender val$original;

		@Override
		public TagAppender setReplace(boolean replace) {
			((FabricProvidedTagBuilder) this.val$original).setReplace(replace);
			return (TagAppender<E, T>) this;
		}

		@Override
		public TagAppender<E, T> forceAddTag(TagKey<T> tag) {
			((FabricProvidedTagBuilder) this.val$original).forceAddTag(tag);
			return (TagAppender<E, T>) this;
		}
	}
}
