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

package net.fabricmc.fabric.mixin.gametest;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.gametest.framework.GameTestServer;

@Mixin(GameTestServer.class)
public abstract class GameTestServerMixin {
	@Inject(method = "isDedicatedServer", at = @At("HEAD"), cancellable = true)
	public void isDedicated(CallbackInfoReturnable<Boolean> cir) {
		// Allow dedicated server commands to be registered.
		// Should aid with mods that use this to detect if they are running on a dedicated server as well.
		cir.setReturnValue(true);
	}
}
