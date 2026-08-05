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

import java.util.Optional;
import java.util.function.Consumer;

import com.mojang.blaze3d.pipeline.RenderPipeline;

import net.fabricmc.fabric.impl.client.rendering.FabricRenderPipelineImpl;

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

	/**
	 * Creates a builder that when built would create this pipeline.
	 * Note that the caller must call {@link RenderPipeline.Builder#withLocation(net.minecraft.resources.Identifier)} returned builder before building.
	 *
	 * @return a builder that could build this pipeline
	 */
	default RenderPipeline.Builder toBuilder() {
		RenderPipeline $self = (RenderPipeline) this;
		RenderPipeline.Builder builder = RenderPipeline.builder()
				.withLocation($self.getLocation())
				.withVertexShader($self.getVertexShader())
				.withFragmentShader($self.getFragmentShader())
				.withDepthTestFunction($self.getDepthTestFunction())
				.withPolygonMode($self.getPolygonMode())
				.withCull($self.isCull())
				.withColorLogic($self.getColorLogic())
				.withColorWrite($self.isWriteColor(), $self.isWriteAlpha())
				.withDepthWrite($self.isWriteDepth())
				.withVertexFormat($self.getVertexFormat(), $self.getVertexFormatMode())
				.withDepthBias($self.getDepthBiasScaleFactor(), $self.getDepthBiasConstant())
				.withUsePipelineDrawModeForGui($self.usePipelineDrawModeForGui());
		$self.getShaderDefines().values().forEach((v1, v2) -> ((FabricRenderPipelineImpl.BuilderImpl) builder).fabric$getShaderDefines().define(v1, v2));
		$self.getShaderDefines().flags().forEach(flag -> ((FabricRenderPipelineImpl.BuilderImpl) builder).fabric$getShaderDefines().define(flag));
		return builder;
	}

	/**
	 * Creates a snippet that describes this pipeline.
	 *
	 * @return a snippet that describes this pipeline
	 */
	default RenderPipeline.Snippet toSnippet() {
		return toBuilder().buildSnippet();
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
		 * Creates a new snippet that is identical to this one aside from the changes induced by {@code mutator}.
		 *
		 * @param mutator the changes to make to this snippet
		 * @return a new snippet identical to this one aside from the changes applied by mutator
		 */
		default RenderPipeline.Snippet with(Consumer<RenderPipeline.Builder> mutator) {
			RenderPipeline.Builder builder = RenderPipeline.builder((RenderPipeline.Snippet) this);
			mutator.accept(builder);
			return builder.buildSnippet();
		}

		/**
		 * Creates a new snippet with the specified pipeline draw mode for GUI rendering.
		 *
		 * @param usePipelineDrawMode true if the pipeline draw mode should be used for GUI rendering, false otherwise.
		 * @return a new RenderPipeline.Snippet instance with the specified pipeline draw mode.
		 */
		static RenderPipeline.Snippet withPipelineDrawModeForGui(RenderPipeline.Snippet base, boolean usePipelineDrawMode) {
			return RenderPipeline.builder(base).withUsePipelineDrawModeForGui(usePipelineDrawMode).buildSnippet();
		}

		/**
		 * Creates a new snippet without the pipeline draw mode for GUI rendering.
		 *
		 * @return a new RenderPipeline.Snippet instance without any effect on whether the pipeline draw mode will be used for GUI rendering.
		 */
		static RenderPipeline.Snippet withoutPipelineDrawModeForGui(RenderPipeline.Snippet base) {
			return RenderPipeline.builder(base).withoutUsePipelineDrawModeForGui().buildSnippet();
		}
	}
}
