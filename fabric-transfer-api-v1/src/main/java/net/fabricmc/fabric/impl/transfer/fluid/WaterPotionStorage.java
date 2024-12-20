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

import org.jetbrains.annotations.Nullable;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.PotionContentsComponent;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.potion.Potions;

import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.StoragePreconditions;
import net.fabricmc.fabric.api.transfer.v1.storage.base.ExtractionOnlyStorage;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleSlotStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;

/**
 * Implementation of the storage for a water potion.
 */
public class WaterPotionStorage implements ExtractionOnlyStorage<FluidVariant>, SingleSlotStorage<FluidVariant> {
	private static final FluidVariant CONTAINED_FLUID = FluidVariant.of(Fluids.WATER);
	private static final long CONTAINED_AMOUNT = FluidConstants.BOTTLE;

	@Nullable
	public static WaterPotionStorage find(ContainerItemContext context) {
		return isWaterPotion(context) ? new WaterPotionStorage(context) : null;
	}

	private static boolean isWaterPotion(ContainerItemContext context) {
		ItemVariant variant = context.getItemVariant();
		PotionContentsComponent potionContents = variant.getComponentMap()
				.getOrDefault(DataComponentTypes.POTION_CONTENTS, PotionContentsComponent.DEFAULT);
		return variant.isOf(Items.POTION) && potionContents.potion().orElse(null) == Potions.WATER;
	}

	private final ContainerItemContext context;

	private WaterPotionStorage(ContainerItemContext context) {
		this.context = context;
	}

	private boolean isWaterPotion() {
		return isWaterPotion(context);
	}

	private ItemVariant mapToGlassBottle() {
		ItemStack newStack = context.getItemVariant().toStack();
		newStack.set(DataComponentTypes.POTION_CONTENTS, PotionContentsComponent.DEFAULT);
		return ItemVariant.of(Items.GLASS_BOTTLE, newStack.getComponentChanges());
	}

	@Override
	public long extract(FluidVariant resource, long maxAmount, TransactionContext transaction) {
		StoragePreconditions.notBlankNotNegative(resource, maxAmount);

		// Not a water potion anymore
		if (!isWaterPotion()) return 0;

		// Make sure that the fluid and the amount match.
		if (resource.equals(CONTAINED_FLUID) && maxAmount >= CONTAINED_AMOUNT) {
			if (context.exchange(mapToGlassBottle(), 1, transaction) == 1) {
				// Conversion ok!
				return CONTAINED_AMOUNT;
			}
		}

		return 0;
	}

	@Override
	public boolean isResourceBlank() {
		return getResource().isBlank();
	}

	@Override
	public FluidVariant getResource() {
		// Only contains a resource if this is still a water potion.
		if (isWaterPotion()) {
			return CONTAINED_FLUID;
		} else {
			return FluidVariant.blank();
		}
	}

	@Override
	public long getAmount() {
		if (isWaterPotion()) {
			return CONTAINED_AMOUNT;
		} else {
			return 0;
		}
	}

	@Override
	public long getCapacity() {
		// Capacity is the same as the amount.
		return getAmount();
	}

	@Override
	public String toString() {
		return "WaterPotionStorage[" + context + "]";
	}
}
