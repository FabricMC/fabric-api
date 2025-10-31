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


import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.server.dedicated.MinecraftDedicatedServer;
import net.minecraft.server.dedicated.management.ManagementLogger;
import net.minecraft.server.dedicated.management.dispatch.GameRuleRpcDispatcher;
import net.minecraft.server.dedicated.management.handler.GameRuleManagementHandlerImpl;
import net.minecraft.world.rule.GameRule;

import net.fabricmc.fabric.impl.gamerule.RuleTypeExtensions;
import net.fabricmc.fabric.impl.gamerule.rpc.FabricGameRuleType;
import net.fabricmc.fabric.impl.gamerule.rpc.FabricTypedRule;

@Mixin(GameRuleManagementHandlerImpl.class)
public abstract class GameRuleManagementHandlerImplMixin {
	@Shadow
	@Final
	private MinecraftDedicatedServer server;

	@Shadow
	@Final
	private ManagementLogger logger;

	@Shadow
	public abstract <T> GameRuleRpcDispatcher.class_12254<T> toTypedRule(GameRule<T> gameRule, T object);

	/*
	@WrapOperation(method = "updateRule", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/rule/GameRules;setValue(Lnet/minecraft/world/rule/GameRule;Ljava/lang/Object;Lnet/minecraft/server/MinecraftServer;)V"))
	private <T> void updateRule(GameRules instance, GameRule<T> rule, T value, @Nullable MinecraftServer server, Operation<Void> original, GameRuleRpcDispatcher.class_12254<T> untypedRule, ManagementConnectionId remote, @Cancellable CallbackInfoReturnable<GameRuleRpcDispatcher.class_12254<T>> cir) {
		final String from = original.call(instance, rule, value, server);

		try {
			if (rule instanceof DoubleRule doubleRule) {
				doubleRule.set(Double.parseDouble(untypedRule.value()), server);
				cir.setReturnValue(doUpdate(untypedRule, remote, rule, from));
			} else if (rule instanceof EnumRule<?> enumRule) {
				enumRule.set(untypedRule.value(), server);
				cir.setReturnValue(doUpdate(untypedRule, remote, rule, from));
			}
		} catch (IllegalArgumentException e) {
			throw new RpcException(e.getMessage());
		}

		return from;
	}
	 */

	@Inject(method = "toTypedRule", at = @At("HEAD"), cancellable = true)
	public <T> void toTypedRule(GameRule<T> gameRule, T object, CallbackInfoReturnable<GameRuleRpcDispatcher.class_12254<T>> cir) {
		FabricGameRuleType type = ((RuleTypeExtensions) (Object) gameRule).fabric_getType();
		if (type != null) {
			cir.setReturnValue(FabricTypedRule.create(gameRule, object, type));
		}
	}

	/*
	@Unique
	private <T> GameRuleRpcDispatcher.class_12254<T> doUpdate(GameRuleRpcDispatcher.UntypedRule untypedRule, ManagementConnectionId remote, GameRules.Rule<?> rule, String from) {
		// 3 lines copied from vanilla:
		GameRuleRpcDispatcher.class_12254<T> typedRule = this.toTypedRule(untypedRule.key(), rule);
		this.logger.logAction(remote, "Game rule '{}' updated from '{}' to '{}'", typedRule.key(), from, typedRule.value());
		this.server.onGameRuleUpdated(untypedRule.key(), rule);
		return typedRule;
	}

	 */
}
