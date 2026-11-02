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

package net.fabricmc.fabric.impl.biome.modification.data;

import net.fabricmc.fabric.api.biome.v1.BiomeModificationContext;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectionContext;
import net.fabricmc.fabric.api.biome.v1.ModificationPhase;
import net.fabricmc.fabric.api.biome.v1.data.BiomeModifier;
import net.fabricmc.fabric.api.biome.v1.data.BiomeModifierType;

public final class NoneBiomeModifier implements BiomeModifier {
	public static final NoneBiomeModifier INSTANCE = new NoneBiomeModifier();

	private NoneBiomeModifier() {
	}

	@Override
	public BiomeModifierType<?> getType() {
		return BiomeModifiersImpl.NONE;
	}

	@Override
	public ModificationPhase getPhase() {
		return ModificationPhase.ADDITIONS;
	}

	@Override
	public boolean matches(BiomeSelectionContext context) {
		return false;
	}

	@Override
	public void modify(BiomeSelectionContext selection, BiomeModificationContext context) {
	}
}
