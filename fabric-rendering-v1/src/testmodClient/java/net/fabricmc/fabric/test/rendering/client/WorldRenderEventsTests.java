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

import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.item.Items;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;

public class WorldRenderEventsTests implements ClientModInitializer {
	private static boolean onBlockOutline(WorldRenderContext wrc, WorldRenderContext.BlockOutlineContext blockOutlineContext) {
		if (blockOutlineContext.blockState().isOf(Blocks.DIAMOND_BLOCK)) {
			wrc.matrixStack().push();
			Vec3d cameraPos = MinecraftClient.getInstance().gameRenderer.getCamera().getPos();
			BlockPos pos = blockOutlineContext.blockPos();
			double x = pos.getX() - cameraPos.x;
			double y = pos.getY() - cameraPos.y;
			double z = pos.getZ() - cameraPos.z;
			wrc.matrixStack().translate(x+0.25, y+0.25+1, z+0.25);
			wrc.matrixStack().scale(0.5f, 0.5f, 0.5f);

			MinecraftClient.getInstance().getBlockRenderManager().renderBlockAsEntity(
					Blocks.DIAMOND_BLOCK.getDefaultState(),
					wrc.matrixStack(), wrc.consumers(), 15728880, OverlayTexture.DEFAULT_UV);

			wrc.matrixStack().pop();
		}

		return true;
	}

	private static boolean onRenderSky(WorldRenderContext wrc, Runnable fogCallback) {
		ClientPlayerEntity player = MinecraftClient.getInstance().player;

		return !player.isHolding(Items.DEBUG_STICK) || !(player.getY() > 200);
	}

	// Renders a diamond block above diamond blocks when they are looked at.
	@Override
	public void onInitializeClient() {
		WorldRenderEvents.BLOCK_OUTLINE.register(WorldRenderEventsTests::onBlockOutline);
		WorldRenderEvents.RENDER_SKY.register(WorldRenderEventsTests::onRenderSky);
	}
}
