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

package net.fabricmc.fabric.api.transfer.v1.item;

import java.util.Objects;

import com.mojang.serialization.Codec;
import org.jetbrains.annotations.ApiStatus;

import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;

import net.fabricmc.fabric.api.transfer.v1.storage.TransferVariant;
import net.fabricmc.fabric.impl.transfer.VariantCodecs;
import net.fabricmc.fabric.impl.transfer.item.ItemVariantImpl;

/**
 * An immutable count-less ItemStack, i.e. an immutable association of an item and its data components.
 *
 * <p>Do not implement, use the static {@code of(...)} functions instead.
 */
@ApiStatus.NonExtendable
public interface ItemVariant extends TransferVariant<Item> {
	Codec<ItemVariant> CODEC = VariantCodecs.ITEM_CODEC;
	StreamCodec<RegistryFriendlyByteBuf, ItemVariant> PACKET_CODEC = VariantCodecs.ITEM_PACKET_CODEC;

	/**
	 * Retrieve a blank ItemVariant.
	 */
	static ItemVariant blank() {
		return of(Items.AIR);
	}

	/**
	 * Retrieve an ItemVariant with the item and tag of a stack.
	 */
	static ItemVariant of(ItemStack stack) {
		return of(stack.getItem(), stack.getComponentsPatch());
	}

	/**
	 * Retrieve an ItemVariant with an item and without a tag.
	 */
	static ItemVariant of(ItemLike item) {
		return of(item, DataComponentPatch.EMPTY);
	}

	/**
	 * Retrieve an ItemVariant with an item and an optional tag.
	 */
	static ItemVariant of(ItemLike item, DataComponentPatch components) {
		return ItemVariantImpl.of(item.asItem(), components);
	}

	/**
	 * Return true if the item and tag of this variant match those of the passed stack, and false otherwise.
	 */
	default boolean matches(ItemStack stack) {
		return isOf(stack.getItem()) && Objects.equals(stack.getComponentsPatch(), getComponents());
	}

	/**
	 * Return the item of this variant.
	 */
	default Item getItem() {
		return getObject();
	}

	default Holder<Item> getRegistryEntry() {
		return getItem().builtInRegistryHolder();
	}

	/**
	 * Create a new item stack with count 1 from this variant.
	 */
	default ItemStack toStack() {
		return toStack(1);
	}

	/**
	 * Create a new item stack from this variant.
	 *
	 * @param count The count of the returned stack. It may lead to counts higher than maximum stack size.
	 */
	default ItemStack toStack(int count) {
		if (isBlank()) return ItemStack.EMPTY;
		return new ItemStack(getRegistryEntry(), count, getComponents());
	}

	/**
	 * Creates a copy of this ItemVariant with the provided component changes applied.
	 * @param changes the changes to apply
	 * @return the new variant with the changes applied
	 *
	 * @see ItemStack#applyComponents(DataComponentPatch)
	 */
	@Override
	ItemVariant withComponentChanges(DataComponentPatch changes);
}
