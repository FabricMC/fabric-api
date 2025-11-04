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

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;

import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;

@Mixin(GameRenderer.class)
abstract class GameRendererMixin {
	@WrapOperation(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/Screen;renderWithTooltipAndSubtitles(Lnet/minecraft/client/gui/GuiGraphics;IIF)V"))
	private void onRenderScreen(Screen currentScreen, GuiGraphics drawContext, int mouseX, int mouseY, float tickDelta, Operation<Void> operation) {
		ScreenEvents.beforeRender(currentScreen).invoker().beforeRender(currentScreen, drawContext, mouseX, mouseY, tickDelta);
		operation.call(currentScreen, drawContext, mouseX, mouseY, tickDelta);
		ScreenEvents.afterRender(currentScreen).invoker().afterRender(currentScreen, drawContext, mouseX, mouseY, tickDelta);
	}
}
