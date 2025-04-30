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

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import net.minecraft.registry.tag.TagBuilder;
import net.minecraft.registry.tag.TagEntry;
import net.minecraft.util.Identifier;

import net.fabricmc.fabric.impl.datagen.FabricTagBuilder;
import net.fabricmc.fabric.impl.datagen.ForcedTagEntry;

@Mixin(TagBuilder.class)
public abstract class TagBuilderMixin implements FabricTagBuilder {
	@Shadow
	public abstract TagBuilder add(TagEntry entry);

	@Unique
	private boolean replace = false;

	@Override
	public void fabric_setReplace(boolean replace) {
		this.replace = replace;
	}

	@Override
	public boolean fabric_isReplaced() {
		return this.replace;
	}

	@Override
	public void fabric_forceAddTag(Identifier tag) {
		this.add(new ForcedTagEntry(tag));
	}
}
