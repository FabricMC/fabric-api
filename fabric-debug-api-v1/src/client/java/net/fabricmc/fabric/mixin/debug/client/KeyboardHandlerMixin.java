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

package net.fabricmc.fabric.mixin.debug.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.client.KeyboardHandler;
import net.minecraft.client.input.KeyEvent;

import net.fabricmc.fabric.impl.debug.client.DebugKeyBindingRegistryImpl;

@Mixin(KeyboardHandler.class)
abstract class KeyboardHandlerMixin {
	@Inject(
			method = "handleDebugKeys",
			at = @At("RETURN"),
			cancellable = true
	)
	private void onHandleDebugKeys(
			KeyEvent event,
			CallbackInfoReturnable<Boolean> cir
	) {
		boolean debugAction = cir.getReturnValue();
		cir.setReturnValue(DebugKeyBindingRegistryImpl.invoke(event, debugAction));
	}
}
