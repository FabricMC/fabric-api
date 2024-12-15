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

package net.fabricmc.fabric.mixin.object.builder;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.entity.EntityType;

import net.fabricmc.fabric.impl.object.builder.FabricEntityTypeImpl;

@Mixin(EntityType.class)
public abstract class EntityTypeMixin implements FabricEntityTypeImpl {
	@Unique
	private Boolean alwaysUpdateVelocity;
	@Unique
	private Boolean canPotentiallyExecuteCommands;

	@Inject(method = "alwaysUpdateVelocity", at = @At("HEAD"), cancellable = true)
	public void onAlwaysUpdateVelocity(CallbackInfoReturnable<Boolean> cir) {
		if (alwaysUpdateVelocity != null) {
			cir.setReturnValue(alwaysUpdateVelocity);
		}
	}

	@Inject(method = "canPotentiallyExecuteCommands", at = @At("HEAD"), cancellable = true)
	public void onCanPotentiallyExecuteCommands(CallbackInfoReturnable<Boolean> cir) {
		if (canPotentiallyExecuteCommands != null) {
			cir.setReturnValue(canPotentiallyExecuteCommands);
		}
	}

	@Override
	public void fabric_setAlwaysUpdateVelocity(Boolean alwaysUpdateVelocity) {
		this.alwaysUpdateVelocity = alwaysUpdateVelocity;
	}

	@Override
	public void fabric_setCanPotentiallyExecuteCommands(Boolean canPotentiallyExecuteCommands) {
		this.canPotentiallyExecuteCommands = canPotentiallyExecuteCommands;
	}
}
