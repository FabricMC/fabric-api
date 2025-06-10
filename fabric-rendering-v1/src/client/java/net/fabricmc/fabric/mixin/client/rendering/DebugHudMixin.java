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

import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.client.gui.hud.DebugHud;

import net.fabricmc.fabric.api.client.rendering.v1.GatherDebugTextEvents;

@Mixin(DebugHud.class)
abstract class DebugHudMixin {
	@Inject(method = "getLeftText", at = @At("RETURN"))
	protected void getLeftText(CallbackInfoReturnable<List<String>> ci) {
		GatherDebugTextEvents.LEFT.invoker().onGatherLeftDebugText(ci.getReturnValue());
	}

	@Inject(method = "getRightText", at = @At("RETURN"))
	protected void getRightText(CallbackInfoReturnable<List<String>> ci) {
		GatherDebugTextEvents.RIGHT.invoker().onGatherRightDebugText(ci.getReturnValue());
	}
}
