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

package net.fabricmc.fabric.mixin.client.keybinding;

import java.util.List;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.client.option.KeyBinding;
import net.minecraft.util.Identifier;

import net.fabricmc.fabric.impl.client.keybinding.CategoryComparator;

@Mixin(KeyBinding.Category.class)
abstract class KeyBindingCategoryMixin {
	@Shadow
	@Final
	static List<KeyBinding.Category> CATEGORIES;

	@Inject(method = "create(Lnet/minecraft/util/Identifier;)Lnet/minecraft/client/option/KeyBinding$Category;", at = @At("RETURN"))
	private static void onReturnRegister(Identifier id, CallbackInfoReturnable<KeyBinding.Category> cir) {
		CATEGORIES.sort(CategoryComparator.INSTANCE);
	}
}
