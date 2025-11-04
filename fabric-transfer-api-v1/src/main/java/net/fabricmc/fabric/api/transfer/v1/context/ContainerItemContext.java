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

package net.fabricmc.fabric.api.transfer.v1.context;

import java.util.List;

import org.jetbrains.annotations.UnmodifiableView;
import org.jspecify.annotations.Nullable;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;

import net.fabricmc.fabric.api.lookup.v1.item.ItemApiLookup;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.item.PlayerInventoryStorage;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StoragePreconditions;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleSlotStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.fabricmc.fabric.impl.transfer.context.ConstantContainerItemContext;
import net.fabricmc.fabric.impl.transfer.context.CreativeInteractionContainerItemContext;
import net.fabricmc.fabric.impl.transfer.context.PlayerContainerItemContext;
import net.fabricmc.fabric.impl.transfer.context.SingleSlotContainerItemContext;

/**
 * A context that allows an item-queried {@link Storage} implementation to interact with its containing inventory,
 * such as a player inventory or an emptying or filling machine.
 * For example, it is what allows the {@code Storage<FluidVariant>} of a water bucket to replace the full bucket by an empty bucket
 * on extraction.
 * Such items that contain resources are often referred to as "container items".
 *
 * <p>When an {@linkplain ItemApiLookup item API} requires a {@code ContainerItemContext} as context,
 * it will generally be suitable to obtain a context instance with {@link #ofPlayerHand} or {@link #ofPlayerCursor},
 * and then use {@link #find} to query an API instance.
 *
 * <p>When water is extracted from the {@code Storage} of a water bucket, this is how it interacts with the context:
 * <ul>
 *     <li>The first step is to remove one water bucket item from the current slot,
 *     that is the slot that contains the water bucket.</li>
 *     <li>The second step is to try to add one empty bucket item to the current slot, at the same position.</li>
 *     <li>If that fails, the third step is to add the empty bucket item somewhere else in the inventory.</li>
 *     <li>The water extraction can only proceed if both step 1, and step 2 or 3, succeed.</li>
 * </ul>
 * Before attempting to change the current item, the {@code Storage} implementation must of course check that
 * the item in the current slot is still a water bucket.
 *
 * <p>A {@code ContainerItemContext} allows these operations to be performed, thanks to the following parts:
 * <ul>
 *     <li>{@linkplain #getMainSlot The main slot} or current slot of the context, containing the item the API was queried for initially.
 *     In the example above, this is the slot containing the water bucket, used for steps 1 and 2.</li>
 *     <li>{@linkplain #insertOverflow An overflow insertion function}, that can be used to insert items into the context's inventory
 *     when insertion into a specific slot fails. In our example above, this is the function used for step 3.</li>
 *     <li>The context may also contain additional slots, accessible through {@link #getAdditionalSlots}.</li>
 * </ul>
 *
 * <p>Implementors of item APIs can freely use these methods, but most will generally want to use the following convenience methods instead:
 * <ul>
 *     <li>Query which variant is currently in the main slot through {@link #getItemVariant}.
 *     <b>It is important to check this before any operation, to make sure the item variant hasn't changed since the query.</b></li>
 *     <li>Query how much of the (non-blank) variant is in the inventory through {@link #getAmount}.</li>
 *     <li>Extract some items from the main slot with {@link #extract}. In the water bucket example, this can be used for step 1.</li>
 *     <li>Insert some items, either into the main slot if possible or the rest of the inventory otherwise, with {@link #insert}.
 *     In the water bucket example, this can be used for steps 2 and 3.</li>
 *     <li>Exchange some of the current variant with another variant through {@link #exchange}.
 *     In the water bucket example, this function can be used to combine steps 1, 2 and 3.</li>
 * </ul>
 */
public interface ContainerItemContext {
	/**
	 * Returns a context for interaction with a player's hand. This is recommended for item use interactions.
	 *
	 * <p>In creative mode, {@link #forCreativeInteraction} is used with the hand stack.
	 * Otherwise, {@link #ofPlayerHand} is used.
	 * This matches the behavior of {@link ItemUtils#createFilledResult}.
	 */
	static ContainerItemContext forPlayerInteraction(Player player, InteractionHand hand) {
		if (player.hasInfiniteMaterials()) {
			return forCreativeInteraction(player, player.getItemInHand(hand));
		} else {
			return ofPlayerHand(player, hand);
		}
	}

	/**
	 * Returns a context for creative interaction.
	 *
	 * <p>The stack will never be modified, and any updated stack will only be added to the player's inventory
	 * if the player's inventory doesn't already contain it.
	 * This matches the creative behavior of {@link ItemUtils#createFilledResult}.
	 */
	static ContainerItemContext forCreativeInteraction(Player player, ItemStack interactingStack) {
		return new CreativeInteractionContainerItemContext(ItemVariant.of(interactingStack), interactingStack.getCount(), player);
	}

	/**
	 * Return a context for the passed player's hand.
	 */
	static ContainerItemContext ofPlayerHand(Player player, InteractionHand hand) {
		return new PlayerContainerItemContext(player, hand);
	}

	/**
	 * Return a context for the passed player's cursor slot. This is recommended for screen handler click interactions.
	 */
	static ContainerItemContext ofPlayerCursor(Player player, AbstractContainerMenu screenHandler) {
		return ofPlayerSlot(player, PlayerInventoryStorage.getCursorStorage(screenHandler));
	}

