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

package net.fabricmc.fabric.mixin.client.rendering;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.item.ItemStack;

import net.fabricmc.fabric.api.client.rendering.v1.DrawItemStackOverlayCallback;

@Mixin(DrawContext.class)
abstract class DrawContextMixin {
	@Inject(
			method = "drawStackOverlay(Lnet/minecraft/client/font/TextRenderer;Lnet/minecraft/item/ItemStack;IILjava/lang/String;)V",
			at = @At("RETURN")
	)
	public void drawStackOverlay(TextRenderer textRenderer, ItemStack stack, int x, int y, @Nullable String stackCountText, CallbackInfo callback) {
		if (!stack.isEmpty()) {
			DrawItemStackOverlayCallback.EVENT.invoker()
					.onDrawItemStackOverlay((DrawContext) (Object) this, textRenderer, stack, x, y);
		}
	}
}
