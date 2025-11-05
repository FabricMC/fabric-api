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

package net.fabricmc.fabric.mixin.client.gametest.input;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.Window;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.fabricmc.fabric.impl.client.gametest.TestInputImpl;

@Mixin(InputConstants.class)
public class InputConstantsMixin {
	@Inject(method = "isKeyDown", at = @At("HEAD"), cancellable = true)
	private static void useGameTestInputForKeyPressed(Window window, int keyCode, CallbackInfoReturnable<Boolean> cir) {
		cir.setReturnValue(TestInputImpl.isKeyDown(keyCode));
	}

	@Inject(method = {"setupKeyboardCallbacks", "setupMouseCallbacks"}, at = @At("HEAD"), cancellable = true)
	private static void dontAttachCallbacks(CallbackInfo ci) {
		ci.cancel();
	}

	@Inject(method = "grabOrReleaseMouse", at = @At("HEAD"), cancellable = true)
	private static void disableCursorLocking(CallbackInfo ci) {
		ci.cancel();
	}
}
