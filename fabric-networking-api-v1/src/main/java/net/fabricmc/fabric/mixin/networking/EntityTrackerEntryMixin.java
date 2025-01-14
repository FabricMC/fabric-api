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

package net.fabricmc.fabric.mixin.networking;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.entity.Entity;
import net.minecraft.server.network.EntityTrackerEntry;
import net.minecraft.server.network.ServerPlayerEntity;

import net.fabricmc.fabric.api.networking.v1.EntityTrackingEvents;

@Mixin(EntityTrackerEntry.class)
abstract class EntityTrackerEntryMixin {
	@Shadow
	@Final
	private Entity entity;

	@Inject(method = "startTracking", at = @At("TAIL"))
	private void onStartTracking(ServerPlayerEntity player, CallbackInfo ci) {
		EntityTrackingEvents.START_TRACKING.invoker().onStartTracking(this.entity, player);
	}

	@Inject(method = "stopTracking", at = @At("HEAD"))
	private void onStopTracking(ServerPlayerEntity player, CallbackInfo ci) {
		EntityTrackingEvents.STOP_TRACKING.invoker().onStopTracking(this.entity, player);
	}
}
