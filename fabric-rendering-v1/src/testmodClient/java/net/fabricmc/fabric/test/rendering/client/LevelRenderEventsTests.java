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

import java.util.Optional;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.renderpearl.api.commands.CommandEncoder;
import com.mojang.renderpearl.api.commands.RenderPass;
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
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelExtractionEvents;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelTerrainRenderContext;

public class LevelRenderEventsTests implements ClientModInitializer, FabricClientGameTest {
	private static final RenderStateDataKey<Boolean> DIAMOND_BLOCK_OUTLINE = RenderStateDataKey.create(() -> "fabric api test mod block outline diamond block");
	@Nullable
	private static BlockModelResolver blockModelResolver = null;
	private static int mainPassStage = 0;
	private static int completedMainPasses = 0;

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
			context.submitNodeCollector().submitCustomGeometry(poseStack, RenderTypes.debugFilledBox(), (pose, buffer) -> {
				TestRenderUtils.drawFilledBox(pose, buffer, box, green);
			});
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
		context.submitNodeCollector().submitCustomGeometry(context.poseStack(), RenderTypes.debugFilledBox(), (pose, buffer) -> {
			TestRenderUtils.drawFilledBox(pose, buffer, box, color);
		});

		context.poseStack().popPose();
	}

	/**
	 * Opens and closes an empty render pass on the main render target, like a mod that needs its own render pass would.
	 * It draws nothing, but shows up under its label in a graphics debugger such as RenderDoc.
	 */
	private static void openEmptyRenderPass(AbstractLevelRenderContext context, String label) {
		RenderTarget mainTarget = context.gameRenderer().mainRenderTarget();
		CommandEncoder encoder = RenderSystem.getDevice().createCommandEncoder();

		try (RenderPass renderPass = encoder.createRenderPass(() -> label, mainTarget.getColorTextureView(), Optional.empty())) {
			renderPass.pushDebugGroup(() -> label);
			renderPass.popDebugGroup();
		}
	}

	/**
	 * Checks that the main pass events are called in the order
	 * {@code BEFORE_RENDER_PASS}, {@code START_MAIN}, {@code AFTER_RENDER_PASS}, {@code END_MAIN}, once each per frame.
	 */
	private static void advanceMainPassStage(int expectedStage, String event) {
		if (mainPassStage != expectedStage) {
			throw new AssertionError(event + " was called out of order: expected stage " + expectedStage + ", but was at stage " + mainPassStage);
		}

		mainPassStage = (mainPassStage + 1) % 4;

		if (mainPassStage == 0) {
			completedMainPasses++;
		}
	}

	@Override
	public void onInitializeClient() {
		// Renders a diamond block above diamond blocks when they are looked at.
		LevelExtractionEvents.AFTER_BLOCK_OUTLINE_EXTRACTION.register(
				LevelRenderEventsTests::extractBlockOutline);
		LevelRenderEvents.BEFORE_BLOCK_OUTLINE.register(LevelRenderEventsTests::beforeBlockOutline);
		// Renders a translucent filled box at (0, 100, 0)
		LevelRenderEvents.BEFORE_TRANSLUCENT_TERRAIN.register(LevelRenderEventsTests::renderBeforeTranslucent);
		// Opens a render pass of its own right before and right after the main render pass.
		LevelRenderEvents.BEFORE_RENDER_PASS.register(context -> openEmptyRenderPass(context, "Fabric API test: before main render pass"));
		LevelRenderEvents.AFTER_RENDER_PASS.register(context -> openEmptyRenderPass(context, "Fabric API test: after main render pass"));
	}

	@Override
	public void runTest(ClientGameTestContext context) {
		LevelExtractionEvents.AFTER_BLOCK_OUTLINE_EXTRACTION.register((renderContext, hitResult) -> assertExtractionContext(renderContext));
		LevelExtractionEvents.END_EXTRACTION.register(LevelRenderEventsTests::assertExtractionContext);
		LevelRenderEvents.BEFORE_RENDER_PASS.register(LevelRenderEventsTests::assertTerrainRenderContext);
		LevelRenderEvents.START_MAIN.register(LevelRenderEventsTests::assertTerrainRenderContext);
		LevelRenderEvents.AFTER_OPAQUE_TERRAIN.register(LevelRenderEventsTests::assertTerrainRenderContext);
		LevelRenderEvents.COLLECT_SUBMITS.register(LevelRenderEventsTests::assertRenderContext);
		LevelRenderEvents.AFTER_SOLID_FEATURES.register(LevelRenderEventsTests::assertRenderContextWithTerrain);
		LevelRenderEvents.AFTER_TRANSLUCENT_FEATURES.register(LevelRenderEventsTests::assertRenderContextWithTerrain);
		LevelRenderEvents.BEFORE_GIZMOS.register(LevelRenderEventsTests::assertRenderContext);
		LevelRenderEvents.BEFORE_TRANSLUCENT_TERRAIN.register(LevelRenderEventsTests::assertRenderContextWithTerrain);
		LevelRenderEvents.AFTER_TRANSLUCENT_TERRAIN.register(LevelRenderEventsTests::assertRenderContextWithTerrain);
		LevelRenderEvents.END_MAIN.register(LevelRenderEventsTests::assertRenderContextWithTerrain);
		LevelRenderEvents.AFTER_RENDER_PASS.register(LevelRenderEventsTests::assertRenderContextWithTerrain);
		LevelRenderEvents.BEFORE_RENDER_PASS.register(renderContext -> advanceMainPassStage(0, "BEFORE_RENDER_PASS"));
		LevelRenderEvents.START_MAIN.register(renderContext -> advanceMainPassStage(1, "START_MAIN"));
		LevelRenderEvents.AFTER_RENDER_PASS.register(renderContext -> advanceMainPassStage(2, "AFTER_RENDER_PASS"));
		LevelRenderEvents.END_MAIN.register(renderContext -> advanceMainPassStage(3, "END_MAIN"));

		try (TestSingleplayerContext singleplayer = context.worldBuilder().create()) {
			// Set up the test world
			singleplayer.getServer().runCommand("/setblock 0 99 -3 minecraft:stone");
			singleplayer.getServer().runCommand("/tp @a 0 100 -3");
			singleplayer.getServer().runCommand("/setblock 0 101 0 minecraft:diamond_block");
			singleplayer.getConnection().waitForChunksRender();
			context.waitTicks(10);
			context.assertScreenshotEquals(TestScreenshotComparisonOptions.of("level_render_events_block_outline_and_after_translucent").withRegion(356, 98, 142, 238).save());

			if (context.computeOnClient(_ -> completedMainPasses) == 0) {
				throw new AssertionError("No frame called BEFORE_RENDER_PASS, START_MAIN, AFTER_RENDER_PASS and END_MAIN");
			}
		}
	}

	private static void assertExtractionContext(LevelExtractionContext context) {
		assertAbstractRenderContext(context);
		assertNotNull(context.level(), "level is null");
		assertNotNull(context.camera(), "camera is null");
		assertNotNull(context.deltaTracker(), "deltaTracker is null");
	}

	private static void assertRenderContext(LevelRenderContext context) {
		assertNotNull(context.submitNodeCollector(), "submitNodeCollector is null");
		assertNotNull(context.poseStack(), "poseStack is null");
	}

	private static void assertRenderContextWithTerrain(LevelRenderContext context) {
		assertRenderContext(context);
		assertTerrainRenderContext(context);
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
