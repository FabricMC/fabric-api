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

package net.fabricmc.fabric.mixin.dimension;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import net.minecraft.core.RegistryAccess;

import net.fabricmc.fabric.impl.dimension.DimensionModificationImpl;
import net.fabricmc.fabric.impl.dimension.DimensionModificationMarker;

/**
 * This Mixin allows us to prevent double-modifications of dimensions via
 * {@link DimensionModificationImpl} on a per-DynamicRegistryManager basis.
 */
@Mixin(RegistryAccess.ImmutableRegistryAccess.class)
public class RegistryAccessImmutableRegistryAccessMixin implements DimensionModificationMarker {
	@Unique
	private boolean dimensionsModified;

	@Override
	public void fabric_markDimensionsModified() {
		if (dimensionsModified) {
			throw new IllegalStateException("Dimensions in this dynamic registries instance have already been modified");
		}

		dimensionsModified = true;
	}
}
