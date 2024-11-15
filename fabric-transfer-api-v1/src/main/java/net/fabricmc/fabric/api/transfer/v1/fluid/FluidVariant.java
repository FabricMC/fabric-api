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

package net.fabricmc.fabric.api.transfer.v1.fluid;

import com.mojang.serialization.Codec;
import org.jetbrains.annotations.ApiStatus;

import net.minecraft.component.ComponentChanges;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.Fluids;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.registry.entry.RegistryEntry;

import net.fabricmc.fabric.api.transfer.v1.storage.TransferVariant;
import net.fabricmc.fabric.impl.transfer.VariantCodecs;
import net.fabricmc.fabric.impl.transfer.fluid.FluidVariantImpl;

/**
 * An immutable association of a still fluid and data components.
 *
 * <p>Do not extend this class. Use {@link #of(Fluid)} and {@link #of(Fluid, ComponentChanges)} to create instances.
 *
 * <p>{@link net.fabricmc.fabric.api.transfer.v1.client.fluid.FluidVariantRendering} can be used for client-side rendering of fluid variants.
 *
 * <p><b>Fluid variants must always be compared with {@code equals}, never by reference!</b>
 * {@code hashCode} is guaranteed to be correct and constant time independently of the size of the components.
 */
@ApiStatus.NonExtendable
public interface FluidVariant extends TransferVariant<Fluid> {
	Codec<FluidVariant> CODEC = VariantCodecs.FLUID_CODEC;
	PacketCodec<RegistryByteBuf, FluidVariant> PACKET_CODEC = VariantCodecs.FLUID_PACKET_CODEC;

	/**
	 * Retrieve a blank FluidVariant.
	 */
	static FluidVariant blank() {
		return of(Fluids.EMPTY);
	}

	/**
	 * Retrieve a FluidVariant with a fluid, and a {@code null} tag.
	 *
	 * <p>The flowing and still variations of {@linkplain net.minecraft.fluid.FlowableFluid flowable fluids}
	 * are normalized to always refer to the still variant. For example,
	 * {@code FluidVariant.of(Fluids.FLOWING_WATER).getFluid() == Fluids.WATER}.
	 */
	static FluidVariant of(Fluid fluid) {
		return of(fluid, ComponentChanges.EMPTY);
	}

	/**
	 * Retrieve a FluidVariant with a fluid, and an optional tag.
	 *
	 * <p>The flowing and still variations of {@linkplain net.minecraft.fluid.FlowableFluid flowable fluids}
	 * are normalized to always refer to the still fluid. For example,
	 * {@code FluidVariant.of(Fluids.FLOWING_WATER, ComponentChanges.EMPTY).getFluid() == Fluids.WATER}.
	 */
	static FluidVariant of(Fluid fluid, ComponentChanges components) {
		return FluidVariantImpl.of(fluid, components);
	}

	/**
	 * Return the fluid of this variant.
	 */
	default Fluid getFluid() {
		return getObject();
	}

	default RegistryEntry<Fluid> getRegistryEntry() {
		return getFluid().getRegistryEntry();
	}

	/**
	 * Creates a copy of this FluidVariant with the provided component changes applied.
	 * @param changes the changes to apply
	 * @return the new variant with the changes applied
	 */
	@Override
	FluidVariant withComponentChanges(ComponentChanges changes);
}
