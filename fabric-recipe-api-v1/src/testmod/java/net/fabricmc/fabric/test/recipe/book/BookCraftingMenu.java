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

package net.fabricmc.fabric.test.recipe.book;

import java.util.List;
import java.util.Optional;

import org.jspecify.annotations.Nullable;

import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket;
import net.minecraft.recipebook.ServerPlaceRecipe;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.StackedItemContents;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.inventory.RecipeBookMenu;
import net.minecraft.world.inventory.RecipeBookType;
import net.minecraft.world.inventory.ResultContainer;
import net.minecraft.world.inventory.ResultSlot;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.inventory.TransientCraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.RecipeHolder;

public class BookCraftingMenu extends RecipeBookMenu {
	private final ContainerLevelAccess access;
	private final Player player;
	private boolean placingRecipe;

	protected final CraftingContainer craftSlots = new TransientCraftingContainer(this, 1, 1);
	protected final ResultContainer resultSlots = new ResultContainer();

	public BookCraftingMenu(final int containerId, final Inventory inventory) {
		this(containerId, inventory, ContainerLevelAccess.NULL);
	}

	public BookCraftingMenu(int containerId, Inventory inventory, ContainerLevelAccess access) {
		super(RecipeBookTestContent.BOOK_CRAFTING_MENU_TYPE, containerId);
		this.access = access;
		this.player = inventory.player;
		this.addSlot(new ResultSlot(player, this.craftSlots, this.resultSlots, 0, 116, 34));
		this.addSlot(new Slot(this.craftSlots, 0, 48, 35));
		this.addStandardInventorySlots(inventory, 8, 84);
	}

	protected static void slotChangedCraftingGrid(final AbstractContainerMenu menu, final ServerLevel level, final Player player, final CraftingContainer container, final ResultContainer resultSlots, final @Nullable RecipeHolder<BookRecipe> recipeHint) {
		CraftingInput input = container.asCraftInput();
		ServerPlayer serverPlayer = (ServerPlayer) player;
		ItemStack result = ItemStack.EMPTY;
		Optional<RecipeHolder<BookRecipe>> maybeRecipe = level.getServer().getRecipeManager().getRecipeFor(RecipeBookTestContent.BOOK_RECIPE_TYPE, input, level, recipeHint);

		if (maybeRecipe.isPresent()) {
			RecipeHolder<BookRecipe> recipeHolder = maybeRecipe.get();
			BookRecipe craftingRecipe = recipeHolder.value();

			if (resultSlots.setRecipeUsed(serverPlayer, recipeHolder)) {
				ItemStack recipeResult = craftingRecipe.assemble(input);

				if (recipeResult.isItemEnabled(level.enabledFeatures())) {
					result = recipeResult;
				}
			}
		}

		resultSlots.setItem(0, result);
		menu.setRemoteSlot(0, result);
		serverPlayer.connection.send(new ClientboundContainerSetSlotPacket(menu.containerId, menu.incrementStateId(), 0, result));
	}

	@Override
	public void slotsChanged(final Container container) {
		if (!this.placingRecipe) {
			this.access.execute((level, pos) -> {
				if (level instanceof ServerLevel serverLevel) {
					slotChangedCraftingGrid(this, serverLevel, this.player, this.craftSlots, this.resultSlots, null);
				}
			});
		}
	}

	protected void beginPlacingRecipe() {
		this.placingRecipe = true;
	}

	public void finishPlacingRecipe(final ServerLevel level, final RecipeHolder<BookRecipe> recipe) {
		this.placingRecipe = false;
		slotChangedCraftingGrid(this, level, this.player, this.craftSlots, this.resultSlots, recipe);
	}

	@Override
	public void removed(final Player player) {
		super.removed(player);
		this.access.execute((level, pos) -> this.clearContainer(player, this.craftSlots));
	}

	@Override
	public boolean stillValid(final Player player) {
		return stillValid(this.access, player, RecipeBookTestContent.BOOK_CRAFTER_BLOCK);
	}

	@Override
	public ItemStack quickMoveStack(final Player player, final int slotIndex) {
		ItemStack clicked = ItemStack.EMPTY;
		Slot slot = this.slots.get(slotIndex);

		if (slot.hasItem()) {
			ItemStack stack = slot.getItem();
			clicked = stack.copy();

			if (slotIndex == 0) {
				stack.getItem().onCraftedBy(stack, player);

				if (!this.moveItemStackTo(stack, 10, 38, true)) {
					return ItemStack.EMPTY;
				}

				slot.onQuickCraft(stack, clicked);
			} else if (slotIndex >= 2 && slotIndex < 37) {
				if (!this.moveItemStackTo(stack, 1, 2, false)) {
					if (slotIndex < 28) {
						if (!this.moveItemStackTo(stack, 28, 37, false)) {
							return ItemStack.EMPTY;
						}
					} else if (!this.moveItemStackTo(stack, 2, 28, false)) {
						return ItemStack.EMPTY;
					}
				}
			} else if (!this.moveItemStackTo(stack, 10, 46, false)) {
				return ItemStack.EMPTY;
			}

			if (stack.isEmpty()) {
				slot.setByPlayer(ItemStack.EMPTY);
			} else {
				slot.setChanged();
			}

			if (stack.getCount() == clicked.getCount()) {
				return ItemStack.EMPTY;
			}

			slot.onTake(player, stack);

			if (slotIndex == 0) {
				player.drop(stack, false);
			}
		}

		return clicked;
	}

	public boolean canTakeItemForPickAll(final ItemStack carried, final Slot target) {
		return target.container != this.resultSlots && super.canTakeItemForPickAll(carried, target);
	}

	@Override
	public void fillCraftSlotsStackedContents(StackedItemContents stackedContents) {
		craftSlots.fillStackedContents(stackedContents);
	}

	@Override
	public RecipeBookMenu.PostPlaceAction handlePlacement(final boolean useMaxItems, final boolean allowDroppingItemsToClear, final RecipeHolder<?> recipe, final ServerLevel level, final Inventory inventory) {
		RecipeHolder<BookRecipe> typedRecipe = (RecipeHolder<BookRecipe>) recipe;
		this.beginPlacingRecipe();

		RecipeBookMenu.PostPlaceAction var8;

		try {
			var8 = ServerPlaceRecipe.placeRecipe(new ServerPlaceRecipe.CraftingMenuAccess<BookRecipe>() {
				public void fillCraftSlotsStackedContents(final StackedItemContents stackedContents) {
					BookCraftingMenu.this.fillCraftSlotsStackedContents(stackedContents);
				}

				public void clearCraftingContent() {
					BookCraftingMenu.this.resultSlots.clearContent();
					BookCraftingMenu.this.craftSlots.clearContent();
				}

				public boolean recipeMatches(final RecipeHolder<BookRecipe> recipe) {
					return recipe.value().matches(BookCraftingMenu.this.craftSlots.asCraftInput(), BookCraftingMenu.this.owner().level());
				}
			}, 1, 1, List.of(getInputSlot()), List.of(getInputSlot()), inventory, typedRecipe, useMaxItems, allowDroppingItemsToClear);
		} finally {
			this.finishPlacingRecipe(level, typedRecipe);
		}

		return var8;
	}

	public Slot getResultSlot() {
		return slots.getFirst();
	}

	public Slot getInputSlot() {
		return slots.get(1);
	}

	@Override
	public RecipeBookType getRecipeBookType() {
		return RecipeBookType.FABRIC_RECIPE_API_V1_TESTMOD_BOOK_CRAFTING;
	}

	protected Player owner() {
		return player;
	}
}
