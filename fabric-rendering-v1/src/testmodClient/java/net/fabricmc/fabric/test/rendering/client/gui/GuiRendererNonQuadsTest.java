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

package net.fabricmc.fabric.test.rendering.client.gui;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.vertex.VertexFormat;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix3x2f;

import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.ScreenRect;
import net.minecraft.client.gui.render.state.SimpleGuiElementRenderState;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.texture.TextureSetup;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;

public class GuiRendererNonQuadsTest implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		HudElementRegistry.addFirst(Identifier.of("test", "gui_renderer_non_quads_test"), (context, renderTickCounter) -> {
			context.getMatrices().pushMatrix();
			context.getMatrices().rotateAbout(
					(float) Util.getMeasuringTimeMs() / 3000,
					(float) context.getScaledWindowHeight() / 8,
					(float) context.getScaledWindowHeight() / 8
			);

			context.state.addSimpleElement(new CustomTestState(
					new Matrix3x2f(context.getMatrices()),
					context.scissorStack.peekLast(),
					context.getScaledWindowHeight() / 8, context.getScaledWindowHeight() / 8,
					context.getScaledWindowHeight() / 8 + 16, context.getScaledWindowHeight() / 8 + 16,
					context.getScaledWindowWidth() / 8, context.getScaledWindowHeight() / 8
			));

			context.getMatrices().popMatrix();
		});
	}

	record CustomTestState(Matrix3x2f matrix, ScreenRect bounds, ScreenRect scissorArea, int x0, int y0, int x1, int y1, int x2, int y2) implements SimpleGuiElementRenderState {
		CustomTestState(Matrix3x2f matrix, ScreenRect scissorArea, int x0, int y0, int x1, int y1, int x2, int y2) {
			this(matrix, createTriangleBounds(x0, y0, x1, y1, x2, y2, matrix, scissorArea), scissorArea, x0, y0, x1, y1, x2, y2);
		}

		private static final RenderPipeline PIPELINE = RenderPipeline.builder(RenderPipelines.GUI_SNIPPET)
				.withLocation(Identifier.of("test", "gui_renderer_non_quads_test"))
				.withUsePipelineDrawModeForGui(true)
				.withVertexFormat(VertexFormats.POSITION_COLOR, VertexFormat.DrawMode.TRIANGLES)
				.build();

		@Override
		public void setupVertices(VertexConsumer vertices) {
			vertices.vertex(matrix, x0, y0).color(0x99FFFF00)
					.vertex(matrix, x1, y1).color(0x99FF00FF)
					.vertex(matrix, x2, y2).color(0x9900FFFF);
		}

		@Override
		public TextureSetup textureSetup() {
			return TextureSetup.empty();
		}

		public RenderPipeline pipeline() {
			return PIPELINE;
		}

		private static ScreenRect createTriangleBounds(int x0, int y0, int x1, int y1, int x2, int y2, Matrix3x2f matrix, @Nullable ScreenRect scissorArea) {
			int minX = Math.min(x0, Math.min(x1, x2));
			int minY = Math.min(y0, Math.min(y1, y2));
			int maxX = Math.max(x0, Math.max(x1, x2));
			int maxY = Math.max(y0, Math.max(y1, y2));
			return createBounds(minX, minY, maxX, maxY, matrix, scissorArea);
		}

		@Nullable
		private static ScreenRect createBounds(int x0, int y0, int x1, int y1, Matrix3x2f matrix, @Nullable ScreenRect scissorArea) {
			ScreenRect screenRect = new ScreenRect(x0, y0, x1 - x0, y1 - y0).transformEachVertex(matrix);
			return scissorArea != null
				? scissorArea.intersection(screenRect)
				: screenRect;
		}
	}
}
