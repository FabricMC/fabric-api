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

import com.mojang.blaze3d.platform.SDLEventHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SDLEventHandler.class)
public class SDLEventHandlerMixin {
	@Inject(method = {
			"handleKeyEvent",
			"handleTextEditingEvent",
			"handleTextInputEvent",
			"handleMouseMotionEvent",
			"handleMouseButtonEvent",
			"handleMouseWheelEvent",
			"handleDropFileEvent",
			"handleDropBeginEvent",
			"handleDropCompleteEvent"
	}, at = @At("HEAD"), cancellable = true)
	private void disableRealInput(CallbackInfo ci) {
		ci.cancel();
	}
}
