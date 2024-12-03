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

package net.fabricmc.fabric.mixin.client.rendering;

import java.util.HashMap;
import java.util.Map;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.block.Block;
import net.minecraft.client.render.item.model.special.SpecialModelRenderer;
import net.minecraft.client.render.item.model.special.SpecialModelTypes;

import net.fabricmc.fabric.impl.client.rendering.SpecialBlockRendererRegistryImpl;

@Mixin(SpecialModelTypes.class)
abstract class SpecialModelTypesMixin {
	@Shadow
	@Final
	@Mutable
	private static Map<Block, SpecialModelRenderer.Unbaked> BLOCK_TO_MODEL_TYPE;

	@Inject(at = @At("RETURN"), method = "<clinit>*")
	private static void onReturnClinit(CallbackInfo ci) {
		// The map is normally an ImmutableMap.
		if (!(BLOCK_TO_MODEL_TYPE instanceof HashMap)) {
			BLOCK_TO_MODEL_TYPE = new HashMap<>(BLOCK_TO_MODEL_TYPE);
		}

		SpecialBlockRendererRegistryImpl.setup(BLOCK_TO_MODEL_TYPE::put);
	}
}
