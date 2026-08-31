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

package net.fabricmc.fabric.mixin.dimension;

import java.util.Optional;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.mojang.datafixers.Products;
import com.mojang.datafixers.kinds.App;
import com.mojang.serialization.Lifecycle;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.core.RegistrationInfo;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.WorldDimensions;

import net.fabricmc.fabric.impl.dimension.FailSoftMapCodec;

@Mixin(WorldDimensions.class)
public class WorldDimensionsMixin {
	@Unique
	private static final ScopedValue<Registry<LevelStem>> REGISTRY = ScopedValue.newInstance();

	/**
	 * Fix the issue that cannot load world after uninstalling a dimension mod/datapack.
	 * After uninstalling a dimension mod/datapack, the dimension config in `level.dat` file cannot be deserialized.
	 * The solution is to make it fail-soft.
	 */
	@Redirect(method = "lambda$static$0", at = @At(value = "INVOKE", target = "Lcom/mojang/serialization/codecs/RecordCodecBuilder$Instance;group(Lcom/mojang/datafixers/kinds/App;)Lcom/mojang/datafixers/Products$P1;"))
	private static Products.P1 useFailSoftMap(RecordCodecBuilder.Instance instance, App app) {
		return instance.group(
				new FailSoftMapCodec<>(ResourceKey.codec(Registries.LEVEL_STEM), LevelStem.CODEC)
						.fieldOf("dimensions").forGetter(WorldDimensions::dimensions)
		);
	}

	@WrapMethod(method = "bake")
	private WorldDimensions.Complete wrapBakeToProvideContext(Registry<LevelStem> baseDimensions, Operation<WorldDimensions.Complete> original) {
		return ScopedValue.where(REGISTRY, baseDimensions).call(() -> original.call(baseDimensions));
	}

	/**
	 * Make all modded dimensions that are loaded from mod-provided resources use their defined lifecycle,
	 * rather than always defaulting to experimental. This will hide the experimental message when creating/joining
	 * a world.
	 * This does not affect regular datapack provided changes or if mod overrides vanilla dimension!
	 */
	@Inject(method = "checkStability", at = @At("HEAD"), cancellable = true)
	private static void betterModdedStabilityCheck(ResourceKey<LevelStem> key, LevelStem dimension, CallbackInfoReturnable<Lifecycle> cir) {
		if (key.identifier().getNamespace().equals(Identifier.DEFAULT_NAMESPACE) || !REGISTRY.isBound()) {
			return;
		}

		Optional<RegistrationInfo> registrationInfo = REGISTRY.get().registrationInfo(key);

		if (registrationInfo.isEmpty() || registrationInfo.get().knownPackInfo().isEmpty()) {
			return;
		}

		cir.setReturnValue(registrationInfo.get().lifecycle());
	}
}
