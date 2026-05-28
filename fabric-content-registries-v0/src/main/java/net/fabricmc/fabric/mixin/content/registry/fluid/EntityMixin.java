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

import java.util.HashSet;
import java.util.Set;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityFluidInteraction;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;

import net.fabricmc.fabric.impl.content.registry.fluid.EntityFluidInteractionRegistryImpl;

@Mixin(Entity.class)
public abstract class EntityMixin {
	@Shadow
	@Final
	private EntityFluidInteraction fluidInteraction;

	@Shadow
	public abstract boolean isPushedByFluid();

	@ModifyArg(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/EntityFluidInteraction;<init>(Ljava/util/Set;)V"))
	private Set<TagKey<Fluid>> addCustomTags(Set<TagKey<Fluid>> fluids) {
		var result = new HashSet<>(fluids);
		result.addAll(EntityFluidInteractionRegistryImpl.getTrackedFluids());
		return result;
	}

	@ModifyReturnValue(method = "isInLiquid", at = @At("RETURN"))
	private boolean checkForCustomFluids(boolean original) {
		if (original) {
			return true;
		}

		for (TagKey<Fluid> tagKey : EntityFluidInteractionRegistryImpl.getTrackedFluids()) {
			boolean inFluid = this.fluidInteraction.isInFluid(tagKey);

			if (inFluid) {
				return true;
			}
		}

		return false;
	}

	@ModifyReturnValue(method = "updateFluidInteraction", at = @At("RETURN"))
	private boolean handleCustomFluidInteractionUpdates(boolean hasInteracted) {
		final boolean isPushedByFluid = this.isPushedByFluid();

		for (TagKey<Fluid> tagKey : EntityFluidInteractionRegistryImpl.getTrackedFluids()) {
			boolean inFluid = this.fluidInteraction.isInFluid(tagKey);

			if (inFluid) {
				hasInteracted = true;
				EntityFluidInteractionRegistryImpl.getFluidBehavior(tagKey).handleFluidInteractionUpdate(tagKey, (Entity) (Object) this, this.fluidInteraction, isPushedByFluid);
			}
		}

		return hasInteracted;
	}

	@ModifyExpressionValue(method = "updateSwimming", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;isInWater()Z"))
	private boolean checkIfInSwimmableFluid(boolean original) {
		if (original) {
			return true;
		}

		for (TagKey<Fluid> tagKey : EntityFluidInteractionRegistryImpl.getTrackedFluids()) {
			boolean inFluid = this.fluidInteraction.isInFluid(tagKey);

			if (inFluid && EntityFluidInteractionRegistryImpl.getFluidBehavior(tagKey).canSwimInFluid(tagKey, (Entity) (Object) this)) {
				return true;
			}
		}

		return false;
	}

	@ModifyExpressionValue(method = "updateSwimming", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;isUnderWater()Z"))
	private boolean checkIfUnderSwimmableFluid(boolean original, @Share("successfulFluids") LocalRef<Set<TagKey<Fluid>>> successfulFluids) {
		var set = new HashSet<TagKey<Fluid>>();
		successfulFluids.set(set);

		for (TagKey<Fluid> tagKey : EntityFluidInteractionRegistryImpl.getTrackedFluids()) {
			boolean inFluid = this.fluidInteraction.isEyeInFluid(tagKey);

			if (inFluid && EntityFluidInteractionRegistryImpl.getFluidBehavior(tagKey).canSwimInFluid(tagKey, (Entity) (Object) this)) {
				original = true;
				set.add(tagKey);
			}
		}

		return original;
	}

	@WrapOperation(method = "updateSwimming", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/material/FluidState;is(Lnet/minecraft/tags/TagKey;)Z"))
	private boolean checkIfStandingInSwimmableFluid(FluidState instance, TagKey<Fluid> waterFluidKey, Operation<Boolean> original,
													@Share("successfulFluids") LocalRef<Set<TagKey<Fluid>>> successfulFluids) {
		if (original.call(instance, waterFluidKey)) {
			return true;
		}

		for (TagKey<Fluid> tagKey : successfulFluids.get()) {
			if (instance.is(tagKey)) {
				return true;
			}
		}

		return false;
	}

	@ModifyExpressionValue(method = "isVisuallyCrawling", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;isInWater()Z"))
	private boolean checkCustomFluids(boolean original) {
		if (original) {
			return true;
		}

		for (TagKey<Fluid> tagKey : EntityFluidInteractionRegistryImpl.getTrackedFluids()) {
			boolean inFluid = this.fluidInteraction.isInFluid(tagKey);

			if (inFluid && EntityFluidInteractionRegistryImpl.getFluidBehavior(tagKey).canSwimInFluid(tagKey, (Entity) (Object) this)) {
				return true;
			}
		}

		return false;
	}

	@ModifyReturnValue(method = "canSpawnSprintParticle", at = @At("RETURN"))
	private boolean preventParticlesInFluids(boolean original) {
		if (!original) {
			return false;
		}

		for (TagKey<Fluid> tagKey : EntityFluidInteractionRegistryImpl.getTrackedFluids()) {
			boolean inFluid = this.fluidInteraction.isInFluid(tagKey);

			if (inFluid) {
				return false;
			}
		}

		return true;
	}
}
