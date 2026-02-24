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

package net.fabricmc.fabric.impl.transfer.item;

import java.util.Objects;

import org.jspecify.annotations.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.world.level.block.entity.BrewingStandBlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.entity.ShulkerBoxBlockEntity;
import net.minecraft.world.level.block.state.properties.ChestType;

import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.item.base.SingleStackStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.fabricmc.fabric.impl.transfer.DebugMessages;

/**
 * A wrapper around a single slot of an inventory.
 * We must ensure that only one instance of this class exists for every inventory slot,
 * or the transaction logic will not work correctly.
 * This is handled by the Map in InventoryStorageImpl.
 */
class ContainerSlotWrapper extends SingleStackStorage {
	/**
	 * The strong reference to the InventoryStorageImpl ensures that the weak value doesn't get GC'ed when individual slots are still being accessed.
	 */
	private final ContainerStorageImpl storage;
	final int slot;
	private final @Nullable SpecialLogicContainer specialContainer;
	private ItemStack lastReleasedSnapshot = null;

	ContainerSlotWrapper(ContainerStorageImpl storage, int slot) {
		this.storage = storage;
		this.slot = slot;
		this.specialContainer = storage.container instanceof SpecialLogicContainer special ? special : null;
	}

	@Override
	protected ItemStack getStack() {
		return storage.container.getItem(slot);
	}

	@Override
	protected void setStack(ItemStack stack) {
		if (specialContainer == null) {
			storage.container.setItem(slot, stack);
		} else {
			specialContainer.fabric_setSuppress(true);

			try {
				storage.container.setItem(slot, stack);
			} finally {
				specialContainer.fabric_setSuppress(false);
			}
		}
	}

	@Override
	public long insert(ItemVariant insertedVariant, long maxAmount, TransactionContext transaction) {
		if (!canInsert(slot, ((ItemVariantImpl) insertedVariant).getCachedStack())) {
			return 0;
		}

		long ret = super.insert(insertedVariant, maxAmount, transaction);
		if (specialContainer != null && ret > 0) specialContainer.fabric_onTransfer(slot, transaction);
		return ret;
	}

	private boolean canInsert(int slot, ItemStack stack) {
		if (storage.container instanceof ShulkerBoxBlockEntity shulker) {
			// Shulkers override canInsert but not isValid.
			return shulker.canPlaceItemThroughFace(slot, stack, null);
		} else {
			return storage.container.canPlaceItem(slot, stack);
		}
	}

	@Override
	public long extract(ItemVariant variant, long maxAmount, TransactionContext transaction) {
		long ret = super.extract(variant, maxAmount, transaction);
		if (specialContainer != null && ret > 0) specialContainer.fabric_onTransfer(slot, transaction);
		return ret;
	}

	/**
	 * Special cases because vanilla checks the current stack in the following functions (which it shouldn't):
	 * <ul>
	 *     <li>{@link AbstractFurnaceBlockEntity#canPlaceItem(int, ItemStack)}.</li>
	 *     <li>{@link BrewingStandBlockEntity#canPlaceItem(int, ItemStack)}.</li>
	 * </ul>
	 */
	@Override
	public int getCapacity(ItemVariant variant) {
		// Special case to limit buckets to 1 in furnace fuel inputs.
		if (storage.container instanceof AbstractFurnaceBlockEntity && slot == 1 && variant.isOf(Items.BUCKET)) {
			return 1;
		}

		// Special case to limit brewing stand "bottle inputs" to 1.
		if (storage.container instanceof BrewingStandBlockEntity && slot < 3) {
			return 1;
		}

		return Math.min(storage.container.getMaxStackSize(), ItemVariantImpl.getMaxStackSize(variant));
	}

	// We override updateSnapshots to also schedule a setChanged call for the backing inventory.
	@Override
	public void updateSnapshots(TransactionContext transaction) {
		storage.setChangedParticipant.updateSnapshots(transaction);
		super.updateSnapshots(transaction);

		// For chests: also schedule a setChanged call for the other half
		if (storage.container instanceof ChestBlockEntity chest && chest.getBlockState().getValue(ChestBlock.TYPE) != ChestType.SINGLE) {
			BlockPos otherChestPos = chest.getBlockPos().relative(ChestBlock.getConnectedDirection(chest.getBlockState()));

			if (chest.getLevel().getBlockEntity(otherChestPos) instanceof ChestBlockEntity otherChest) {
				((ContainerStorageImpl) ContainerStorageImpl.of(otherChest, null)).setChangedParticipant.updateSnapshots(transaction);
			}
		}
	}

	@Override
	protected void releaseSnapshot(ItemStack snapshot) {
		lastReleasedSnapshot = snapshot;
	}

	@Override
	protected void onFinalCommit() {
		// Try to apply the change to the original stack
		ItemStack original = lastReleasedSnapshot;
		ItemStack currentStack = getStack();

		if (storage.container instanceof SpecialLogicContainer specialLogicInv) {
			specialLogicInv.fabric_onFinalCommit(slot, original, currentStack);
		}

		if (!original.isEmpty() && original.getItem() == currentStack.getItem()) {
			// Components have changed, we need to copy the stack.
			if (!Objects.equals(original.getComponentsPatch(), currentStack.getComponentsPatch())) {
				// Remove all the existing components and copy the new ones on top.
				for (DataComponentType<?> type : original.getComponents().keySet()) {
					original.set(type, null);
				}

				original.applyComponents(currentStack.getComponents());
			}

			// None is empty and the items and components match: just update the amount, and reuse the original stack.
			original.setCount(currentStack.getCount());
			setStack(original);
		} else {
			// Otherwise assume everything was taken from original so empty it.
			original.setCount(0);
		}
	}

	@Override
	public String toString() {
		return "ContainerSlotWrapper[%s#%d]".formatted(DebugMessages.forInventory(storage.container), slot);
	}
}
