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

package net.fabricmc.fabric.mixin.event.lifecycle.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.multiplayer.ClientLevel;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;

@Mixin(ClientLevel.class)
public abstract class ClientLevelMixin {
	@Inject(method = "tickEntities", at = @At("TAIL"))
	public void tickWorldAfterBlockEntities(CallbackInfo ci) {
		ClientTickEvents.END_WORLD_TICK.invoker().onEndTick((ClientLevel) (Object) this);
	}

	@Inject(method = "tickEntities", at = @At("HEAD"))
	private void startWorldTick(CallbackInfo ci) {
		ClientTickEvents.START_WORLD_TICK.invoker().onStartTick((ClientLevel) (Object) this);
	}
}
