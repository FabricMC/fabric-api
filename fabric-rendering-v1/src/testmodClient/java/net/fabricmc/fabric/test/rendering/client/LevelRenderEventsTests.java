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
import org.jspecify.annotations.Nullable;

import net.minecraft.client.renderer.block.BlockModelResolver;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.BlockOutlineRenderState;
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
import net.fabricmc.fabric.api.client.rendering.v1.level.AbstractLevelRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelExtractionContext;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelTerrainRenderContext;

public class LevelRenderEventsTests implements ClientModInitializer, FabricClientGameTest {
	private static final RenderStateDataKey<Boolean> DIAMOND_BLOCK_OUTLINE = RenderStateDataKey.create(() -> "fabric api test mod block outline diamond block");
	@Nullable
	private static BlockModelResolver blockModelResolver = null;

	private static void extractBlockOutline(LevelExtractionContext context, HitResult hitResult) {
		if (hitResult instanceof BlockHitResult blockHitResult && blockHitResult.getType() != HitResult.Type.MISS && context.level().getBlockState(blockHitResult.getBlockPos()).is(Blocks.DIAMOND_BLOCK)) {
			context.levelState().blockOutlineRenderState.setData(DIAMOND_BLOCK_OUTLINE, true);
		}
	}

	private static boolean beforeBlockOutline(LevelRenderContext context, BlockOutlineRenderState outlineRenderState) {
		if (Boolean.TRUE.equals(outlineRenderState.getData(DIAMOND_BLOCK_OUTLINE))) {
			PoseStack poseStack = context.poseStack();
			poseStack.pushPose();
			Vec3 cameraPos = context.levelState().cameraRenderState.pos;
			BlockPos pos = outlineRenderState.pos();
			double x = pos.getX() - cameraPos.x;
			double y = pos.getY() - cameraPos.y;
			double z = pos.getZ() - cameraPos.z;
			poseStack.translate(x + 0.25, y + 0.25 + 1, z + 0.25);
			poseStack.scale(0.5f, 0.5f, 0.5f);
			AABB box = new AABB(0, 0, 0, 1, 1, 1);
			int green = ARGB.colorFromFloat(1.0f, 0, 1, 0);
			TestRenderUtils.drawFilledBox(poseStack, context.bufferSource().getBuffer(RenderTypes.debugFilledBox()), box, green);
			poseStack.popPose();
		}

		return true;
	}

	/**
	 * Renders a translucent filled box at (0, 100, 0).
	 */
	private static void renderBeforeTranslucent(LevelRenderContext context) {
		Vec3 camera = context.levelState().cameraRenderState.pos;

		context.poseStack().pushPose();
		context.poseStack().translate(-camera.x, -camera.y, -camera.z);

		AABB box = new AABB(BlockPos.ZERO.above(100));
		int color = ARGB.colorFromFloat(0.5f, 0, 1, 0);
		TestRenderUtils.drawFilledBox(context.poseStack(), context.bufferSource().getBuffer(RenderTypes.debugFilledBox()), box, color);

		context.poseStack().popPose();
	}

	@Override
	public void onInitializeClient() {
		// Renders a diamond block above diamond blocks when they are looked at.
		LevelRenderEvents.AFTER_BLOCK_OUTLINE_EXTRACTION.register(
				LevelRenderEventsTests::extractBlockOutline);
		LevelRenderEvents.BEFORE_BLOCK_OUTLINE.register(LevelRenderEventsTests::beforeBlockOutline);
		// Renders a translucent filled box at (0, 100, 0)
		LevelRenderEvents.BEFORE_TRANSLUCENT_TERRAIN.register(LevelRenderEventsTests::renderBeforeTranslucent);
	}

	@Override
	public void runTest(ClientGameTestContext context) {
		LevelRenderEvents.AFTER_BLOCK_OUTLINE_EXTRACTION.register((renderContext, hitResult) -> assertExtractionContext(renderContext));
		LevelRenderEvents.END_EXTRACTION.register(LevelRenderEventsTests::assertExtractionContext);
		LevelRenderEvents.START_MAIN.register(LevelRenderEventsTests::assertTerrainRenderContext);
		LevelRenderEvents.AFTER_OPAQUE_TERRAIN.register(LevelRenderEventsTests::assertTerrainRenderContext);
		LevelRenderEvents.COLLECT_SUBMITS.register(LevelRenderEventsTests::assertRenderContext);
		LevelRenderEvents.AFTER_SOLID_FEATURES.register(LevelRenderEventsTests::assertRenderContext);
		LevelRenderEvents.AFTER_TRANSLUCENT_FEATURES.register(LevelRenderEventsTests::assertRenderContext);
		LevelRenderEvents.BEFORE_GIZMOS.register(LevelRenderEventsTests::assertRenderContext);
		LevelRenderEvents.BEFORE_TRANSLUCENT_TERRAIN.register(LevelRenderEventsTests::assertRenderContext);
		LevelRenderEvents.AFTER_TRANSLUCENT_TERRAIN.register(LevelRenderEventsTests::assertRenderContext);
		LevelRenderEvents.END_MAIN.register(LevelRenderEventsTests::assertRenderContext);

		try (TestSingleplayerContext singleplayer = context.worldBuilder().create()) {
			// Set up the test world
			singleplayer.getServer().runCommand("/setblock 0 99 -3 minecraft:stone");
			singleplayer.getServer().runCommand("/tp @a 0 100 -3");
			singleplayer.getServer().runCommand("/setblock 0 101 0 minecraft:diamond_block");
			singleplayer.getClientLevel().waitForChunksRender();
			context.waitTicks(10);
			context.assertScreenshotEquals(TestScreenshotComparisonOptions.of("level_render_events_block_outline_and_after_translucent").withRegion(356, 98, 142, 238).save());
		}
	}

	private static void assertExtractionContext(LevelExtractionContext context) {
		assertAbstractRenderContext(context);
		assertNotNull(context.level(), "level is null");
		assertNotNull(context.camera(), "camera is null");
		assertNotNull(context.deltaTracker(), "deltaTracker is null");
	}

	private static void assertRenderContext(LevelRenderContext context) {
		assertTerrainRenderContext(context);
		assertNotNull(context.submitNodeCollector(), "submitNodeCollector is null");
		assertNotNull(context.poseStack(), "poseStack is null");
		assertNotNull(context.bufferSource(), "bufferSource is null");
	}

	private static void assertTerrainRenderContext(LevelTerrainRenderContext context) {
		assertNotNull(context.sectionsToRender(), "sectionsToRender is null");
	}

	private static void assertAbstractRenderContext(AbstractLevelRenderContext context) {
		assertNotNull(context.gameRenderer(), "gameRenderer is null");
		assertNotNull(context.levelRenderer(), "levelRenderer is null");
		assertNotNull(context.levelState(), "levelRenderState is null");
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
