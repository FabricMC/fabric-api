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
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalBooleanRef;
import com.mojang.blaze3d.platform.InputConstants;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import net.minecraft.client.input.MouseButtonInfo;

import net.fabricmc.fabric.impl.client.keymapping.KeyMappingExtensions;
import net.fabricmc.fabric.impl.client.keymapping.KeyMappingRegistryImpl;

@Mixin(MouseHandler.class)
abstract class MouseHandlerMixin {
	@Inject(method = "onButton", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/platform/FramerateLimitTracker;onInputReceived()V", shift = At.Shift.AFTER))
	private void trackScreenInput(long handle, MouseButtonInfo rawButtonInfo, int action, CallbackInfo ci, @Share("screenInput") LocalBooleanRef screenInput) {
		screenInput.set(Minecraft.getInstance().gui.screen() != null);

		if (action == InputConstants.RELEASE && screenInput.get()) {
			KeyMappingRegistryImpl.onScreenKey(InputConstants.Type.MOUSE.getOrCreate(rawButtonInfo.button()), false);
		}
	}

	@WrapOperation(method = "onButton", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/KeyMapping;set(Lcom/mojang/blaze3d/platform/InputConstants$Key;Z)V"))
	private void setNativeKeyMappings(InputConstants.Key key, boolean state, Operation<Void> original, @Share("screenInput") LocalBooleanRef screenInput) {
		if (!state || !screenInput.get()) {
			original.call(key, state);
			return;
		}

		KeyMapping.forAllKeyMappings(key, mapping -> {
			if (((KeyMappingExtensions) mapping).fabric_getContext() == null) {
				mapping.setDown(true);
			}
		});
	}

	@WrapOperation(method = "onButton", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/KeyMapping;click(Lcom/mojang/blaze3d/platform/InputConstants$Key;)V"))
	private void clickNativeKeyMappings(InputConstants.Key key, Operation<Void> original, @Share("screenInput") LocalBooleanRef screenInput) {
		if (!screenInput.get()) {
			original.call(key);
			return;
		}

		KeyMapping.forAllKeyMappings(key, mapping -> {
			if (((KeyMappingExtensions) mapping).fabric_getContext() == null) {
				mapping.clickCount++;
			}
		});
	}
}
