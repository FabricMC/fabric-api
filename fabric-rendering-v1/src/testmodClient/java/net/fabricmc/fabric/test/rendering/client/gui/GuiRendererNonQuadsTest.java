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

import java.util.function.BiFunction;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import org.joml.Matrix3x2f;
import org.jspecify.annotations.Nullable;

import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.render.TextureSetup;
import net.minecraft.client.gui.render.state.GuiElementRenderState;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Util;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;

public class GuiRendererNonQuadsTest implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		HudElementRegistry.addFirst(Identifier.fromNamespaceAndPath("test", "gui_renderer_non_quads_test"), (context, renderTickCounter) -> {
			context.pose().pushMatrix();
			context.pose().rotateAbout(
					(float) Util.getMillis() / 3000,
					(float) context.guiHeight() / 8,
					(float) context.guiHeight() / 8
			);

			BiFunction<Integer, Integer, CustomTestState> testStateCreator = (xOffset, yOffset) ->
					new CustomTestState(
							new Matrix3x2f(context.pose()),
							null,
							context.guiHeight() / 8 + xOffset, context.guiHeight() / 8 + yOffset,
							context.guiHeight() / 8 + 16 + xOffset, context.guiHeight() / 8 + 16 + yOffset,
							context.guiWidth() / 8 + xOffset, context.guiHeight() / 8 + yOffset
			);

			context.guiRenderState.submitGuiElement(testStateCreator.apply(0, 0));
			// this second triangle should not stretch to include the first triangle's vertex
			context.guiRenderState.submitGuiElement(testStateCreator.apply(24, 24));

			context.pose().popMatrix();
		});
	}

	record CustomTestState(Matrix3x2f matrix, ScreenRectangle bounds, @Nullable ScreenRectangle scissorArea, int x0, int y0, int x1, int y1, int x2, int y2) implements GuiElementRenderState {
		CustomTestState(Matrix3x2f matrix, @Nullable ScreenRectangle scissorArea, int x0, int y0, int x1, int y1, int x2, int y2) {
			this(matrix, createTriangleBounds(x0, y0, x1, y1, x2, y2, matrix, scissorArea), scissorArea, x0, y0, x1, y1, x2, y2);
		}

		private static final RenderPipeline PIPELINE = RenderPipeline.builder(RenderPipelines.GUI_SNIPPET)
				.withLocation(Identifier.fromNamespaceAndPath("test", "gui_renderer_non_quads_test"))
				.withUsePipelineDrawModeForGui(true)
				.withVertexFormat(DefaultVertexFormat.POSITION_COLOR, VertexFormat.Mode.TRIANGLE_FAN)
				.build();

		@Override
		public void buildVertices(VertexConsumer vertices) {
			vertices.addVertexWith2DPose(matrix, x0, y0).setColor(0x99FFFF00)
					.addVertexWith2DPose(matrix, x1, y1).setColor(0x99FF00FF)
					.addVertexWith2DPose(matrix, x2, y2).setColor(0x9900FFFF);
		}

		@Override
		public TextureSetup textureSetup() {
			return TextureSetup.noTexture();
		}

		public RenderPipeline pipeline() {
			return PIPELINE;
		}

		private static ScreenRectangle createTriangleBounds(int x0, int y0, int x1, int y1, int x2, int y2, Matrix3x2f matrix, @Nullable ScreenRectangle scissorArea) {
			int minX = Math.min(x0, Math.min(x1, x2));
			int minY = Math.min(y0, Math.min(y1, y2));
			int maxX = Math.max(x0, Math.max(x1, x2));
			int maxY = Math.max(y0, Math.max(y1, y2));
			return createBounds(minX, minY, maxX, maxY, matrix, scissorArea);
		}

		@Nullable
		private static ScreenRectangle createBounds(int x0, int y0, int x1, int y1, Matrix3x2f matrix, @Nullable ScreenRectangle scissorArea) {
			ScreenRectangle screenRect = new ScreenRectangle(x0, y0, x1 - x0, y1 - y0).transformMaxBounds(matrix);
			return scissorArea != null
					? scissorArea.intersection(screenRect)
					: screenRect;
		}
	}
}
