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

package net.fabricmc.fabric.impl.content.registry.fluid;

import java.util.function.BiPredicate;
import java.util.function.Predicate;

import net.minecraft.tags.EntityTypeTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.ToFloatFunction;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityFluidInteraction;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.phys.Vec3;

import net.fabricmc.fabric.api.registry.fluid.FluidBehavior;
import net.fabricmc.fabric.mixin.content.registry.fluid.LivingEntityAccessor;

public record SimpleConfiguredFluidBehavior(ToFloatFunction<LivingEntity> movementSpeed,
											FluidBehavior.Builder.MovementSlowdownFunction movementSlowdown,
											float gravityMultiplier, float fallDistanceMultiplier,
											double flowingPushScale,
											boolean allowMovingDown, boolean allowBoats,
											boolean allowSwimming, boolean makeMobsFloat,
											boolean makeRiddenMobsFloat, boolean drowning,
											BiPredicate<TagKey<Fluid>, LivingEntity> allowSprinting) implements FluidBehavior {
	public static final FluidBehavior WATER_LIKE = new Builder()
			.movementSpeed(entity -> {
				float speed = 0.02F;
				float waterWalker = (float) entity.getAttributeValue(Attributes.WATER_MOVEMENT_EFFICIENCY);

				if (!entity.onGround()) {
					waterWalker *= 0.5F;
				}

				if (waterWalker > 0.0F) {
					speed += (entity.getSpeed() - speed) * waterWalker;
				}

				return speed;
			}).movementSlowdown((entity, movement, _, baseGravity, isFalling) -> {
				float slowDown = entity.isSprinting() ? 0.9F : ((LivingEntityAccessor) entity).callGetWaterSlowDown();
				float waterWalker = (float) entity.getAttributeValue(Attributes.WATER_MOVEMENT_EFFICIENCY);

				if (!entity.onGround()) {
					waterWalker *= 0.5F;
				}

				if (waterWalker > 0.0F) {
					slowDown += (0.54600006F - slowDown) * waterWalker;
				}

				if (entity.hasEffect(MobEffects.DOLPHINS_GRACE)) {
					slowDown = 0.96F;
				}

				if (entity.horizontalCollision && entity.onClimbable()) {
					movement = new Vec3(movement.x, 0.2, movement.z);
				}

				movement = movement.multiply(slowDown, 0.8F, slowDown);
				// This also applies the gravity multiplier
				return entity.getFluidFallingAdjustedMovement(baseGravity, isFalling, movement);
			}).fallDistanceModifier(0).flowingPushScale(0.014).gravityMultiplier(0).makeMobsFloat(true)
			.makeRiddenMobsFloat(true).enableDrowning(true).allowSwimming(true).allowMovingDown(true)
			.allowBoats(true).allowSprinting((fluid, entity) -> entity.isEyeInFluid(fluid)).build();

	@Override
	public void handleFluidInteractionUpdate(TagKey<Fluid> fluid, Entity entity, EntityFluidInteraction interaction, boolean canPushEntity) {
		if (canPushEntity) {
			interaction.applyCurrentTo(fluid, entity, this.flowingPushScale);
		}

		entity.fallDistance *= this.fallDistanceMultiplier;
	}

	@Override
	public boolean canSwimInFluid(TagKey<Fluid> fluid, Entity entity) {
		return this.allowSwimming;
	}

	@Override
	public boolean shouldTryFloatingInFluid(TagKey<Fluid> fluid, Entity entity) {
		return this.makeMobsFloat;
	}

	@Override
	public void travelInFluid(TagKey<Fluid> fluid, LivingEntity entity, Vec3 input, double baseGravity, boolean isFalling, double oldY) {
		float speed = this.movementSpeed.applyAsFloat(entity);
		entity.moveRelative(speed, input);
		entity.move(MoverType.SELF, entity.getDeltaMovement());

		entity.setDeltaMovement(this.movementSlowdown.apply(entity, entity.getDeltaMovement(), entity.getFluidHeight(fluid) <= entity.getFluidJumpThreshold(), baseGravity, isFalling));

		if (baseGravity != 0.0F && this.gravityMultiplier != 0.0F) {
			entity.setDeltaMovement(entity.getDeltaMovement().add(0.0F, -baseGravity * this.gravityMultiplier, 0.0F));
		}

		((LivingEntityAccessor) entity).callJumpOutOfFluid(oldY);

		if (this.makeRiddenMobsFloat) {
			boolean canEntityFloatInWater = entity.is(EntityTypeTags.CAN_FLOAT_WHILE_RIDDEN);

			if (canEntityFloatInWater && entity.isVehicle() && entity.getFluidHeight(fluid) > entity.getFluidJumpThreshold()) {
				entity.setDeltaMovement(entity.getDeltaMovement().add(0.0F, 0.04F, 0.0F));
			}
		}
	}

	@Override
	public boolean canMoveDownInFluid(TagKey<Fluid> fluid, Entity entity) {
		return this.allowMovingDown;
	}

	@Override
	public boolean canDrownInFluid(TagKey<Fluid> fluid, LivingEntity entity) {
		return this.drowning;
	}

	@Override
	public boolean canSupportBoat(TagKey<Fluid> fluid, Entity entity) {
		return this.allowBoats;
	}

	@Override
	public boolean canSprintInFluid(TagKey<Fluid> fluid, LivingEntity entity) {
		return this.allowSprinting.test(fluid, entity);
	}

	public static class Builder implements FluidBehavior.Builder {
		private ToFloatFunction<LivingEntity> movementSpeed = _ -> 0.02f;
		private MovementSlowdownFunction movementSlowdown = (_, m, _, _, _) -> m.multiply(0.65f, 0.8f, 0.65f);
		private float gravityMultiplier = 1 / 16f;
		private double flowingPushScale = 0.014f;
		private boolean allowMovingDown = false;
		private boolean allowBoats = false;
		private boolean allowSwimming = false;
		private boolean makeMobsFloat = true;
		private boolean makeRiddenMobsFloat = false;
		private boolean drowning = false;
		private float fallDistanceModifier = 0;
		private BiPredicate<TagKey<Fluid>, LivingEntity> allowSprinting = (_, _) -> true;

		@Override
		public FluidBehavior.Builder movementSpeed(float value) {
			this.movementSpeed = _ -> value;
			return this;
		}

		@Override
		public FluidBehavior.Builder movementSpeed(ToFloatFunction<LivingEntity> function) {
			this.movementSpeed = function;
			return this;
		}

		@Override
		public FluidBehavior.Builder movementSlowdown(float value) {
			this.movementSlowdown = (_, m, _, _, _) -> m.scale(value);
			return this;
		}

		@Override
		public FluidBehavior.Builder movementSlowdown(float horizontal, float vertical) {
			this.movementSlowdown = (_, m, _, _, _) -> m.multiply(horizontal, vertical, horizontal);
			return this;
		}

		@Override
		public FluidBehavior.Builder movementSlowdown(ToFloatFunction<LivingEntity> function) {
			this.movementSlowdown = (e, m, _, _, _) -> m.scale(function.applyAsFloat(e));
			return this;
		}

		@Override
		public FluidBehavior.Builder movementSlowdown(MovementSlowdownFunction function) {
			this.movementSlowdown = function;
			return this;
		}

		@Override
		public FluidBehavior.Builder fallDistanceModifier(float value) {
			this.fallDistanceModifier = value;
			return this;
		}

		@Override
		public FluidBehavior.Builder gravityMultiplier(float value) {
			this.gravityMultiplier = value;
			return this;
		}

		@Override
		public FluidBehavior.Builder flowingPushScale(double value) {
			this.flowingPushScale = value;
			return this;
		}

		@Override
		public FluidBehavior.Builder allowMovingDown(boolean value) {
			this.allowMovingDown = value;
			return this;
		}

		@Override
		public FluidBehavior.Builder allowBoats(boolean value) {
			this.allowBoats = value;
			return this;
		}

		@Override
		public FluidBehavior.Builder allowSwimming(boolean value) {
			this.allowSwimming = value;
			return this;
		}

		@Override
		public FluidBehavior.Builder allowSprinting(boolean value) {
			this.allowSprinting = (_, _) -> value;
			return this;
		}

		@Override
		public FluidBehavior.Builder allowSprinting(Predicate<LivingEntity> value) {
			this.allowSprinting = (_, e) -> value.test(e);
			return this;
		}

		@Override
		public FluidBehavior.Builder allowSprinting(BiPredicate<TagKey<Fluid>, LivingEntity> value) {
			this.allowSprinting = value;
			return this;
		}

		@Override
		public FluidBehavior.Builder makeMobsFloat(boolean value) {
			this.makeMobsFloat = value;
			return this;
		}

		@Override
		public FluidBehavior.Builder makeRiddenMobsFloat(boolean value) {
			this.makeRiddenMobsFloat = value;
			return this;
		}

		@Override
		public FluidBehavior.Builder enableDrowning(boolean value) {
			this.drowning = value;
			return this;
		}

		@Override
		public FluidBehavior build() {
			return new SimpleConfiguredFluidBehavior(this.movementSpeed, this.movementSlowdown,
					this.gravityMultiplier, this.fallDistanceModifier, this.flowingPushScale, this.allowMovingDown, this.allowBoats, this.allowSwimming,
					this.makeMobsFloat, this.makeRiddenMobsFloat, this.drowning, this.allowSprinting);
		}
	}
}
