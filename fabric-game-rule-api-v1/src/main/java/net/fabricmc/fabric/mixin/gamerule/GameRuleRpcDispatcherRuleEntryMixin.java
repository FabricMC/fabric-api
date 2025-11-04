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

import java.util.Objects;
import java.util.function.Function;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.server.dedicated.management.RpcException;
import net.minecraft.server.dedicated.management.dispatch.GameRuleRpcDispatcher;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.world.rule.GameRule;

import net.fabricmc.fabric.impl.gamerule.RuleTypeExtensions;
import net.fabricmc.fabric.impl.gamerule.rpc.FabricGameRuleType;
import net.fabricmc.fabric.impl.gamerule.rpc.FabricTypedRule;

@Mixin(GameRuleRpcDispatcher.RuleEntry.class)
public abstract class GameRuleRpcDispatcherRuleEntryMixin implements FabricTypedRule {
	@Nullable
	@Unique
	private FabricGameRuleType fabricGameRuleType = null;

	@Override
	public @Nullable FabricGameRuleType getFabricType() {
		return fabricGameRuleType;
	}

	@Override
	public void setFabricType(FabricGameRuleType type) {
		this.fabricGameRuleType = Objects.requireNonNull(type);
	}

	@Inject(method = "<init>", at = @At("RETURN"))
	private <T> void updateFabricType(GameRule<T> rule, Object value, CallbackInfo ci) {
		FabricGameRuleType type = ((RuleTypeExtensions) (Object) rule).fabric_getType();

		if (type == null) {
			return;
		}

		this.setFabricType(type);
	}

	@ModifyReturnValue(method = "typedCodec", at = @At("RETURN"))
	private static <T, R extends GameRuleRpcDispatcher.RuleEntry<T>> MapCodec<R> fabricTypeCodec(MapCodec<? extends GameRuleRpcDispatcher.RuleEntry<T>> original, GameRule<T> gameRule) {
		MapCodec<? extends GameRuleRpcDispatcher.RuleEntry<?>> fabricTypedCodec = fabric_createTypedCodec(gameRule);
		//noinspection unchecked
		return (MapCodec<R>) Codec.mapEither(fabricTypedCodec, original).xmap(
				either -> either.map(Function.identity(), Function.identity()),
				typedRule -> ((FabricTypedRule) (Object) typedRule).getFabricType() == null ? (Either) Either.right(typedRule) : (Either) Either.left(typedRule));
	}

	@Unique
	private static <T> GameRuleRpcDispatcher.RuleEntry<T> fabric_checkType(GameRule<T> gameRule, FabricGameRuleType type, T object) {
		FabricGameRuleType gameRuleType = ((RuleTypeExtensions) (Object) gameRule).fabric_getType();

		if (gameRuleType != type) {
			throw new RpcException("Stated type \"" + type + "\" mismatches with actual type \"" + gameRuleType + "\" of gamerule \"" + gameRule.toShortString() + "\"");
		} else {
			return new GameRuleRpcDispatcher.RuleEntry<>(gameRule, object);
		}
	}

	@Unique
	private static <T> MapCodec<? extends GameRuleRpcDispatcher.RuleEntry<T>> fabric_createTypedCodec(GameRule<T> rule) {
		return RecordCodecBuilder.mapCodec((instance) ->
				instance.group(
						StringIdentifiable.createCodec(FabricGameRuleType::values).fieldOf("type").forGetter((arg) -> ((RuleTypeExtensions) (Object) arg.gameRule()).fabric_getType()),
						rule.getCodec().fieldOf("value").forGetter(GameRuleRpcDispatcher.RuleEntry::value)
				).apply(instance, (type, object) -> fabric_checkType(rule, type, object)));
	}
}
