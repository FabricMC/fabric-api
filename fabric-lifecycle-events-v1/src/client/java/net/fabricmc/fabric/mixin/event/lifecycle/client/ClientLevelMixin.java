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
abstract class ClientLevelMixin {
	@Inject(method = "tickEntities", at = @At("HEAD"))
	private void startLevelTick(CallbackInfo ci) {
		// level.tick(Block)Entities is called before level.tick() in Minecraft.tick()
		ClientTickEvents.START_LEVEL_TICK.invoker().onStartTick((ClientLevel) (Object) this);
	}

	@Inject(method = "tick", at = @At("RETURN"))
	public void endLevelTick(CallbackInfo ci) {
		ClientTickEvents.END_LEVEL_TICK.invoker().onEndTick((ClientLevel) (Object) this);
	}
}
