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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.Blocks;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.LivingEntityFeatureRendererRegistrationCallback;

public final class FeatureRendererTest implements ClientModInitializer {
	private static final Logger LOGGER = LoggerFactory.getLogger(FeatureRendererTest.class);
	private int playerRegistrations = 0;

	@Override
	public void onInitializeClient() {
		LOGGER.info("Registering feature renderer tests");
		LivingEntityFeatureRendererRegistrationCallback.EVENT.register((entityType, entityRenderer, registrationHelper, context) -> {
			// minecraft:player SHOULD be printed twice
			LOGGER.info(String.format("Received registration for %s", BuiltInRegistries.ENTITY_TYPE.getKey(entityType)));

			if (entityType == EntityType.PLAYER) {
				this.playerRegistrations++;
			}

			if (entityRenderer instanceof AvatarRenderer) {
				registrationHelper.register(new TestPlayerFeatureRenderer((AvatarRenderer) entityRenderer));
			}
		});

		// FIXME: Add AfterResourceReload event to client so this can be tested.
		//  This is due to a change in 20w45a which now means this is called after the client is initialized.
		/*ClientLifecycleEvents.CLIENT_STARTED.register(client -> {
			LOGGER.info("Client is starting");

			if (this.playerRegistrations != 2) {
				throw new AssertionError(String.format("Expected 2 entity feature renderer registration events for \"minecraft:player\" but received %s registrations", this.playerRegistrations));
			}

			LOGGER.info("Successfully called feature renderer registration events");
		});*/
	}

	private static class TestPlayerFeatureRenderer extends RenderLayer<AvatarRenderState, PlayerModel> {
		TestPlayerFeatureRenderer(RenderLayerParent<AvatarRenderState, PlayerModel> featureRendererContext) {
			super(featureRendererContext);
		}

		@Override
		public void submit(PoseStack matrices, SubmitNodeCollector commandQueue, int light, AvatarRenderState state, float limbAngle, float limbDistance) {
			matrices.pushPose();

			// Translate to center above the player's head
			matrices.translate(-0.5F, -state.boundingBoxHeight + 0.25F, -0.5F);
			// Render a diamond block above the player's head
			commandQueue.order(0).submitBlock(matrices, Blocks.DIAMOND_BLOCK.defaultBlockState(), light, OverlayTexture.NO_OVERLAY, 0);

			matrices.popPose();
		}
	}
}
