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

package net.fabricmc.fabric.api.registry.fluid;

import net.minecraft.tags.TagKey;
import net.minecraft.world.level.material.Fluid;

/**
 * Entity extensions related to fluid interaction handling.
 */
public interface EntityFluidExtension {
	/**
	 * Checks if entity is in a specific fluid type.
	 * The fluid must be fist registered within the {@link EntityFluidInteractionRegistry}.
	 *
	 * @param type tag representing the fluid type
	 * @return true if entity is in specific fluid, false otherwise
	 */
	default boolean isInFluid(TagKey<Fluid> type) {
		throw new AssertionError("Implemented in Mixin");
	}
}
