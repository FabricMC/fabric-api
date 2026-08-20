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

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.level.material.Fluid;

import net.fabricmc.fabric.impl.content.registry.fluid.EntityFluidInteractionRegistryImpl;
import net.fabricmc.fabric.impl.content.registry.fluid.InternalEntityFluidExtension;

@Mixin(FloatGoal.class)
public class FloatGoalMixin {
	@Shadow
	@Final
	private Mob mob;

	@ModifyReturnValue(method = "canUse", at = @At("RETURN"))
	private boolean floatInCustomFluids(boolean original) {
		if (original) {
			return true;
		}

		for (TagKey<Fluid> tagKey : ((InternalEntityFluidExtension) mob).fabric_api$getTouchedCustomFluids()) {
			if (EntityFluidInteractionRegistryImpl.getFluidBehavior(tagKey).shouldTryFloatingInFluid(tagKey, this.mob)) {
				return true;
			}
		}

		return false;
	}
}
