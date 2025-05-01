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

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import net.minecraft.data.tag.ProvidedTagBuilder;
import net.minecraft.registry.tag.TagBuilder;
import net.minecraft.registry.tag.TagKey;

import net.fabricmc.fabric.api.datagen.v1.provider.FabricProvidedTagBuilder;
import net.fabricmc.fabric.impl.datagen.FabricTagBuilder;

/**
 * Extends ProvidedTagBuilder to support setting the replace field.
 */
@SuppressWarnings({"rawtypes", "unchecked"})
@Mixin(ProvidedTagBuilder.class)
interface ProvidedTagBuilderMixin<E, T> extends FabricProvidedTagBuilder<E, T> {
	@Mixin(targets = "net.minecraft.data.tag.ProvidedTagBuilder$1")
	abstract class ProvidedTagBuilder1Mixin<E, T> implements ProvidedTagBuilderMixin<E, T> {
		// the builder param
		@Shadow
		@Final
		TagBuilder field_60483;

		@Override
		public ProvidedTagBuilder<E, T> setReplace(boolean replace) {
			((FabricTagBuilder) this.field_60483).fabric_setReplace(replace);
			return (ProvidedTagBuilder<E, T>) this;
		}

		@Override
		public ProvidedTagBuilder<E, T> forceAddTag(TagKey<T> tag) {
			((FabricTagBuilder) this.field_60483).fabric_forceAddTag(tag.id());
			return (ProvidedTagBuilder<E, T>) this;
		}
	}

	@Mixin(targets = "net.minecraft.data.tag.ProvidedTagBuilder$2")
	abstract class ProvidedTagBuilder2Mixin<E, T> implements ProvidedTagBuilderMixin<E, T> {
		// ProvidedTagBuilder.this
		@Shadow
		@Final
		ProvidedTagBuilder field_60484;

		@Override
		public ProvidedTagBuilder<E, T> setReplace(boolean replace) {
			((FabricProvidedTagBuilder) this.field_60484).setReplace(replace);
			return (ProvidedTagBuilder<E, T>) this;
		}

		@Override
		public ProvidedTagBuilder<E, T> forceAddTag(TagKey<T> tag) {
			((FabricProvidedTagBuilder) this.field_60484).forceAddTag(tag);
			return (ProvidedTagBuilder<E, T>) this;
		}
	}
}
