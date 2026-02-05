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

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import net.minecraft.util.debug.ServerDebugSubscribers;

import net.fabricmc.loader.api.FabricLoader;

@Mixin(ServerDebugSubscribers.class)
abstract class ServerDebugSubscribersMixin {
	@Definition(
			id = "IS_RUNNING_IN_IDE",
			field = "Lnet/minecraft/SharedConstants;IS_RUNNING_IN_IDE:Z"
	)
	@Expression("IS_RUNNING_IN_IDE")
	@WrapOperation(
			method = "hasRequiredPermissions",
			at = @At("MIXINEXTRAS:EXPRESSION")
	)
	private boolean requireInIde(Operation<Boolean> original) {
		return original.call() || FabricLoader.getInstance()
				.isDevelopmentEnvironment();
	}
}
