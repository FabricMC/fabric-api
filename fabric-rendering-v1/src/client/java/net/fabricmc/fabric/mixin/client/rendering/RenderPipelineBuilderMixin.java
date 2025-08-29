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

import java.util.List;
import java.util.Optional;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.DepthTestFunction;
import com.mojang.blaze3d.platform.LogicOp;
import com.mojang.blaze3d.platform.PolygonMode;
import com.mojang.blaze3d.vertex.VertexFormat;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.gl.Defines;
import net.minecraft.util.Identifier;

import net.fabricmc.fabric.api.client.rendering.v1.FabricRenderPipeline;
import net.fabricmc.fabric.impl.client.rendering.FabricRenderPipelineImpl;
import net.fabricmc.fabric.impl.client.rendering.FabricRenderPipelineInternals;

@Mixin(RenderPipeline.Builder.class)
class RenderPipelineBuilderMixin implements FabricRenderPipeline.Builder {
	@Unique
	private Optional<Boolean> usePipelineDrawModeForGui = Optional.empty();

	@Override
	public RenderPipeline.Builder withUsePipelineDrawModeForGui(boolean usePipelineDrawMode) {
		this.usePipelineDrawModeForGui = Optional.of(usePipelineDrawMode);
		return (RenderPipeline.Builder) (Object) this;
	}

	@Override
	public RenderPipeline.Builder withoutUsePipelineDrawModeForGui() {
		this.usePipelineDrawModeForGui = Optional.empty();
		return (RenderPipeline.Builder) (Object) this;
	}

	@Inject(
			method = "withSnippet",
			at = @At("TAIL")
	)
	private void copyUsePipelineDrawModeForGuiFromSnippet(RenderPipeline.Snippet snippet, CallbackInfo ci) {
		snippet.usePipelineDrawModeForGui().ifPresent(value -> this.usePipelineDrawModeForGui = Optional.of(value));
	}

	@WrapOperation(
			method = "buildSnippet",
			at = @At(
					value = "NEW",
					target = "Lcom/mojang/blaze3d/pipeline/RenderPipeline$Snippet;"
			)
	)
	private RenderPipeline.Snippet copyUsePipelineDrawModeForGuiToSnippet(
			Optional<Identifier> vertexShader,
			Optional<Identifier> fragmentShader,
			Optional<Defines> shaderDefines,
			Optional<List<String>> samplers,
			Optional<List<RenderPipeline.UniformDescription>> uniforms,
			Optional<BlendFunction> blendFunction,
			Optional<DepthTestFunction> depthTestFunction,
			Optional<PolygonMode> polygonMode,
			Optional<Boolean> cull,
			Optional<Boolean> writeColor,
			Optional<Boolean> writeAlpha,
			Optional<Boolean> writeDepth,
			Optional<LogicOp> colorLogic,
			Optional<VertexFormat> vertexFormat,
			Optional<VertexFormat.DrawMode> vertexFormatMode,
			Operation<RenderPipeline.Snippet> original
	) {
		return FabricRenderPipelineInternals.withSnippetUsePipelineVertexFormatForGui(() -> original.call(vertexShader, fragmentShader, shaderDefines, samplers, uniforms, blendFunction, depthTestFunction, polygonMode, cull, writeColor, writeAlpha, writeDepth, colorLogic, vertexFormat, vertexFormatMode), usePipelineDrawModeForGui);
	}

	@ModifyReturnValue(
			method = "build",
			at = @At("RETURN")
	)
	private RenderPipeline copyUsePipelineDrawModeForGuiToPipeline(RenderPipeline original) {
		((FabricRenderPipelineImpl) original).fabric$setUsePipelineDrawModeForGuiSetter(this.usePipelineDrawModeForGui.orElse(false));
		return original;
	}
}
