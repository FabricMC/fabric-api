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

package net.fabricmc.fabric.api.registry.fluid;

import java.util.function.BiPredicate;
import java.util.function.Predicate;

import org.jetbrains.annotations.ApiStatus;

import net.minecraft.tags.TagKey;
import net.minecraft.util.ToFloatFunction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityFluidInteraction;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.phys.Vec3;

import net.fabricmc.fabric.impl.content.registry.fluid.SimpleConfiguredFluidBehavior;

/**
 * Interface for handling common entity fluid interactions.
 */
@ApiStatus.Experimental
public interface FluidBehavior {
	/**
	 * A simple fluid behavior that acts similarly to water.
	 */
	FluidBehavior WATER_LIKE = SimpleConfiguredFluidBehavior.WATER_LIKE;

	/**
	 * Called when fluid pushing should be applied to an entity.
	 *
	 * @param fluid         a tag key representing the fluid type
	 * @param entity        entity that fluid interaction update is processed for
	 * @param interaction   entity's fluid interaction tracker, can be used to query values or apply fluid current
	 * @param canPushEntity controls whatever entity can be pushed
	 */
	void handleFluidInteractionUpdate(TagKey<Fluid> fluid, Entity entity, EntityFluidInteraction interaction, boolean canPushEntity);

	/**
	 * Used to apply fluid movement logic for the entity.
	 * For implementing this method, you should look into how vanilla handles,
	 * movement in fluids at {@link LivingEntity#travelInWater(Vec3, double, boolean, double)}
	 * and {@link LivingEntity#travelInLava(Vec3, double, boolean, double)}.
	 *
	 * @param fluid  a tag key representing the fluid type
	 * @param entity entity that is moving through a fluid
	 * @param input entity's movement input
	 * @param baseGravity entity's gravity
	 * @param isFalling whatever entity is currently falling or not
	 * @param oldY old y position value
	 */
	void travelInFluid(TagKey<Fluid> fluid, LivingEntity entity, Vec3 input, double baseGravity, boolean isFalling, double oldY);

	/**
	 * Used to determine whatever player or entity can sprint-swim in a fluid (like in water).
	 *
	 * @param fluid  a tag key representing the fluid type
	 * @param entity entity that fluid interaction update is processed for
	 */
	default boolean canSwimInFluid(TagKey<Fluid> fluid, Entity entity) {
		return false;
	}

	/**
	 * Used to determine whatever entity should try floating/jumping in fluid (think mobs in water/lava).
	 *
	 * @param fluid  a tag key representing the fluid type
	 * @param entity entity that fluid interaction update is processed for
	 */
	default boolean shouldTryFloatingInFluid(TagKey<Fluid> fluid, Entity entity) {
		return true;
	}

	/**
	 * Checks if player can controllably go down faster by sneaking while in fluid.
	 *
	 * @param fluid  a tag key representing the fluid type
	 * @param entity entity that is moving through a fluid
	 */
	default boolean canMoveDownInFluid(TagKey<Fluid> fluid, Entity entity) {
		return false;
	}

	/**
	 * Checks if entity should drown while submerged in fluid.
	 *
	 * @param fluid  a tag key representing the fluid type
	 * @param entity entity to check against
	 */
	default boolean canDrownInFluid(TagKey<Fluid> fluid, LivingEntity entity) {
		return false;
	}

	/**
	 * Checks if boat-like entity should be able to float on this fluid.
	 *
	 * @param fluid  a tag key representing the fluid type
	 * @param entity entity that is moving through a fluid
	 */
	default boolean canSupportBoat(TagKey<Fluid> fluid, Entity entity) {
		return false;
	}

	/**
	 * Checks if entity should be able to sprint in this fluid.
	 *
	 * @param fluid  a tag key representing the fluid type
	 * @param entity entity that is moving through a fluid
	 */
	default boolean canSprintInFluid(TagKey<Fluid> fluid, LivingEntity entity) {
		return true;
	}

	static Builder simple() {
		return new SimpleConfiguredFluidBehavior.Builder();
	}

	@ApiStatus.NonExtendable
	interface Builder {
		/**
		 * Controls the movement speed multiplier (applied speed when moving).
		 * Defaults to 0.02.
		 *
		 * @param value value to set
		 * @return this builder
		 */
		Builder movementSpeed(float value);