	/**
	 * Return a context for a slot, with the passed player as fallback.
	 */
	static ContainerItemContext ofPlayerSlot(Player player, SingleSlotStorage<ItemVariant> slot) {
		return new PlayerContainerItemContext(player, slot);
	}

	/**
	 * Return a context for a single slot, with no fallback.
	 *
	 * @param slot The main slot of the context.
	 */
	static ContainerItemContext ofSingleSlot(SingleSlotStorage<ItemVariant> slot) {
		return new SingleSlotContainerItemContext(slot);
	}

	/**
	 * Return a context that always has some content, and will accept (and destroy) any overflow items.
	 * This can typically be used to check if a stack provides an API, or simulate operations on the returned API,
	 * for example to simulate how much fluid could be extracted from the stack.
	 *
	 * <p>Note that the stack can never be mutated by this function: its contents are copied directly.
	 */
	static ContainerItemContext withConstant(ItemStack constantContent) {
		return withConstant(ItemVariant.of(constantContent), constantContent.getCount());
	}

	/**
	 * Return a context that always has some content, and will accept (and destroy) any overflow items.
	 * This can typically be used to check if a stack provides an API, or simulate operations on the returned API,
	 * for example to simulate how much fluid could be extracted from the variant and amount.
	 */
	static ContainerItemContext withConstant(ItemVariant constantVariant, long constantAmount) {
		StoragePreconditions.notNegative(constantAmount);
		return new ConstantContainerItemContext(constantVariant, constantAmount);
	}

	/**
	 * Try to find an API instance for the passed lookup and return it, or {@code null} if there is none.
	 * The API is queried for the current variant, if it's not blank.
	 *
	 * @see ItemApiLookup#find
	 */
	@Nullable
	default <A> A find(ItemApiLookup<A, ContainerItemContext> lookup) {
		return getItemVariant().isBlank() ? null : lookup.find(getItemVariant().toStack(), this);
	}

	/**
	 * Return the current item variant of this context, that is the variant in the slot of the context.
	 * If the result is not blank, {@link #getAmount} should be positive.
	 */
	default ItemVariant getItemVariant() {
		return getMainSlot().getResource();
	}

	/**
	 * Return the current amount of {@link #getItemVariant()} in the slot of the context.
	 *
	 * @throws IllegalStateException If {@linkplain #getItemVariant() the current variant} is blank.
	 */
	default long getAmount() {
		if (getItemVariant().isBlank()) {
			throw new IllegalStateException("Amount may not be queried when the current item variant is blank.");
		}

		return getMainSlot().getAmount();
	}

	/**
	 * Try to insert some items into this context, prioritizing the main slot over the rest of the inventory.
	 *
	 * @see Storage#insert
	 */
	default long insert(ItemVariant itemVariant, long maxAmount, TransactionContext transaction) {
		// Main slot first
		long mainInserted = getMainSlot().insert(itemVariant, maxAmount, transaction);
		// Overflow second
		long overflowInserted = insertOverflow(itemVariant, maxAmount - mainInserted, transaction);

		return mainInserted + overflowInserted;
	}

	/**
	 * Try to extract some items from this context's main slot.
	 *
	 * @see Storage#extract
	 */
	default long extract(ItemVariant itemVariant, long maxAmount, TransactionContext transaction) {
		return getMainSlot().extract(itemVariant, maxAmount, transaction);
	}

	/**
	 * Try to exchange as many items as possible of {@linkplain #getItemVariant() the current variant} with another variant.
	 * That is, extract the old variant, and insert the same amount of the new variant instead.
	 *
	 * @param newVariant The variant of the items after the conversion. May not be blank.
	 * @param maxAmount The maximum amount of items to convert. May not be negative.
	 * @param transaction The transaction this operation is part of.
	 * @return A non-negative integer not greater than maxAmount: the amount that was transformed.
	 */
	default long exchange(ItemVariant newVariant, long maxAmount, TransactionContext transaction) {
		StoragePreconditions.notBlankNotNegative(newVariant, maxAmount);

		try (Transaction nested = transaction.openNested()) {
			long extracted = extract(getItemVariant(), maxAmount, nested);

			if (insert(newVariant, extracted, nested) == extracted) {
				nested.commit();
				return extracted;
			}
		}

		return 0;
	}

	/**
	 * Return the main slot of this context.
	 */
	SingleSlotStorage<ItemVariant> getMainSlot();

	/**
	 * Try to insert items into this context, without prioritizing a specific slot, similar to {@link Inventory#placeItemBackInInventory}.
	 * This should be used for insertion after insertion into the main slot failed.
	 * {@link #insert} can be used to insert into the main slot first, then send any overflow through this function.
	 *
	 * @see Storage#insert
	 */
	long insertOverflow(ItemVariant itemVariant, long maxAmount, TransactionContext transactionContext);

	/**
	 * Get additional slots that may be available in this context.
	 * These may or may not include the main slot of this context, as it is not always practical to remove it from the list.
	 *
	 * @return An unmodifiable list containing additional slots of this context. If no additional slot is available, the list is empty.
	 */
	@UnmodifiableView
	List<SingleSlotStorage<ItemVariant>> getAdditionalSlots();
}
