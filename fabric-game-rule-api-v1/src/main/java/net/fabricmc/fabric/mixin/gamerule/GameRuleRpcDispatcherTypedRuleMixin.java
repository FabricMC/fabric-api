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

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import net.minecraft.server.dedicated.management.dispatch.GameRuleRpcDispatcher;
import net.minecraft.util.StringIdentifiable;

import net.fabricmc.fabric.impl.gamerule.rpc.FabricGameRuleType;
import net.fabricmc.fabric.impl.gamerule.rpc.FabricTypedRule;

@Mixin(GameRuleRpcDispatcher.TypedRule.class)
public class GameRuleRpcDispatcherTypedRuleMixin implements FabricTypedRule {
	@Shadow
	@Final
	@Mutable
	public static MapCodec<GameRuleRpcDispatcher.TypedRule> CODEC;

	static {
		MapCodec<GameRuleRpcDispatcher.TypedRule> fabricTypedCodec = RecordCodecBuilder.mapCodec((instance) ->
				instance.group(
						Codec.STRING.fieldOf("key").forGetter(GameRuleRpcDispatcher.TypedRule::key),
						Codec.STRING.fieldOf("value").forGetter(GameRuleRpcDispatcher.TypedRule::value),
						StringIdentifiable.createCodec(FabricGameRuleType::values).fieldOf("type")
								.forGetter(typedRule -> ((FabricTypedRule) (Object) typedRule).getFabricType())
				).apply(instance, FabricTypedRule::create));

		CODEC = Codec.mapEither(fabricTypedCodec, CODEC).xmap(
				either -> either.map(Function.identity(), Function.identity()),
				typedRule -> ((FabricTypedRule) (Object) typedRule).getFabricType() == null ? Either.right(typedRule) : Either.left(typedRule));
	}

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
}
