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

package net.fabricmc.fabric.impl.content.registry.fluid;

import java.util.Collection;
import java.util.HashMap;
import java.util.Objects;

import net.minecraft.tags.TagKey;
import net.minecraft.world.level.material.Fluid;

import net.fabricmc.fabric.api.registry.fluid.FluidBehavior;

public final class EntityFluidInteractionRegistryImpl {
	private static final HashMap<TagKey<Fluid>, FluidBehavior> TRACKED_FLUIDS = new HashMap<>();

	public static void register(TagKey<Fluid> fluidTagKey, FluidBehavior behaviour) {
		TRACKED_FLUIDS.put(fluidTagKey, behaviour);
	}

	public static Collection<TagKey<Fluid>> getTrackedFluids() {
		return TRACKED_FLUIDS.keySet();
	}

	public static FluidBehavior getFluidBehavior(TagKey<Fluid> tagKey) {
		return Objects.requireNonNull(TRACKED_FLUIDS.get(tagKey));
	}
}
