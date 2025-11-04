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

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.BlockOutlineRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.util.ARGB;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.fabricmc.fabric.api.client.gametest.v1.screenshot.TestScreenshotComparisonOptions;
import net.fabricmc.fabric.api.client.rendering.v1.RenderStateDataKey;
import net.fabricmc.fabric.api.client.rendering.v1.world.AbstractWorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldExtractionContext;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderEvents;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldTerrainRenderContext;

public class WorldRenderEventsTests implements ClientModInitializer, FabricClientGameTest {
	private static final RenderStateDataKey<Boolean> DIAMOND_BLOCK_OUTLINE = RenderStateDataKey.create(() -> "fabric api test mod block outline diamond block");

	private static void extractBlockOutline(WorldExtractionContext context, HitResult hitResult) {
		if (hitResult instanceof BlockHitResult blockHitResult && blockHitResult.getType() != HitResult.Type.MISS && context.world().getBlockState(blockHitResult.getBlockPos()).is(Blocks.DIAMOND_BLOCK)) {
			context.worldState().blockOutlineRenderState.setData(DIAMOND_BLOCK_OUTLINE, true);
		}
	}

	private static boolean beforeBlockOutline(WorldRenderContext context, BlockOutlineRenderState outlineRenderState) {
		if (Boolean.TRUE.equals(outlineRenderState.getData(DIAMOND_BLOCK_OUTLINE))) {
			PoseStack matrixStack = new PoseStack();
			matrixStack.pushPose();
			Vec3 cameraPos = Minecraft.getInstance().gameRenderer.getMainCamera().position();
			BlockPos pos = outlineRenderState.pos();
			double x = pos.getX() - cameraPos.x;
			double y = pos.getY() - cameraPos.y;
			double z = pos.getZ() - cameraPos.z;
			matrixStack.translate(x + 0.25, y + 0.25 + 1, z + 0.25);
			matrixStack.scale(0.5f, 0.5f, 0.5f);

			Minecraft.getInstance().getBlockRenderer().renderSingleBlock(
					Blocks.DIAMOND_BLOCK.defaultBlockState(),
					matrixStack, context.consumers(), 15728880, OverlayTexture.NO_OVERLAY
			);

			matrixStack.popPose();
		}

		return true;
	}

	/**
	 * Renders a translucent filled box at (0, 100, 0).
	 */
	private static void renderBeforeTranslucent(WorldRenderContext context) {
		Vec3 camera = context.worldState().cameraRenderState.pos;

		context.matrices().pushPose();
		context.matrices().translate(-camera.x, -camera.y, -camera.z);

		AABB box = new AABB(BlockPos.ZERO.above(100));
		int color = ARGB.colorFromFloat(0.5f, 0, 1, 0);
		TestRenderUtils.drawFilledBox(context.matrices(), context.consumers().getBuffer(RenderTypes.debugFilledBox()), box, color);

		context.matrices().popPose();
	}

	@Override
	public void onInitializeClient() {
		// Renders a diamond block above diamond blocks when they are looked at.
		WorldRenderEvents.AFTER_BLOCK_OUTLINE_EXTRACTION.register(WorldRenderEventsTests::extractBlockOutline);
		WorldRenderEvents.BEFORE_BLOCK_OUTLINE.register(WorldRenderEventsTests::beforeBlockOutline);
		// Renders a translucent filled box at (0, 100, 0)
		WorldRenderEvents.BEFORE_TRANSLUCENT.register(WorldRenderEventsTests::renderBeforeTranslucent);
	}

	@Override
	public void runTest(ClientGameTestContext context) {
		WorldRenderEvents.AFTER_BLOCK_OUTLINE_EXTRACTION.register((renderContext, hitResult) -> assertExtractionContext(renderContext));
		WorldRenderEvents.END_EXTRACTION.register(WorldRenderEventsTests::assertExtractionContext);
		WorldRenderEvents.START_MAIN.register(WorldRenderEventsTests::assertTerrainRenderContext);
		WorldRenderEvents.BEFORE_ENTITIES.register(WorldRenderEventsTests::assertRenderContext);
		WorldRenderEvents.AFTER_ENTITIES.register(WorldRenderEventsTests::assertRenderContext);
		WorldRenderEvents.BEFORE_DEBUG_RENDER.register(WorldRenderEventsTests::assertRenderContext);
		WorldRenderEvents.BEFORE_TRANSLUCENT.register(WorldRenderEventsTests::assertRenderContext);
		WorldRenderEvents.END_MAIN.register(WorldRenderEventsTests::assertRenderContext);

		try (TestSingleplayerContext singleplayer = context.worldBuilder().create()) {
			// Set up the test world
			singleplayer.getServer().runCommand("/setblock 0 99 -3 minecraft:stone");
			singleplayer.getServer().runCommand("/tp @a 0 100 -3");
			singleplayer.getServer().runCommand("/setblock 0 101 0 minecraft:diamond_block");
			singleplayer.getClientWorld().waitForChunksRender();

			context.assertScreenshotEquals(TestScreenshotComparisonOptions.of("world_render_events_block_outline_and_after_translucent").withRegion(356, 98, 142, 238).save());
		}
	}

	private static void assertExtractionContext(WorldExtractionContext context) {
		assertAbstractRenderContext(context);
		assertNotNull(context.world(), "world is null");
		assertNotNull(context.camera(), "camera is null");
		assertNotNull(context.frustum(), "frustum is null");
		assertNotNull(context.tickCounter(), "tickCounter is null");
		assertNotNull(context.viewMatrix(), "viewMatrix is null");
		assertNotNull(context.cullProjectionMatrix(), "cullProjectionMatrix is null");
	}

	private static void assertRenderContext(WorldRenderContext context) {
		assertTerrainRenderContext(context);
		assertNotNull(context.commandQueue(), "commandQueue is null");
		assertNotNull(context.matrices(), "matrices is null");
		assertNotNull(context.consumers(), "consumers is null");
	}

	private static void assertTerrainRenderContext(WorldTerrainRenderContext context) {
		assertNotNull(context.sectionState(), "sectionRenderState is null");
	}

	private static void assertAbstractRenderContext(AbstractWorldRenderContext context) {
		assertNotNull(context.gameRenderer(), "gameRenderer is null");
		assertNotNull(context.worldRenderer(), "worldRenderer is null");
		assertNotNull(context.worldState(), "worldRenderState is null");
	}

	private static void assertNotNull(Object object, String message) {
		if (object == null) {
			throw new AssertionError(message);
		}
	}

	private static void assertNull(Object object, String message) {
		if (object != null) {
			throw new AssertionError(message);
		}
	}
}
