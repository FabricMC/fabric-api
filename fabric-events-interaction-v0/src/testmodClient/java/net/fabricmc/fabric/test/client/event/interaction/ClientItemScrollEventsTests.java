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

package net.fabricmc.fabric.test.client.event.interaction;

import java.util.Objects;

import net.minecraft.client.gui.screens.worldselection.WorldCreationUiState;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.fabricmc.fabric.api.event.client.player.ClientItemScrollEvents;

public class ClientItemScrollEventsTests implements FabricClientGameTest {
	@Override
	public void runTest(ClientGameTestContext context) {
		try (
				TestSingleplayerContext _ = context.worldBuilder()
						.adjustSettings(creator ->
								creator.setGameMode(WorldCreationUiState.SelectedGameMode.CREATIVE))
						.create()) {
			var ctx = new Object() {
				int selectedSlot = 36;
				boolean inScope = true; // scoped events at home
				boolean before = false;
				boolean after = false;
				boolean allowDone = false;
			};
			context.runOnClient((minecraft) -> {
				// player blaze powder testing
				LocalPlayer player = Objects.requireNonNull(minecraft.player);
				Inventory playerInventory = player.getInventory();
				int selectedSlot1 = playerInventory.getSelectedSlot();
				ctx.selectedSlot = selectedSlot1;
				playerInventory.setItem(selectedSlot1, new ItemStack(Items.BLAZE_POWDER));
				ClientItemScrollEvents.ALLOW.register((inventory, currentSlot, _, _, _) -> {
					if (!ctx.inScope) {
						return true;
					}

					boolean allow = inventory.getItem(currentSlot).is(Items.BLAZE_POWDER);

					if (!allow) {
						ctx.allowDone = true;
					}

					return allow;
				});
				ClientItemScrollEvents.BEFORE.register(((inventory, _, newSlot, _, _) -> {
					if (!ctx.inScope) {
						return;
					}

					if (ctx.before) {
						throw new IllegalStateException("client item scroll before invoked twice");
					}

					if (ctx.after) {
						throw new IllegalStateException("client item scroll after invoked before before event");
					}

					if (inventory.getItem(newSlot).is(Items.BLAZE_POWDER)) {
						throw new IllegalStateException("client item scroll before invoked on canceled item scroll event");
					}

					ctx.before = true;
				}));
				ClientItemScrollEvents.AFTER.register(((inventory, _, newSlot, _, _) -> {
					if (!ctx.inScope) {
						return;
					}

					if (ctx.after) {
						throw new IllegalStateException("client item scroll after invoked twice");
					}

					if (!ctx.before) {
						throw new IllegalStateException("client item scroll after invoked before before event");
					}

					if (inventory.getItem(newSlot).is(Items.BLAZE_POWDER)) {
						throw new IllegalStateException("client item scroll after invoked on canceled item scroll event");
					}

					ctx.after = true;
				}));
			});
			context.getInput().scroll(-1.0);
			context.waitFor(mc ->
					Objects.requireNonNull(mc.player)
							.getInventory()
							.getSelectedSlot() == ctx.selectedSlot + 1);

			if (!ctx.before || !ctx.after) {
				throw new IllegalStateException("before/after client item scroll events never fired");
			}

			context.getInput().scroll(-1.0);
			context.waitFor(_ -> ctx.allowDone);
			ctx.inScope = false;
		}
	}
}
