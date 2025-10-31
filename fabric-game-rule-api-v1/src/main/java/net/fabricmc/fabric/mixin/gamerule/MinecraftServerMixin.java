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

package net.fabricmc.fabric.mixin.gamerule;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import org.spongepowered.asm.mixin.Mixin;

import net.minecraft.server.MinecraftServer;
import net.minecraft.world.rule.GameRule;

import net.fabricmc.fabric.api.gamerule.v1.GameRuleEvents;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleRegistry;

@Mixin(MinecraftServer.class)
public abstract class MinecraftServerMixin {
	@WrapMethod(method = "onGameRuleUpdated")
	private <T> void invokeChangeCallbacks(GameRule<T> rule, T object, Operation<Void> original) {
		GameRuleRegistry.LOGGER.info("updated");
		GameRuleEvents.CHANGED_CALLBACK.invoker().accept(rule, object, (MinecraftServer) (Object) this);
		original.call(rule, object);
	}
}
