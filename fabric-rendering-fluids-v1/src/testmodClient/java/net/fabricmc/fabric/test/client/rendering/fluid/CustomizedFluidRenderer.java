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

import org.joml.Vector3f;

import net.minecraft.client.model.geom.builders.UVPair;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.FluidModel;
import net.minecraft.client.renderer.block.FluidRenderer;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;

import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandler;

public class CustomizedFluidRenderer implements FluidRenderHandler {
	@Override
	public void renderFluid(FluidRenderer fluidRenderer, BlockPos pos, BlockAndTintGetter level, FluidRenderer.Output output, BlockState blockState, FluidState fluidState) {
		FluidModel model = fluidRenderer.fluidModels.get(fluidState);
		TextureAtlasSprite sprite = model.flowingMaterial().sprite();

		int light = getLight(level, pos);
		float u1 = sprite.getU(0);
		float v1 = sprite.getV(0);
		float u2 = sprite.getU(1);
		float v2 = sprite.getV(fluidState.getHeight(level, pos));

		float x1 = (pos.getX() & 15) + 0.1f;
		float y1 = pos.getY() & 15;
		float z1 = (pos.getZ() & 15) + 0.1f;

		float x2 = (pos.getX() & 15) + 0.9f;
		float y2 = (pos.getY() & 15) + fluidState.getHeight(level, pos);
		float z2 = (pos.getZ() & 15) + 0.9f;

		BakedQuad quad0 = new BakedQuad(
				new Vector3f(x1, y1, z1),
				new Vector3f(x2, y1, z2),
				new Vector3f(x2, y2, z2),
				new Vector3f(x1, y2, z1),
				UVPair.pack(u1, v1),
				UVPair.pack(u2, v1),
				UVPair.pack(u2, v2),
				UVPair.pack(u1, v2),
				Direction.UP, null
		);
		output.put(ChunkSectionLayer.SOLID, 0, 0, 0, quad0, -1, 255, light);

		BakedQuad quad1 = new BakedQuad(
				new Vector3f(x1, y2, z1),
				new Vector3f(x2, y2, z2),
				new Vector3f(x2, y1, z2),
				new Vector3f(x1, y1, z1),
				UVPair.pack(u1, v2),
				UVPair.pack(u2, v2),
				UVPair.pack(u2, v1),
				UVPair.pack(u1, v1),
				Direction.UP, null
		);
		output.put(ChunkSectionLayer.SOLID, 0, 0, 0, quad1, -1, 255, light);

		BakedQuad quad2 = new BakedQuad(
				new Vector3f(x1, y2, z2),
				new Vector3f(x2, y2, z1),
				new Vector3f(x2, y1, z1),
				new Vector3f(x1, y1, z2),
				UVPair.pack(u1, v2),
				UVPair.pack(u2, v2),
				UVPair.pack(u2, v1),
				UVPair.pack(u1, v1),
				Direction.UP, null
		);
		output.put(ChunkSectionLayer.SOLID, 0, 0, 0, quad2, -1, 255, light);

		BakedQuad quad3 = new BakedQuad(
				new Vector3f(x1, y1, z2),
				new Vector3f(x2, y1, z1),
				new Vector3f(x2, y2, z1),
				new Vector3f(x1, y2, z2),
				UVPair.pack(u1, v1),
				UVPair.pack(u2, v1),
				UVPair.pack(u2, v2),
				UVPair.pack(u1, v2),
				Direction.UP, null
		);
		output.put(ChunkSectionLayer.SOLID, 0, 0, 0, quad3, -1, 255, light);
	}

	private int getLight(BlockAndTintGetter level, BlockPos pos) {
		int i = LightCoordsUtil.getLightCoords(level, pos);
		int j = LightCoordsUtil.getLightCoords(level, pos.above());
		int k = i & 255;
		int l = j & 255;
		int m = i >> 16 & 255;
		int n = j >> 16 & 255;
		return (k > l ? k : l) | (m > n ? m : n) << 16;
	}
}
