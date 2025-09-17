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

package net.fabricmc.fabric.impl.client.rendering.hud;

import java.util.Optional;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.util.Identifier;

import net.fabricmc.fabric.api.client.rendering.v1.FabricRenderPipeline;

public class RenderPipelineGuiVertexFormatTest {
	@Test
	void testBuilderTransfersToSnippet() {
		RenderPipeline.Builder builder = RenderPipeline.builder();
		builder.withUsePipelineDrawModeForGui(true);
		RenderPipeline.Snippet snippet = builder.buildSnippet();
		Assertions.assertEquals(Optional.of(true), snippet.usePipelineDrawModeForGui());
		builder.withUsePipelineDrawModeForGui(false);
		snippet = builder.buildSnippet();
		Assertions.assertEquals(Optional.of(false), snippet.usePipelineDrawModeForGui());
		builder.withoutUsePipelineDrawModeForGui();
		snippet = builder.buildSnippet();
		Assertions.assertEquals(Optional.empty(), snippet.usePipelineDrawModeForGui());
	}

	@Test
	void testSnippetTransfersToPipeline() {
		RenderPipeline.Snippet snippet = FabricRenderPipeline.Snippet.withPipelineDrawModeForGui(createEmptySnippet(), true);
		RenderPipeline pipeline = RenderPipeline.builder(
						RenderPipelines.POSITION_COLOR_SNIPPET,
						RenderPipelines.TRANSFORMS_AND_PROJECTION_SNIPPET,
						snippet
				)
				.withLocation(Identifier.of("test", "pipeline_454b"))
				.build();
		Assertions.assertTrue(pipeline.usePipelineDrawModeForGui());

		snippet = FabricRenderPipeline.Snippet.withPipelineDrawModeForGui(createEmptySnippet(), false);

		pipeline = RenderPipeline.builder(
						RenderPipelines.POSITION_COLOR_SNIPPET,
						RenderPipelines.TRANSFORMS_AND_PROJECTION_SNIPPET,
						snippet
				)
				.withLocation(Identifier.of("test", "pipeline_454cc"))
				.build();
		Assertions.assertFalse(pipeline.usePipelineDrawModeForGui());
		// now the default should apply if no snippet sets the value, and the value isn't set on the builder
		snippet = FabricRenderPipeline.Snippet.withoutPipelineDrawModeForGui(createEmptySnippet());

		pipeline = RenderPipeline.builder(
						RenderPipelines.POSITION_COLOR_SNIPPET,
						RenderPipelines.TRANSFORMS_AND_PROJECTION_SNIPPET,
						snippet
				)
				.withLocation(Identifier.of("test", "pipeline_4547q"))
				.build();
		Assertions.assertFalse(pipeline.usePipelineDrawModeForGui());
	}

	@Test
	void testBuilderTransfersToPipeline() {
		RenderPipeline.Builder builder = RenderPipeline.builder(
						RenderPipelines.POSITION_COLOR_SNIPPET,
						RenderPipelines.TRANSFORMS_AND_PROJECTION_SNIPPET
				)
				.withUsePipelineDrawModeForGui(true)
				.withLocation(Identifier.of("test", "pipeline_454gg"));
		RenderPipeline pipeline = builder.build();
		Assertions.assertTrue(pipeline.usePipelineDrawModeForGui());

		builder.withUsePipelineDrawModeForGui(false)
				.withLocation(Identifier.of("test", "pipeline_454ff"));
		pipeline = builder.build();
		Assertions.assertFalse(pipeline.usePipelineDrawModeForGui());

		builder.withoutUsePipelineDrawModeForGui()
				.withLocation(Identifier.of("test", "pipeline_454jj"));
		pipeline = builder.build();
		Assertions.assertFalse(pipeline.usePipelineDrawModeForGui());
	}

	@Test
	void testSnippetRecordMethods() {
		FabricRenderPipeline.Snippet snippet = RenderPipeline.builder()
				.withUsePipelineDrawModeForGui(true)
				.buildSnippet();
		String expectedToString = "Snippet[vertexShader=Optional.empty, fragmentShader=Optional.empty, shaderDefines=Optional.empty, samplers=Optional.empty, uniforms=Optional.empty, blendFunction=Optional.empty, depthTestFunction=Optional.empty, polygonMode=Optional.empty, cull=Optional.empty, writeColor=Optional.empty, writeAlpha=Optional.empty, writeDepth=Optional.empty, colorLogic=Optional.empty, vertexFormat=Optional.empty, vertexFormatMode=Optional.empty, usePipelineDrawModeForGui=Optional[true]]";
		Assertions.assertEquals(expectedToString, snippet.toString());
		FabricRenderPipeline.Snippet snippet2 = RenderPipeline.builder()
				.withUsePipelineDrawModeForGui(true)
				.buildSnippet();
		Assertions.assertEquals(snippet, snippet2);
		Assertions.assertEquals(snippet.hashCode(), snippet2.hashCode());

		FabricRenderPipeline.Snippet snippet3 = RenderPipeline.builder()
				.buildSnippet();
		Assertions.assertNotEquals(snippet, snippet3);
		Assertions.assertNotEquals(snippet.hashCode(), snippet3.hashCode());
	}

	private static RenderPipeline.Snippet createEmptySnippet() {
		return new RenderPipeline.Snippet(
				Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(),
				Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(),
				Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(),
				Optional.empty(), Optional.empty(), Optional.empty()
		);
	}
}
