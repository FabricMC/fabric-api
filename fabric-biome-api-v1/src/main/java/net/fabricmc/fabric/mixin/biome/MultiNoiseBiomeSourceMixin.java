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

package net.fabricmc.fabric.mixin.biome;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import net.minecraft.world.level.biome.MultiNoiseBiomeSource;

import net.fabricmc.fabric.impl.biome.BiomeSourceAccess;

@Mixin(MultiNoiseBiomeSource.class)
public class MultiNoiseBiomeSourceMixin implements BiomeSourceAccess {
	@Unique
	private boolean modifyBiomeEntries = true;

	@Override
	public void fabric_setModifyBiomeEntries(boolean modifyBiomeEntries) {
		this.modifyBiomeEntries = modifyBiomeEntries;
	}

	@Override
	public boolean fabric_shouldModifyBiomeEntries() {
		return this.modifyBiomeEntries;
	}
}
