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

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.renderpearl.api.pipeline.RenderPipeline;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import net.fabricmc.fabric.impl.client.rendering.RenderPipelineModifierManagerImpl;

@Mixin(RenderSystem.class)
public class RenderSystemMixin {
	@Definition(id = "get", method = "Lcom/mojang/blaze3d/pipeline/PipelineCache;get(Lcom/mojang/renderpearl/api/pipeline/RenderPipeline;)Lcom/mojang/renderpearl/api/pipeline/CompiledRenderPipeline;")
	@Definition(id = "currentPipelineCache", field = "Lcom/mojang/blaze3d/systems/RenderSystem;currentPipelineCache:Lcom/mojang/blaze3d/pipeline/PipelineCache;")
	@Expression("currentPipelineCache.get(?)")
	@ModifyArg(
			method = "getCompiledPipelineNullable",
			at = @At("MIXINEXTRAS:EXPRESSION")
	)
	private static RenderPipeline modifyPipelines(RenderPipeline pipeline) {
		return RenderPipelineModifierManagerImpl.modify(pipeline);
	}
}
