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

import org.jetbrains.annotations.Nullable;

import net.minecraft.component.ComponentChanges;
import net.minecraft.component.ComponentMap;
import net.minecraft.component.ComponentMapImpl;
import net.minecraft.fluid.FlowableFluid;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.Fluids;
import net.minecraft.registry.Registries;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;

import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.impl.transfer.TransferApiImpl;

public class FluidVariantImpl implements FluidVariant {
	public static FluidVariant of(Fluid fluid, ComponentChanges components) {
		Objects.requireNonNull(fluid, "Fluid may not be null.");
		Objects.requireNonNull(components, "Components may not be null.");

		if (!fluid.isStill(fluid.getDefaultState()) && fluid != Fluids.EMPTY) {
			// Note: the empty fluid is not still, that's why we check for it specifically.

			if (fluid instanceof FlowableFluid flowable) {
				// Normalize FlowableFluids to their still variants.
				fluid = flowable.getStill();
			} else {
				// If not a FlowableFluid, we don't know how to convert -> crash.
				Identifier id = Registries.FLUID.getId(fluid);
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

	public static FluidVariant of(RegistryEntry<Fluid> fluid, ComponentChanges components) {
		return of(fluid.value(), components);
	}

	private final Fluid fluid;
	private final ComponentChanges components;
	private final ComponentMap componentMap;
	private final int hashCode;

	public FluidVariantImpl(Fluid fluid, ComponentChanges components) {
		this.fluid = fluid;
		this.components = components;
		this.componentMap = components == ComponentChanges.EMPTY ? ComponentMap.EMPTY : ComponentMapImpl.create(ComponentMap.EMPTY, components);
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
	public @Nullable ComponentChanges getComponents() {
		return components;
	}

	@Override
	public ComponentMap getComponentMap() {
		return componentMap;
	}

	@Override
	public FluidVariant withComponentChanges(ComponentChanges changes) {
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
