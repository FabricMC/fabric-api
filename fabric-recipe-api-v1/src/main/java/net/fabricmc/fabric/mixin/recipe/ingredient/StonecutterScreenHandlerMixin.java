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

package net.fabricmc.fabric.mixin.recipe.ingredient;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.StonecutterScreenHandler;

@Mixin(StonecutterScreenHandler.class)
public class StonecutterScreenHandlerMixin {
	@Shadow
	@Final
	private ItemStack inputStack;

	/**
	 * Since stonecutting recipes with custom ingredients can be stack-aware, recalculating available recipes
	 * only when the input stack's item changes is not enough. This mixin allows the available recipes to be
	 * recalculated when the input stack changes in any way.
	 *
	 * @see <a href="https://github.com/FabricMC/fabric/issues/4340">Issue #4340</a>
	 */
	@Redirect(
			method = "onContentChanged",
			at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;isOf(Lnet/minecraft/item/Item;)Z")
	)
	private boolean recalculateAvailableRecipesForStackChange(ItemStack stack, Item item) {
		return ItemStack.areEqual(stack, this.inputStack);
	}
}
