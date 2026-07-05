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

import net.minecraft.server.commands.GameRuleCommand;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.level.gamerules.GameRule;

import net.fabricmc.fabric.impl.gamerule.EnumRuleCommand;
import net.fabricmc.fabric.impl.gamerule.RuleTypeExtensions;
import net.fabricmc.fabric.impl.gamerule.rpc.FabricGameRuleType;

@Mixin(GameRuleCommand.class)
public class GameRuleCommandMixin {
	@WrapOperation(method = "lambda$queryRule$0", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/gamerules/GameRule;serialize(Ljava/lang/Object;)Ljava/lang/String;"))
	private static <T> String displayProperEnumName(GameRule<T> instance, T value, Operation<String> original) {
		if (((RuleTypeExtensions) (Object) instance).fabric_getType() == FabricGameRuleType.ENUM) {
			return EnumRuleCommand.getLiteralForRuleValue((Enum<?>) value);
		} else {
			return original.call(instance, value);
		}
	}
}
