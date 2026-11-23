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

package net.fabricmc.fabric.mixin.client.keymapping;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.platform.InputConstants;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.KeyboardHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;

import net.fabricmc.fabric.impl.client.keymapping.KeyMappingExtensions;
import net.fabricmc.fabric.impl.client.keymapping.KeyMappingRegistryImpl;

@Mixin(KeyboardHandler.class)
abstract class KeyboardHandlerMixin {
	@WrapOperation(method = "keyPress", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/KeyMapping;set(Lcom/mojang/blaze3d/platform/InputConstants$Key;Z)V"))
	private void setNativeKeyMappings(InputConstants.Key key, boolean down, Operation<Void> original, @Local(name = "screen") @Nullable Screen screen) {
		if (!down || screen == null) {
			original.call(key, down);
			return;
		}

		// Matches the vanilla logic in KeyMapping#set, but only for key mappings that do not have a context.
		// Context aware keys are handled in KeyMappingClientInitializer via beforeKeyPress
		KeyMapping.forAllKeyMappings(key, mapping -> {
			if (((KeyMappingExtensions) mapping).fabric_getContext() == null) {
				mapping.setDown(true);
			}
		});
	}

	@WrapOperation(method = "keyPress", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/KeyMapping;click(Lcom/mojang/blaze3d/platform/InputConstants$Key;)V"))
	private void clickNativeKeyMappings(InputConstants.Key key, Operation<Void> original, @Local(name = "screen") @Nullable Screen screen) {
		if (screen == null) {
			original.call(key);
			return;
		}

		// Matches the vanilla logic in KeyMapping#click, but only for key mappings that do not have a context.
		// Context aware keys are handled in KeyMappingClientInitializer via beforeMouseClick
		KeyMapping.forAllKeyMappings(key, mapping -> {
			if (((KeyMappingExtensions) mapping).fabric_getContext() == null) {
				mapping.clickCount++;
			}
		});
	}

	@Inject(method = "keyPress", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/platform/FramerateLimitTracker;onInputReceived()V", shift = At.Shift.AFTER))
	private void releaseScreenKeyMappings(long handle, int action, KeyEvent event, CallbackInfo ci) {
		if (action == InputConstants.RELEASE && Minecraft.getInstance().gui.screen() != null) {
			KeyMappingRegistryImpl.onScreenKey(InputConstants.getKey(event), false);
		}
	}
}
