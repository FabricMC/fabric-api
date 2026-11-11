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

package net.fabricmc.fabric.api.client.rendering.v1;

import java.util.Map;
import java.util.Optional;

import com.mojang.renderpearl.api.pipeline.ColorTargetState;
import com.mojang.renderpearl.api.pipeline.RenderPipeline;
import com.mojang.renderpearl.api.vertex.VertexFormat;

import net.minecraft.client.renderer.ShaderDefines;

import net.fabricmc.fabric.mixin.client.rendering.RenderPipelineBuilderAccessor;

/**
 * General purpose Fabric extensions to the {@link RenderPipeline} class.
 *
 * <p>Note: This interface is automatically implemented on all render pipelines via Mixin and interface injection.
 */
public interface FabricRenderPipeline {
	/**
	 * Returns whether the pipeline draw mode should be used for GUI rendering.
	 *
	 * @return true if the pipeline draw mode should be used for GUI rendering, false otherwise.
	 */
	default boolean usePipelineDrawModeForGui() {
		throw new AssertionError("Implemented in Mixin");
	}

	/// Converts this pipeline to a builder to allow for modification.
	default RenderPipeline.Builder toBuilder() {
		RenderPipeline pipeline = (RenderPipeline) this;
		RenderPipeline.Builder builder = RenderPipeline.builder();
		builder.withLocation(pipeline.getLocation());

		pipeline.getShaders().forEach((key, id) -> {
			switch (key) {
				case VERTEX -> builder.withVertexShader(id);
				case FRAGMENT -> builder.withFragmentShader(id);
				default -> throw new IllegalStateException("Unexpected ShaderType " + key);
			}
		});

		if (!pipeline.getShaderDefines().isEmpty()) {
			ShaderDefines.Builder defBuilder = ShaderDefines.builder();

			for (Map.Entry<String, String> entry : pipeline.getShaderDefines().values().entrySet()) {
				defBuilder.define(entry.getKey(), entry.getValue());
			}

			for (String flag : pipeline.getShaderDefines().flags()) {
				defBuilder.define(flag);
			}

			((RenderPipelineBuilderAccessor) builder).fabric$setDefines(Optional.of(defBuilder));
		}

		if (!pipeline.getBindGroupLayouts().isEmpty()) {
			pipeline.getBindGroupLayouts().forEach(builder::withBindGroupLayout);
		}

		if (pipeline.getDepthStencilState() != null) {
			builder.withDepthStencilState(pipeline.getDepthStencilState());
		}

		builder.withPolygonMode(pipeline.getPolygonMode());
		builder.withCull(pipeline.isCull());

		int i = 0;

		for (ColorTargetState state : pipeline.getColorTargetStates()) {
			if (state == null) {
				builder.withUnusedColorTargetState(i);
			} else {
				builder.withColorTargetState(i, state);
			}

			i++;
		}

		i = 0;

		for (VertexFormat format : pipeline.getVertexFormatBindings()) {
			if (format != null) builder.withVertexBinding(i, format);
			i++;
		}

		builder.withPrimitiveTopology(pipeline.getPrimitiveTopology());
		builder.withPushConstantSize(pipeline.pushConstantSize());
		return builder;
	}

	/**
	 * General purpose Fabric extensions to the {@link RenderPipeline.Builder} class.
	 *
	 * <p>Note: This interface is automatically implemented on all render pipeline builders via Mixin and interface injection.
	 */
	interface Builder {
		/**
		 * Sets whether the pipeline draw mode should be used for GUI rendering.
		 *
		 * @param usePipelineDrawMode true if the pipeline draw mode should be used for GUI rendering, false otherwise.
		 * @return this builder instance for chaining.
		 */
		default RenderPipeline.Builder withUsePipelineDrawModeForGui(boolean usePipelineDrawMode) {
			throw new AssertionError("Implemented in Mixin");
		}

		/**
		 * Set the default behavior for GUI rendering regarding the pipeline draw mode.
		 *
		 * @return this builder instance for chaining.
		 */
		default RenderPipeline.Builder withoutUsePipelineDrawModeForGui() {
			throw new AssertionError("Implemented in Mixin");
		}
	}

	/**
	 * General purpose Fabric extensions to the {@link RenderPipeline.Snippet} class.
	 *
	 * <p>Note: This interface is automatically implemented on all render pipeline snippets via Mixin and interface injection.
	 */
	interface Snippet {
		/**
		 * Returns whether the pipeline draw mode should be used for GUI rendering.
		 *
		 * @return an Optional containing true if the pipeline draw mode should be used for GUI rendering, false otherwise.
		 */
		default Optional<Boolean> usePipelineDrawModeForGui() {
			throw new AssertionError("Implemented in Mixin");
		}

		/**
		 * Creates a new snippet with the specified pipeline draw mode for GUI rendering.
		 *
		 * @param usePipelineDrawMode true if the pipeline draw mode should be used for GUI rendering, false otherwise.
		 * @return a new RenderPipeline.Snippet instance with the specified pipeline draw mode.
		 */
		static RenderPipeline.Snippet withPipelineDrawModeForGui(RenderPipeline.Snippet base, boolean usePipelineDrawMode) {
			return ((FabricRenderPipeline.Builder) RenderPipeline.builder(base)).withUsePipelineDrawModeForGui(usePipelineDrawMode).buildSnippet();
		}

		/**
		 * Creates a new snippet without the pipeline draw mode for GUI rendering.
		 *
		 * @return a new RenderPipeline.Snippet instance without any effect on whether the pipeline draw mode will be used for GUI rendering.
		 */
		static RenderPipeline.Snippet withoutPipelineDrawModeForGui(RenderPipeline.Snippet base) {
			return ((FabricRenderPipeline.Builder) RenderPipeline.builder(base)).withoutUsePipelineDrawModeForGui().buildSnippet();
		}
	}
}
