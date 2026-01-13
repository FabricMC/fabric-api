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

package net.fabricmc.fabric.mixin.resource.conditions;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import net.minecraft.resources.RegistryLoadTask;
import net.minecraft.resources.ResourceManagerRegistryLoadTask;

import net.fabricmc.fabric.impl.resource.conditions.ResourceConditionsImpl;

@Mixin(ResourceManagerRegistryLoadTask.class)
public class ResourceManagerRegistryLoadTaskMixin {
	@ModifyExpressionValue(method = "lambda$load$2", at = @At(value = "NEW", target = "net/minecraft/resources/RegistryLoadTask$PendingRegistration"))
	private RegistryLoadTask.PendingRegistration<?> load(RegistryLoadTask.PendingRegistration<?> original) {
		if (original.value().right().isPresent() && original.value().right().get() == ResourceConditionsImpl.DISABLED_RESOURCE_EXCEPTION) {
			return null;
		}

		return original;
	}
}
