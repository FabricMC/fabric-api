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

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.world.rule.GameRule;

import net.fabricmc.fabric.impl.gamerule.EnumRuleCommand;
import net.fabricmc.fabric.impl.gamerule.RuleTypeExtensions;
import net.fabricmc.fabric.impl.gamerule.rpc.FabricGameRuleType;

@Mixin(targets = "net/minecraft/server/command/GameRuleCommand$1")
public abstract class GameRuleCommandVisitorMixin {
	@Final
	@Shadow
	LiteralArgumentBuilder<ServerCommandSource> field_19419;

	@Inject(at = @At("HEAD"), method = "visit", cancellable = true)
	private <T> void onRegisterCommand(GameRule<T> rule, CallbackInfo ci) {
		// Check if our type is a EnumRuleType
		if (((RuleTypeExtensions) (Object) rule).fabric_getType() == FabricGameRuleType.ENUM) {
			//noinspection rawtypes,unchecked
			EnumRuleCommand.register(this.field_19419, (GameRule<? extends Enum>) rule);
			ci.cancel();
		}
	}
}
