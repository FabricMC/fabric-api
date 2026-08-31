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

package net.fabricmc.fabric.mixin.screen;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.AbstractRecipeBookScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.RecipeBookMenu;

import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;

@Mixin(AbstractRecipeBookScreen.class)
abstract class AbstractRecipeBookScreenMixin<T extends RecipeBookMenu> extends AbstractContainerScreen<T> {
	private AbstractRecipeBookScreenMixin(T menu, Inventory inventory, Component title) {
		super(menu, inventory, title);
	}

	@Inject(method = "extractRenderState",
			at = @At(value = "INVOKE",
					target = "Lnet/minecraft/client/gui/screens/recipebook/RecipeBookComponent;extractRenderState(Lnet/minecraft/client/gui/GuiGraphicsExtractor;IIF)V",
					shift = At.Shift.AFTER))
	public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a, CallbackInfo ci) {
		ScreenEvents.afterForeground((this))
				.invoker()
				.afterForeground(this, graphics, mouseX, mouseY, a);
	}
}
