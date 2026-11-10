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

package net.fabricmc.fabric.test.rendering.client;

import com.mojang.blaze3d.systems.RenderSystem;
import org.joml.Matrix4f;

import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.DimensionEffects;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.util.Identifier;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.DimensionRenderingRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;

public class DimensionalRenderingTest implements ClientModInitializer {
	private static final Identifier END_SKY = Identifier.ofVanilla("textures/block/dirt.png");

	@Override
	public void onInitializeClient() {
		DimensionRenderingRegistry.registerDimensionEffects(Identifier.of("fabric_dimension", "void"), new FabicVoidDimensionEffects());
	}

	private static class FabicVoidDimensionEffects extends DimensionEffects.End {
		@Override
		public boolean renderSky(WorldRenderContext context) {
			RenderSystem.enableBlend();
			RenderSystem.defaultBlendFunc();
			RenderSystem.depthMask(false);
			RenderSystem.setShader(GameRenderer::getPositionTexColorProgram);
			RenderSystem.setShaderTexture(0, END_SKY);
			Tessellator tessellator = Tessellator.getInstance();
			BufferBuilder bufferBuilder = tessellator.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);

			Matrix4f matrix4f = context.positionMatrix();
			bufferBuilder.vertex(matrix4f, -100.0f, -100.0f, -100.0f).texture(0.0F, 0.0F).color(255, 255, 255, 255);
			bufferBuilder.vertex(matrix4f, -100.0f, -100.0f, 100.0f).texture(0.0F, 1.0F).color(255, 255, 255, 255);
			bufferBuilder.vertex(matrix4f, 100.0f, -100.0f, 100.0f).texture(1.0F, 1.0F).color(255, 255, 255, 255);
			bufferBuilder.vertex(matrix4f, 100.0f, -100.0f, -100.0f).texture(1.0F, 0.0F).color(255, 255, 255, 255);

			bufferBuilder.vertex(matrix4f, -100.0f, 100.0f, -100.0f).texture(0.0F, 0.0F).color(255, 255, 255, 255);
			bufferBuilder.vertex(matrix4f, -100.0f, -100.0f, -99.0f).texture(0.0F, 1.0F).color(255, 255, 255, 255);
			bufferBuilder.vertex(matrix4f, 100.0f, -100.0f, -99.0f).texture(1.0F, 1.0F).color(255, 255, 255, 255);
			bufferBuilder.vertex(matrix4f, 100.0f, 100.0f, -100.0f).texture(1.0F, 0.0F).color(255, 255, 255, 255);

			bufferBuilder.vertex(matrix4f, -100.0f, -100.0f, 100.0f).texture(0.0F, 0.0F).color(255, 255, 255, 255);
			bufferBuilder.vertex(matrix4f, -100.0f, 100.0f, 100.0f).texture(0.0F, 1.0F).color(255, 255, 255, 255);
			bufferBuilder.vertex(matrix4f, 100.0f, 100.0f, 100.0f).texture(1.0F, 1.0F).color(255, 255, 255, 255);
			bufferBuilder.vertex(matrix4f, 100.0f, -100.0f, 100.0f).texture(1.0F, 0.0F).color(255, 255, 255, 255);

			bufferBuilder.vertex(matrix4f, -100.0f, 100.0f, 101.0f).texture(0.0F, 0.0F).color(255, 255, 255, 255);
			bufferBuilder.vertex(matrix4f, -100.0f, 100.0f, -100.0f).texture(0.0F, 1.0F).color(255, 255, 255, 255);
			bufferBuilder.vertex(matrix4f, 100.0f, 100.0f, -100.0f).texture(1.0F, 1.0F).color(255, 255, 255, 255);
			bufferBuilder.vertex(matrix4f, 100.0f, 100.0f, 100.0f).texture(1.0F, 0.0F).color(255, 255, 255, 255);

			bufferBuilder.vertex(matrix4f, 100.0f, -100.0f, -100.0f).texture(0.0F, 0.0F).color(255, 255, 255, 255);
			bufferBuilder.vertex(matrix4f, 100.0f, -100.0f, 100.0f).texture(0.0F, 1.0F).color(255, 255, 255, 255);
			bufferBuilder.vertex(matrix4f, 100.0f, 100.0f, 100.0f).texture(1.0F, 1.0F).color(255, 255, 255, 255);
			bufferBuilder.vertex(matrix4f, 100.0f, 100.0f, -100.0f).texture(1.0F, 0.0F).color(255, 255, 255, 255);

			bufferBuilder.vertex(matrix4f, -100.0f, 100.0f, -100.0f).texture(0.0F, 0.0F).color(255, 255, 255, 255);
			bufferBuilder.vertex(matrix4f, -100.0f, 100.0f, 100.0f).texture(0.0F, 1.0F).color(255, 255, 255, 255);
			bufferBuilder.vertex(matrix4f, -100.0f, -100.0f, 100.0f).texture(1.0F, 1.0F).color(255, 255, 255, 255);
			bufferBuilder.vertex(matrix4f, -100.0f, -100.0f, -100.0f).texture(1.0F, 0.0F).color(255, 255, 255, 255);
			BufferRenderer.drawWithGlobalProgram(bufferBuilder.end());

			RenderSystem.depthMask(true);
			RenderSystem.disableBlend();

			return true;
		}
	}
}
