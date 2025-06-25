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

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import net.minecraft.registry.tag.TagEntry;

import net.fabricmc.fabric.api.tag.v1.FabricTagEntry;

@Mixin(TagEntry.class)
public class TagEntryMixin implements FabricTagEntry {
	@Shadow
	@Final
	private boolean required;

	@Unique
	private final boolean removed;

	public TagEntryMixin() { }

	{
		removed = net.fabricmc.fabric.impl.tag.FabricTagEntryImpl.REMOVED.get() != null;
		required = required && !removed;
	}

	@Override
	public boolean isRemoved() {
		return this.removed;
	}
}
