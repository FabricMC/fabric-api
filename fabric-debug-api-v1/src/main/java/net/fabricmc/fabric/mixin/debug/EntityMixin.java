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

package net.fabricmc.fabric.mixin.debug;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.debug.DebugValueSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;

import net.fabricmc.fabric.impl.debug.EntityDebugSubscriptionRegistryImpl;

/// The Mob class does not super-call
/// [net.minecraft.world.entity.Entity#registerDebugValues], so we have to Mixin it as well.
@Mixin(value = {Entity.class, Mob.class})
abstract class EntityMixin {
	@Inject(
			method = "registerDebugValues",
			at = @At("HEAD")
	)
	private void addDebugValues(
			ServerLevel level,
			DebugValueSource.Registration registration,
			CallbackInfo ci
	) {
		EntityDebugSubscriptionRegistryImpl.addDebugValues(this, registration);
	}
}
