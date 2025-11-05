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

package net.fabricmc.fabric.mixin.event.lifecycle;

import java.util.function.BooleanSupplier;

import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.server.level.ServerLevel;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;

@Mixin(ServerLevel.class)
public abstract class ServerLevelMixin {
	// Make sure "insideBlockTick" is true before we call the start tick, so inject after it is set
	@Inject(method = "tick", at = @At(value = "FIELD", target = "Lnet/minecraft/server/level/ServerLevel;handlingTick:Z", opcode = Opcodes.PUTFIELD, ordinal = 0, shift = At.Shift.AFTER))
	private void startWorldTick(BooleanSupplier shouldKeepTicking, CallbackInfo ci) {
		ServerTickEvents.START_WORLD_TICK.invoker().onStartTick((ServerLevel) (Object) this);
	}

	@Inject(method = "tick", at = @At(value = "TAIL"))
	private void endWorldTick(BooleanSupplier shouldKeepTicking, CallbackInfo ci) {
		ServerTickEvents.END_WORLD_TICK.invoker().onEndTick((ServerLevel) (Object) this);
	}
}
