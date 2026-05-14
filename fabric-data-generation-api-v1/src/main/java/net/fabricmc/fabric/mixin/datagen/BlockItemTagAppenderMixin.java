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

import java.util.Collection;
import java.util.stream.Stream;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import net.minecraft.data.tags.BlockItemTagAppender;
import net.minecraft.data.tags.TagAppender;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;

import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagAppender;
import net.fabricmc.fabric.impl.datagen.TagBuilderHooks;

/**
 * Extends TagAppender to support setting the replace field.
 */
@SuppressWarnings({"rawtypes", "unchecked"})
@Mixin(BlockItemTagAppender.class)
abstract class BlockItemTagAppenderMixin<T> implements FabricTagAppender<T> {
	// the builder param
	@Shadow
	@Final
	TagAppender<T> original;

	@Override
	public BlockItemTagAppender<T> setReplace(boolean replace) {
		if (this.original.getBuilder() instanceof TagBuilderHooks builder) {
			builder.fabric_setReplace(replace);
		}

		return (BlockItemTagAppender<T>) (Object) this;
	}

	@Override
	public BlockItemTagAppender<T> forceAddTag(TagKey<T> tag) {
		if (this.original.getBuilder() instanceof TagBuilderHooks builder) {
			builder.fabric_forceAddTag(tag.location());
		}

		return (BlockItemTagAppender<T>) (Object) this;
	}

	@Override
	public BlockItemTagAppender<T> remove(ResourceKey<T> element) {
		if (this.original.getBuilder() instanceof TagBuilderHooks builder) {
			builder.fabric_removeElement(element.identifier());
		}

		return (BlockItemTagAppender<T>) (Object) this;
	}

	@Override
	public BlockItemTagAppender<T> remove(ResourceKey<T>... elements) {
		if (this.original.getBuilder() instanceof TagBuilderHooks builder) {
			Stream.of(elements).forEach(element -> builder.fabric_removeElement(element.identifier()));
		}

		return (BlockItemTagAppender<T>) (Object) this;
	}

	@Override
	public BlockItemTagAppender<T> removeAll(Collection<ResourceKey<T>> elements) {
		if (this.original.getBuilder() instanceof TagBuilderHooks builder) {
			elements.forEach(element -> builder.fabric_removeElement(element.identifier()));
		}

		return (BlockItemTagAppender<T>) (Object) this;
	}

	@Override
	public BlockItemTagAppender<T> removeAll(Stream<ResourceKey<T>> elements) {
		if (this.original.getBuilder() instanceof TagBuilderHooks builder) {
			elements.forEach(element -> builder.fabric_removeElement(element.identifier()));
		}

		return (BlockItemTagAppender<T>) (Object) this;
	}

	@Override
	public BlockItemTagAppender<T> removeTag(TagKey<T> tag) {
		if (this.original.getBuilder() instanceof TagBuilderHooks builder) {
			builder.fabric_removeTag(tag.location());
		}

		return (BlockItemTagAppender<T>) (Object) this;
	}
}
