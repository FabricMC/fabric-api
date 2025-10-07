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
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexRendering;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.RenderStateDataKey;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldExtractionContext;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderEvents;

public class WorldRenderEventsTests implements ClientModInitializer {
	private static final RenderStateDataKey<Boolean> DIAMOND_BLOCK_OUTLINE = RenderStateDataKey.create(() -> "fabric api test mod block outline diamond block");

	private static void extractBlockOutline(WorldExtractionContext context, HitResult hitResult) {
		if (hitResult instanceof BlockHitResult blockHitResult && blockHitResult.getType() != HitResult.Type.MISS && context.world().getBlockState(blockHitResult.getBlockPos()).isOf(Blocks.DIAMOND_BLOCK)) {
			context.worldRenderState().outlineRenderState.setData(DIAMOND_BLOCK_OUTLINE, true);
		}
	}

	private static boolean onBlockOutline(WorldRenderContext context) {
		if (context.worldRenderState().outlineRenderState.getData(DIAMOND_BLOCK_OUTLINE)) {
			MatrixStack matrixStack = new MatrixStack();
			matrixStack.push();
			Vec3d cameraPos = MinecraftClient.getInstance().gameRenderer.getCamera().getPos();
			BlockPos pos = context.worldRenderState().outlineRenderState.pos();
			double x = pos.getX() - cameraPos.x;
			double y = pos.getY() - cameraPos.y;
			double z = pos.getZ() - cameraPos.z;
			matrixStack.translate(x + 0.25, y + 0.25 + 1, z + 0.25);
			matrixStack.scale(0.5f, 0.5f, 0.5f);

			MinecraftClient.getInstance().getBlockRenderManager().renderBlockAsEntity(
					Blocks.DIAMOND_BLOCK.getDefaultState(),
					matrixStack, context.consumers(), 15728880, OverlayTexture.DEFAULT_UV
			);

			matrixStack.pop();
		}

		return true;
	}

	/**
	 * Renders a translucent filled box at (0, 100, 0).
	 */
	private static void renderAfterTranslucent(WorldRenderContext context) {
		Vec3d camera = context.worldRenderState().cameraRenderState.pos;

		context.matrixStack().push();
		context.matrixStack().translate(-camera.x, -camera.y, -camera.z);

		VertexRendering.drawFilledBox(context.matrixStack(), context.consumers().getBuffer(RenderLayer.getDebugFilledBox()), 0, 100, 0, 1, 101, 1, 0, 1, 0, 0.5f);

		context.matrixStack().pop();
	}

	@Override
	public void onInitializeClient() {
		// Renders a diamond block above diamond blocks when they are looked at.
		WorldRenderEvents.AFTER_BLOCK_OUTLINE_EXTRACTION.register(WorldRenderEventsTests::extractBlockOutline);
		WorldRenderEvents.BEFORE_BLOCK_OUTLINE_RENDER.register(WorldRenderEventsTests::onBlockOutline);
		// Renders a translucent filled box at (0, 100, 0)
		WorldRenderEvents.AFTER_TRANSLUCENT_RENDER.register(WorldRenderEventsTests::renderAfterTranslucent);
	}
}
