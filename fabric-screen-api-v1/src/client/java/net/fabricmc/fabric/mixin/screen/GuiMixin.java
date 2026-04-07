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
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;

import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.impl.client.screen.GuiExtensions;
import net.fabricmc.loader.api.FabricLoader;

@Mixin(Gui.class)
public class GuiMixin implements GuiExtensions {
	@Unique
	private static final Logger LOGGER = LoggerFactory.getLogger("fabric-screen-api-v1");
	@Unique
	private static final boolean DEBUG_SCREEN = FabricLoader.getInstance().isDevelopmentEnvironment() || Boolean.getBoolean("fabric.debugScreen");

	@Shadow
	private @Nullable Screen screen;
	@Shadow
	@Final
	private Minecraft minecraft;

	@Unique
	private Screen tickingScreen;

	@Inject(method = "setScreen", at = @At("HEAD"))
	private void checkThreadOnDev(@Nullable Screen screen, CallbackInfo ci) {
		Thread currentThread = Thread.currentThread();

		if (DEBUG_SCREEN && currentThread != minecraft.getRunningThread()) {
			LOGGER.error("Attempted to set screen to \"{}\" outside the render thread (\"{}\"). This will likely follow a crash! Make sure to call setScreen on the render thread.", screen, currentThread.getName());
		}
	}

	@Inject(method = "setScreen", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/Screen;removed()V", shift = At.Shift.AFTER))
	private void onScreenRemove(@Nullable Screen screen, CallbackInfo ci) {
		ScreenEvents.remove(this.screen).invoker().onRemove(this.screen);
	}

	// These two injections should be caught by the try-catch block if anything fails in an event and then rethrown in the crash report
	@Inject(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/Screen;tick()V"))
	private void beforeScreenTick(CallbackInfo ci) {
		// Store the screen in a variable in case someone tries to change the screen during this before tick event.
		// If someone changes the screen, the after tick event will likely have class cast exceptions or an NPE.
		this.tickingScreen = this.screen;
		ScreenEvents.beforeTick(this.tickingScreen).invoker().beforeTick(this.tickingScreen);
	}

	@Inject(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/Screen;tick()V", shift = At.Shift.AFTER))
	private void afterScreenTick(CallbackInfo ci) {
		ScreenEvents.afterTick(this.tickingScreen).invoker().afterTick(this.tickingScreen);
		// Finally set the currently ticking screen to null
		this.tickingScreen = null;
	}

	@WrapOperation(method = "extractRenderState", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/Screen;extractRenderStateWithTooltipAndSubtitles(Lnet/minecraft/client/gui/GuiGraphicsExtractor;IIF)V"))
	private void onExtractGui(Screen currentScreen, GuiGraphicsExtractor graphics, int mouseX, int mouseY, float tickDelta, Operation<Void> operation) {
		ScreenEvents.beforeExtract(currentScreen).invoker().beforeExtract(currentScreen, graphics, mouseX, mouseY, tickDelta);
		operation.call(currentScreen, graphics, mouseX, mouseY, tickDelta);
		ScreenEvents.afterExtract(currentScreen).invoker().afterExtract(currentScreen, graphics, mouseX, mouseY, tickDelta);
	}

	@Override
	public @Nullable Screen getTickingScreen() {
		return tickingScreen;
	}

	@Override
	public void setTickingScreen(@Nullable Screen screen) {
		this.tickingScreen = screen;
	}
}
