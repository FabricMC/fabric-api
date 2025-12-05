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

package net.fabricmc.fabric.mixin.entity.event.effect;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.consume_effects.ClearAllStatusEffectsConsumeEffect;

import net.fabricmc.fabric.api.entity.event.v1.ServerMobEffectEvents;

@Mixin(ClearAllStatusEffectsConsumeEffect.class)
public final class ClearAllStatusEffectsConsumeEffectMixin {
	private ClearAllStatusEffectsConsumeEffectMixin() {
	}

	@WrapOperation(
			method = "apply",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/world/entity/LivingEntity;removeAllEffects()Z"
			)
	)
	private boolean allowEarlyRemoveMobEffect(LivingEntity instance, Operation<Boolean> original) {
		for (MobEffectInstance effectInstance : instance.getActiveEffects()) {
			if (!ServerMobEffectEvents.ALLOW_EARLY_REMOVE.invoker().allowEarlyRemove(effectInstance, instance)) {
				return false;
			}
		}

		return original.call(instance);
	}
}
