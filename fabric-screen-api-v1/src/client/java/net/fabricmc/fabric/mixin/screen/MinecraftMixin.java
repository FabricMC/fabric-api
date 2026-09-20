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
import com.llamalad7.mixinextras.sugar.Local;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.screens.LevelLoadingScreen;
import net.minecraft.client.gui.screens.Screen;

import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;

@Mixin(Minecraft.class)
abstract class MinecraftMixin {
	@Shadow
	@Final
	public Gui gui;

	@WrapOperation(method = "exitWorldAndClose", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/Screen;removed()V"))
	private void onScreenRemoveBecauseStopping(Screen screen, Operation<Void> original) {
		original.call(screen);
		ScreenEvents.remove(screen).invoker().onRemove(screen);
	}

	// The LevelLoadingScreen is the odd screen that isn't ticked by the main tick loop, so we fire events for this screen.
	// We Coerce the package-private inner class representing the world load action so we don't need an access widener.
	@Inject(method = "doWorldLoad", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/LevelLoadingScreen;tick()V"))
	private void beforeLoadingScreenTick(CallbackInfo ci, @Local(name = "screen") LevelLoadingScreen screen) {
		// Use the local variable screen in case someone tries to change the screen during this before tick event.
		// If someone changes the screen, the after tick event will likely have class cast exceptions or throw a NPE.
		ScreenEvents.beforeTick(screen).invoker().beforeTick(screen);
	}

	@Inject(method = "doWorldLoad", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;renderFrame(Z)V"))
	private void afterLoadingScreenTick(CallbackInfo ci, @Local(name = "screen") LevelLoadingScreen screen) {
		ScreenEvents.afterTick(screen).invoker().afterTick(screen);
	}
}
