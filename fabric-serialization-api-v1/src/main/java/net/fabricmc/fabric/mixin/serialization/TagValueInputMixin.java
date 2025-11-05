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

package net.fabricmc.fabric.mixin.serialization;

import java.util.Collection;
import java.util.Optional;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.storage.TagValueInput;

import net.fabricmc.fabric.api.serialization.v1.view.FabricReadView;

@Mixin(TagValueInput.class)
public class TagValueInputMixin implements FabricReadView {
	@Shadow
	@Final
	private CompoundTag input;

	@Override
	public Collection<String> keys() {
		return this.input.keySet();
	}

	@Override
	public boolean contains(String key) {
		return this.input.contains(key);
	}

	@Override
	public Optional<byte[]> getOptionalByteArray(String key) {
		return this.input.getByteArray(key);
	}

	@Override
	public Optional<long[]> getOptionalLongArray(String key) {
		return this.input.getLongArray(key);
	}
}
