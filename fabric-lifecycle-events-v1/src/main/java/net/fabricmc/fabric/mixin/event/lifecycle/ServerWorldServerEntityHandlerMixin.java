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

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;

@Mixin(targets = "net.minecraft.server.level.ServerLevel$EntityCallbacks")
abstract class ServerWorldServerEntityHandlerMixin {
	// final synthetic Lnet/minecraft/server/level/ServerLevel; field_26936
	@Shadow
	@Final
	ServerLevel field_26936;

	@Inject(method = "onTrackingStart(Lnet/minecraft/world/entity/Entity;)V", at = @At("TAIL"))
	private void invokeEntityLoadEvent(Entity entity, CallbackInfo ci) {
		ServerEntityEvents.ENTITY_LOAD.invoker().onLoad(entity, this.field_26936);
	}

	@Inject(method = "onTrackingEnd(Lnet/minecraft/world/entity/Entity;)V", at = @At("HEAD"))
	private void invokeEntityUnloadEvent(Entity entity, CallbackInfo info) {
		ServerEntityEvents.ENTITY_UNLOAD.invoker().onUnload(entity, this.field_26936);
	}
}
