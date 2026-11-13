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

package net.fabricmc.fabric.mixin.client.keymapping;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import net.minecraft.client.Minecraft;
import net.minecraft.client.ToggleKeyMapping;

import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingContext;
import net.fabricmc.fabric.impl.client.keymapping.KeyMappingExtensions;

@Mixin(ToggleKeyMapping.class)
abstract class ToggleKeyMappingMixin {
	@WrapOperation(method = "release", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/ToggleKeyMapping;isDown()Z"))
	private boolean preserveToggleState(ToggleKeyMapping mapping, Operation<Boolean> original) {
		KeyMappingContext context = ((KeyMappingExtensions) mapping).fabric_getContext();

		// The screen has already changed when vanilla records which toggles should be restored.
		if (context != null && !context.isActive(Minecraft.getInstance())) {
			return mapping.isDown;
		}

		return original.call(mapping);
	}
}