		/**
		 * Controls the movement speed multiplier (applied speed when moving).
		 * Defaults to 0.02.
		 *
		 * @param function source of the multiplier
		 * @return this builder
		 */
		Builder movementSpeed(ToFloatFunction<LivingEntity> function);

		/**
		 * Controls the movement slowdown multiplier (applied to stored speed).
		 * Defaults to 0.65 horizontally and 0.8 vertically.
		 *
		 * @param value value to set
		 * @return this builder
		 */
		Builder movementSlowdown(float value);

		/**
		 * Controls the movement slowdown multiplier (applied to stored speed).
		 * Defaults to 0.65 horizontally and 0.8 vertically.
		 *
		 * @param horizontal horizontal multiplier value to set
		 * @param vertical horizontal multiplier value to set
		 * @return this builder
		 */
		Builder movementSlowdown(float horizontal, float vertical);

		/**
		 * Controls the movement slowdown multiplier (applied to stored speed).
		 * Defaults to 0.65 horizontally and 0.8 vertically.
		 *
		 * @param function source of the multiplier
		 * @return this builder
		 */
		Builder movementSlowdown(ToFloatFunction<LivingEntity> function);

		/**
		 * Controls the movement slowdown multiplier (applied to stored speed).
		 * Defaults to 0.65 horizontally and 0.8 vertically.
		 *
		 * @param function source of the multiplier
		 * @return this builder
		 */
		Builder movementSlowdown(MovementSlowdownFunction function);

		/**
		 * Modifies the applied fall distance when falling or entirely clears it at 0.
		 * Defaults to 0.
		 *
		 * @param value value to set
		 * @return this builder
		 */
		Builder fallDistanceModifier(float value);

		/**
		 * Sets the gravity multiplier.
		 * Defaults to 1 / 16f
		 *
		 * @param value value to set
		 * @return this builder
		 */
		Builder gravityMultiplier(float value);

		/**
		 * Sets the flowing fluid pushing strength.
		 * Defaults to 0.014 (water push strength)
		 *
		 * @param value value to set
		 * @return this builder
		 */
		Builder flowingPushScale(double value);

		/**
		 * Toggles ability to move down faster in fluid when pressing shift.
		 * Defaults to false.
		 *
		 * @param value value to set
		 * @return this builder
		 */
		Builder allowMovingDown(boolean value);

		/**
		 * Allows boats to float on this fluid.
		 * Defaults to false.
		 *
		 * @param value value to set
		 * @return this builder
		 */
		Builder allowBoats(boolean value);

		/**
		 * Allows players to sprint-swim in this fluid.
		 * Defaults to false.
		 *
		 * @param value value to set
		 * @return this builder
		 */
		Builder allowSwimming(boolean value);

		/**
		 * Allows players to sprint in this fluid.
		 * Defaults to true.
		 *
		 * @param value value to set
		 * @return this builder
		 */
		Builder allowSprinting(boolean value);

		/**
		 * Allows players to sprint in this fluid.
		 * Defaults to true.
		 *
		 * @param predicate value to set
		 * @return this builder
		 */
		Builder allowSprinting(Predicate<LivingEntity> predicate);

		/**
		 * Allows players to sprint in this fluid.
		 * Defaults to true.
		 *
		 * @param predicate value to set
		 * @return this builder
		 */
		Builder allowSprinting(BiPredicate<TagKey<Fluid>, LivingEntity> predicate);

		/**
		 * Allows mobs to float in fluid.
		 * Defaults to true.
		 *
		 * @param value value to set
		 * @return this builder
		 */
		Builder makeMobsFloat(boolean value);

		/**
		 * Allows ridden mobs to float in fluid.
		 * Defaults to false.
		 *
		 * @param value value to set
		 * @return this builder
		 */
		Builder makeRiddenMobsFloat(boolean value);

		/**
		 * Allows mobs to drown in fluid.
		 * Defaults to false.
		 *
		 * @param value value to set
		 * @return this builder
		 */
		Builder enableDrowning(boolean value);

		/**
		 * Builds the fluid behavior.
		 *
		 * @return a new fluid behavior
		 */
		FluidBehavior build();

		interface MovementSlowdownFunction {
			Vec3 apply(LivingEntity entity, Vec3 movementDelta, boolean isBelowJumpThreshold, double baseGravity, boolean isFalling);
		}
	}
}
