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

package net.fabricmc.fabric.mixin.content.registry.fluid;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Slice;

import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.phys.Vec3;

import net.fabricmc.fabric.impl.content.registry.fluid.EntityFluidInteractionRegistryImpl;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity {
	public LivingEntityMixin(EntityType<?> type, Level level) {
		super(type, level);
	}

	@ModifyExpressionValue(method = "shouldTravelInFluid", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;isInLava()Z"))
	private boolean isInCustomFluid(boolean original) {
		if (original) {
			return true;
		}

		for (TagKey<Fluid> tagKey : EntityFluidInteractionRegistryImpl.getTrackedFluids()) {
			boolean inFluid = ((EntityAccessor) this).getFluidInteraction().isInFluid(tagKey);

			if (inFluid) {
				return true;
			}
		}

		return false;
	}

	@WrapWithCondition(method = "travelInFluid", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;travelInLava(Lnet/minecraft/world/phys/Vec3;DZD)V"))
	private boolean travelInCustomFluid(LivingEntity instance, Vec3 vec3, double input, boolean baseGravity, double isFalling) {
		for (TagKey<Fluid> tagKey : EntityFluidInteractionRegistryImpl.getTrackedFluids()) {
			boolean inFluid = ((EntityAccessor) this).getFluidInteraction().isInFluid(tagKey);

			if (inFluid) {
				EntityFluidInteractionRegistryImpl.getFluidBehavior(tagKey).travelInFluid(tagKey, (LivingEntity) (Object) this, vec3, input, baseGravity, isFalling);
				return false;
			}
		}

		return true;
	}

	@Definition(id = "WATER", field = "Lnet/minecraft/tags/FluidTags;WATER:Lnet/minecraft/tags/TagKey;")
	@Definition(id = "getFluidHeight", method = "Lnet/minecraft/world/entity/LivingEntity;getFluidHeight(Lnet/minecraft/tags/TagKey;)D")
	@Expression("this.getFluidHeight(WATER)")
	@ModifyExpressionValue(method = "aiStep", at = @At("MIXINEXTRAS:EXPRESSION"))
	private double tryOtherFluidsForFluidJumping(double original, @Share("fluid") LocalRef<TagKey<Fluid>> fluid) {
		if (original != 0) {
			return original;
		}

		for (TagKey<Fluid> tagKey : EntityFluidInteractionRegistryImpl.getTrackedFluids()) {
			boolean inFluid = ((EntityAccessor) this).getFluidInteraction().isInFluid(tagKey);

			if (inFluid) {
				fluid.set(tagKey);
				return ((EntityAccessor) this).getFluidInteraction().getFluidHeight(tagKey);
			}
		}

		return 0;
	}

	@ModifyExpressionValue(method = "baseTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;isEyeInFluid(Lnet/minecraft/tags/TagKey;)Z"))
	private boolean customFluidDrowning(boolean original) {
		if (original) {
			return true;
		}

		for (TagKey<Fluid> tagKey : EntityFluidInteractionRegistryImpl.getTrackedFluids()) {
			boolean inFluid = ((EntityAccessor) this).getFluidInteraction().isEyeInFluid(tagKey);

			if (inFluid && EntityFluidInteractionRegistryImpl.getFluidBehavior(tagKey).canDrownInFluid(tagKey, (LivingEntity) (Object) this)) {
				return true;
			}
		}

		return false;
	}

	// This looks to be a vanilla bug or some sort of leftover?
	// Causes fluids to apply their push twice if they aren't water (so in case of vanilla, lava only).
	// This is not desired effect for mods through, as it will cause push value to functionally double (and fluid update stuff applying twice).
	// So I decided to just skip it for modded ones, while keeping vanilla/lava as is.
	@ModifyExpressionValue(method = "checkFallDamage", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;isInWater()Z"))
	private boolean fixDoubleUpdateForCustomFluids(boolean original) {
		if (original) {
			return true;
		}

		for (TagKey<Fluid> tagKey : EntityFluidInteractionRegistryImpl.getTrackedFluids()) {
			boolean inFluid = ((EntityAccessor) this).getFluidInteraction().isInFluid(tagKey);

			if (inFluid) {
				return true;
			}
		}

		return false;
	}

	@ModifyExpressionValue(method = "aiStep", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;isInLava()Z"),
			slice = @Slice(from = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;getFluidJumpThreshold()D"))
	)
	private boolean jumpInCustomFluid(boolean original, @Share("fluid") LocalRef<TagKey<Fluid>> fluid) {
		return original || fluid.get() != null;
	}

	@Definition(id = "LAVA", field = "Lnet/minecraft/tags/FluidTags;LAVA:Lnet/minecraft/tags/TagKey;")
	@Definition(id = "jumpInLiquid", method = "Lnet/minecraft/world/entity/LivingEntity;jumpInLiquid(Lnet/minecraft/tags/TagKey;)V")
	@Expression("this.jumpInLiquid(LAVA)")
	@ModifyArg(method = "aiStep", at = @At("MIXINEXTRAS:EXPRESSION"))
	private TagKey<Fluid> swapFluidTag(TagKey<Fluid> fluidTagKey, @Share("fluid") LocalRef<TagKey<Fluid>> fluid) {
		TagKey<Fluid> custom = fluid.get();
		return custom != null ? custom : fluidTagKey;
	}
}
