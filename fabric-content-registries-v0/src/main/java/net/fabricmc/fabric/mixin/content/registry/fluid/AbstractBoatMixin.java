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

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.vehicle.boat.AbstractBoat;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;

import net.fabricmc.fabric.impl.content.registry.fluid.EntityFluidInteractionRegistryImpl;

@Mixin(AbstractBoat.class)
public class AbstractBoatMixin {
	@WrapOperation(method = {"checkInWater", "isUnderwater"}, at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/material/FluidState;is(Lnet/minecraft/tags/TagKey;)Z"))
	private boolean customFluidSupport(FluidState instance, TagKey<Fluid> water, Operation<Boolean> original) {
		for (TagKey<Fluid> tagKey : EntityFluidInteractionRegistryImpl.getTrackedFluids()) {
			if (instance.is(tagKey) && EntityFluidInteractionRegistryImpl.getFluidBehavior(tagKey).canSupportBoat(tagKey, (Entity) (Object) this)) {
				return true;
			}
		}

		return original.call(instance, water);
	}
}
