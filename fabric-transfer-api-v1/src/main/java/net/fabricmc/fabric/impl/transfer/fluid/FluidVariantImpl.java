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

package net.fabricmc.fabric.impl.transfer.fluid;

import java.util.Objects;

import org.jspecify.annotations.Nullable;

import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.PatchedDataComponentMap;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;

import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.impl.transfer.TransferApiImpl;

public class FluidVariantImpl implements FluidVariant {
	public static FluidVariant of(Fluid fluid, DataComponentPatch components) {
		Objects.requireNonNull(fluid, "Fluid may not be null.");
		Objects.requireNonNull(components, "Components may not be null.");

		if (!fluid.isSource(fluid.defaultFluidState()) && fluid != Fluids.EMPTY) {
			// Note: the empty fluid is not still, that's why we check for it specifically.

			if (fluid instanceof FlowingFluid flowable) {
				// Normalize FlowableFluids to their still variants.
				fluid = flowable.getSource();
			} else {
				// If not a FlowableFluid, we don't know how to convert -> crash.
				Identifier id = BuiltInRegistries.FLUID.getKey(fluid);
				throw new IllegalArgumentException("Cannot convert flowing fluid %s (%s) into a still fluid.".formatted(id, fluid));
			}
		}

		if (components.isEmpty() || fluid == Fluids.EMPTY) {
			// Use the cached variant inside the fluid
			return ((FluidVariantCache) fluid).fabric_getCachedFluidVariant();
		} else {
			// TODO explore caching fluid variants for non null tags.
			return new FluidVariantImpl(fluid, components);
		}
	}

	public static FluidVariant of(Holder<Fluid> fluid, DataComponentPatch components) {
		return of(fluid.value(), components);
	}

	private final Fluid fluid;
	private final DataComponentPatch components;
	private final DataComponentMap componentMap;
	private final int hashCode;

	public FluidVariantImpl(Fluid fluid, DataComponentPatch components) {
		this.fluid = fluid;
		this.components = components;
		this.componentMap = components == DataComponentPatch.EMPTY ? DataComponentMap.EMPTY : PatchedDataComponentMap.fromPatch(DataComponentMap.EMPTY, components);
		this.hashCode = Objects.hash(fluid, components);
	}

	@Override
	public boolean isBlank() {
		return fluid == Fluids.EMPTY;
	}

	@Override
	public Fluid getObject() {
		return fluid;
	}

	@Override
	public @Nullable DataComponentPatch getComponents() {
		return components;
	}

	@Override
	public DataComponentMap getComponentMap() {
		return componentMap;
	}

	@Override
	public FluidVariant withComponentChanges(DataComponentPatch changes) {
		return of(fluid, TransferApiImpl.mergeChanges(getComponents(), changes));
	}

	@Override
	public String toString() {
		return "FluidVariant{fluid=" + fluid + ", components=" + components + '}';
	}

	@Override
	public boolean equals(Object o) {
		// succeed fast with == check
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		FluidVariantImpl fluidVariant = (FluidVariantImpl) o;
		// fail fast with hash code
		return hashCode == fluidVariant.hashCode && fluid == fluidVariant.fluid && componentsMatch(fluidVariant.components);
	}

	@Override
	public int hashCode() {
		return hashCode;
	}
}
