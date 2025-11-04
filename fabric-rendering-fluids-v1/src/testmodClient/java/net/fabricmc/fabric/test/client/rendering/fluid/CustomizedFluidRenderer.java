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

package net.fabricmc.fabric.test.client.rendering.fluid;

import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;

import net.fabricmc.fabric.api.client.render.fluid.v1.SimpleFluidRenderHandler;

public class CustomizedFluidRenderer extends SimpleFluidRenderHandler {
	public CustomizedFluidRenderer(Identifier overlayTexture) {
		super(overlayTexture, overlayTexture, overlayTexture);
	}

	@Override
	public void renderFluid(BlockPos pos, BlockAndTintGetter world, VertexConsumer vertexConsumer, BlockState blockState, FluidState fluidState) {
		int light = getLight(world, pos);
		float u1 = sprites[2].getU(0);
		float v1 = sprites[2].getV(0);
		float u2 = sprites[2].getU(1);
		float v2 = sprites[2].getV(fluidState.getHeight(world, pos));

		float x1 = (pos.getX() & 15) + 0.1f;
		float y1 = pos.getY() & 15;
		float z1 = (pos.getZ() & 15) + 0.1f;

		float x2 = (pos.getX() & 15) + 0.9f;
		float y2 = (pos.getY() & 15) + fluidState.getHeight(world, pos);
		float z2 = (pos.getZ() & 15) + 0.9f;

		vertex(vertexConsumer, x1, y1, z1, 1, 1, 1, u1, v1, light);
		vertex(vertexConsumer, x2, y1, z2, 1, 1, 1, u2, v1, light);
		vertex(vertexConsumer, x2, y2, z2, 1, 1, 1, u2, v2, light);
		vertex(vertexConsumer, x1, y2, z1, 1, 1, 1, u1, v2, light);

		vertex(vertexConsumer, x1, y2, z1, 1, 1, 1, u1, v2, light);
		vertex(vertexConsumer, x2, y2, z2, 1, 1, 1, u2, v2, light);
		vertex(vertexConsumer, x2, y1, z2, 1, 1, 1, u2, v1, light);
		vertex(vertexConsumer, x1, y1, z1, 1, 1, 1, u1, v1, light);

		vertex(vertexConsumer, x1, y2, z2, 1, 1, 1, u1, v2, light);
		vertex(vertexConsumer, x2, y2, z1, 1, 1, 1, u2, v2, light);
		vertex(vertexConsumer, x2, y1, z1, 1, 1, 1, u2, v1, light);
		vertex(vertexConsumer, x1, y1, z2, 1, 1, 1, u1, v1, light);

		vertex(vertexConsumer, x1, y1, z2, 1, 1, 1, u1, v1, light);
		vertex(vertexConsumer, x2, y1, z1, 1, 1, 1, u2, v1, light);
		vertex(vertexConsumer, x2, y2, z1, 1, 1, 1, u2, v2, light);
		vertex(vertexConsumer, x1, y2, z2, 1, 1, 1, u1, v2, light);
	}

	private void vertex(VertexConsumer vertexConsumer, float x, float y, float z, float red, float green, float blue, float u, float v, int light) {
		vertexConsumer.addVertex(x, y, z).setColor(red, green, blue, 1.0F).setUv(u, v).setLight(light).setNormal(0.0F, 1.0F, 0.0F);
	}

	private int getLight(BlockAndTintGetter world, BlockPos pos) {
		int i = LevelRenderer.getLightColor(world, pos);
		int j = LevelRenderer.getLightColor(world, pos.above());
		int k = i & 255;
		int l = j & 255;
		int m = i >> 16 & 255;
		int n = j >> 16 & 255;
		return (k > l ? k : l) | (m > n ? m : n) << 16;
	}
}
