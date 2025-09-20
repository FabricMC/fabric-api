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

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Cancellable;
import com.llamalad7.mixinextras.sugar.Local;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.server.dedicated.MinecraftDedicatedServer;
import net.minecraft.server.dedicated.management.ManagementLogger;
import net.minecraft.server.dedicated.management.RpcException;
import net.minecraft.server.dedicated.management.dispatch.GameRuleRpcDispatcher;
import net.minecraft.server.dedicated.management.handler.GameRuleManagementHandlerImpl;
import net.minecraft.server.dedicated.management.network.ManagementConnectionId;
import net.minecraft.world.GameRules;

import net.fabricmc.fabric.api.gamerule.v1.rule.DoubleRule;
import net.fabricmc.fabric.api.gamerule.v1.rule.EnumRule;
import net.fabricmc.fabric.impl.gamerule.rpc.FabricGameRuleType;
import net.fabricmc.fabric.impl.gamerule.rpc.FabricTypedRule;

@Mixin(GameRuleManagementHandlerImpl.class)
public abstract class GameRuleManagementHandlerImplMixin {
	@Shadow
	@Final
	private MinecraftDedicatedServer server;

	@Shadow
	public abstract GameRuleRpcDispatcher.TypedRule toTypedRule(String name, GameRules.Rule<?> gameRule);

	@Shadow
	@Final
	private ManagementLogger logger;

	@WrapOperation(method = "updateRule", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/GameRules$Rule;serialize()Ljava/lang/String;"))
	private String updateRule(GameRules.Rule<?> rule, Operation<String> original, @Cancellable CallbackInfoReturnable<GameRuleRpcDispatcher.TypedRule> cir,
								@Local(argsOnly = true) GameRuleRpcDispatcher.UntypedRule untypedRule, @Local(argsOnly = true) ManagementConnectionId remote) {
		final String from = original.call(rule);

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

	@Inject(method = "toTypedRule", at = @At("HEAD"), cancellable = true)
	public void toTypedRule(String name, GameRules.Rule<?> rule, CallbackInfoReturnable<GameRuleRpcDispatcher.TypedRule> cir) {
		if (rule instanceof DoubleRule) {
			cir.setReturnValue(FabricTypedRule.create(name, rule.serialize(), FabricGameRuleType.DOUBLE));
		} else if (rule instanceof EnumRule<?>) {
			cir.setReturnValue(FabricTypedRule.create(name, rule.serialize(), FabricGameRuleType.ENUM));
		}
	}

	@Unique
	private GameRuleRpcDispatcher.TypedRule doUpdate(GameRuleRpcDispatcher.UntypedRule untypedRule, ManagementConnectionId remote, GameRules.Rule<?> rule, String from) {
		// 3 lines copied from vanilla:
		GameRuleRpcDispatcher.TypedRule typedRule = this.toTypedRule(untypedRule.key(), rule);
		this.logger.logAction(remote, "Game rule '{}' updated from '{}' to '{}'", typedRule.key(), from, typedRule.value());
		this.server.onGameRuleUpdated(untypedRule.key(), rule);
		return typedRule;
	}
}
