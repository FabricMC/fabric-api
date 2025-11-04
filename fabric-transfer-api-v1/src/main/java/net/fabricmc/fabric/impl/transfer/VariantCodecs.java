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

package net.fabricmc.fabric.impl.transfer;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.PatchedDataComponentMap;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;

import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.impl.transfer.fluid.FluidVariantImpl;
import net.fabricmc.fabric.impl.transfer.item.ItemVariantImpl;

public class VariantCodecs {
	// AIR is valid (for some reason), don't use ItemStack#ITEM_CODEC
	private static final Codec<ItemVariant> UNVALIDATED_ITEM_CODEC = RecordCodecBuilder.create(instance -> instance.group(
			BuiltInRegistries.ITEM.holderByNameCodec().fieldOf("item").forGetter(ItemVariant::getRegistryEntry),
			DataComponentPatch.CODEC.optionalFieldOf("components", DataComponentPatch.EMPTY).forGetter(ItemVariant::getComponents)
		).apply(instance, ItemVariantImpl::of)
	);
	public static final Codec<ItemVariant> ITEM_CODEC = UNVALIDATED_ITEM_CODEC.validate(VariantCodecs::validateComponents);
	public static final StreamCodec<RegistryFriendlyByteBuf, ItemVariant> ITEM_PACKET_CODEC = StreamCodec.composite(
			ByteBufCodecs.holderRegistry(Registries.ITEM), ItemVariant::getRegistryEntry,
			DataComponentPatch.STREAM_CODEC, ItemVariant::getComponents,
			ItemVariantImpl::of
	);

	public static final Codec<FluidVariant> FLUID_CODEC = RecordCodecBuilder.create(instance -> instance.group(
			BuiltInRegistries.FLUID.holderByNameCodec().fieldOf("fluid").forGetter(FluidVariant::getRegistryEntry),
			DataComponentPatch.CODEC.optionalFieldOf("components", DataComponentPatch.EMPTY).forGetter(FluidVariant::getComponents)
		).apply(instance, FluidVariantImpl::of)
	);
	public static final StreamCodec<RegistryFriendlyByteBuf, FluidVariant> FLUID_PACKET_CODEC = StreamCodec.composite(
			ByteBufCodecs.holderRegistry(Registries.FLUID), FluidVariant::getRegistryEntry,
			DataComponentPatch.STREAM_CODEC, FluidVariant::getComponents,
			FluidVariantImpl::of
	);

	private static DataResult<ItemVariant> validateComponents(ItemVariant variant) {
		return ItemStack.validateComponents(PatchedDataComponentMap.fromPatch(variant.getItem().components(), variant.getComponents())).map(v -> variant);
	}
}
