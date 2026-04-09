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

package net.fabricmc.fabric.mixin.recipe.client.book;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import net.minecraft.client.gui.screens.recipebook.OverlayRecipeComponent;
import net.minecraft.client.gui.screens.recipebook.RecipeBookComponent;
import net.minecraft.client.gui.screens.recipebook.RecipeBookPage;
import net.minecraft.client.gui.screens.recipebook.SlotSelectTime;

import net.fabricmc.fabric.api.client.recipe.v1.book.FabricRecipeBookComponent;

@Mixin(RecipeBookComponent.class)
public class RecipeBookComponentMixin implements FabricRecipeBookComponent {
	@Shadow
	@Final
	private RecipeBookPage recipeBookPage;

	@Unique
	private SlotSelectTime slotSelectTime;

	@ModifyVariable(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/recipebook/GhostSlots;<init>(Lnet/minecraft/client/gui/screens/recipebook/SlotSelectTime;)V"), name = "slotSelectTime")
	private SlotSelectTime setSlotSelectTime(SlotSelectTime slotSelectTime) {
		this.slotSelectTime = slotSelectTime;
		return getSlotSelectTime();
	}

	@Override
	public SlotSelectTime getSlotSelectTime() {
		return this.slotSelectTime;
	}

	@Override
	public void setOverlay(OverlayRecipeComponent overlay) {
		((RecipeBookPageAccessor) recipeBookPage).setOverlay(overlay);
	}
}
