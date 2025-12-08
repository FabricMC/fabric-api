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

package net.fabricmc.fabric.api.entity.event.v1.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;

/**
 * An extension for {@link MobEffect} subclasses adding basic events.
 */
public interface FabricMobEffect {
	/**
	 * Called before an {@linkplain MobEffectInstance instance of this effect} has been added to a {@linkplain LivingEntity living entity}.
	 *
	 * @param effectInstance an instance of this effect
	 * @param entity the entity the effect instance is being applied to
	 */
	default void onEffectAdded(MobEffectInstance effectInstance, LivingEntity entity) {
	}

	/**
	 * Called after an {@linkplain MobEffectInstance instance of this effect} has been added to a {@linkplain LivingEntity living entity}.
	 *
	 * @param effectInstance an instance of this effect
	 * @param entity the entity the effect instance has been applied to
	 */
	default void onEffectStarted(MobEffectInstance effectInstance, LivingEntity entity) {
	}

	/**
	 * Called before an {@linkplain MobEffectInstance instance of this effect} has been removed from a {@linkplain LivingEntity living entity}.
	 *
	 * @param effectInstance an instance of this effect
	 * @param entity the entity the effect instance is being removed from
	 */
	default void onEffectRemoved(MobEffectInstance effectInstance, LivingEntity entity) {
	}
}
