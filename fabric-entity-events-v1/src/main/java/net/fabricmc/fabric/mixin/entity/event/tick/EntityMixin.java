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

package net.fabricmc.fabric.mixin.entity.event.tick;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.world.entity.Entity;

import net.fabricmc.fabric.api.entity.event.v1.tick.EntityTickEvents;

@Mixin(Entity.class)
abstract class EntityMixin {
	@Inject(method = "tick", at = @At("HEAD"))
	private void startTick(CallbackInfo ci) {
		EntityTickEvents.START_TICK.invoker().startTick((Entity) (Object) this);
	}

	@Inject(method = "tick", at = @At("TAIL"))
	private void endTick(CallbackInfo ci) {
		EntityTickEvents.END_TICK.invoker().endTick((Entity) (Object) this);
	}
}
