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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.minecraft.block.Blocks;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.render.entity.state.PlayerEntityRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.EntityType;
import net.minecraft.registry.Registries;

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
			LOGGER.info(String.format("Received registration for %s", Registries.ENTITY_TYPE.getId(entityType)));

			if (entityType == EntityType.PLAYER) {
				this.playerRegistrations++;
			}

			if (entityRenderer instanceof PlayerEntityRenderer) {
				registrationHelper.register(new TestPlayerFeatureRenderer((PlayerEntityRenderer) entityRenderer));
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

	private static class TestPlayerFeatureRenderer extends FeatureRenderer<PlayerEntityRenderState, PlayerEntityModel> {
		TestPlayerFeatureRenderer(FeatureRendererContext<PlayerEntityRenderState, PlayerEntityModel> featureRendererContext) {
			super(featureRendererContext);
		}

		@Override
		public void render(MatrixStack matrices, OrderedRenderCommandQueue commandQueue, int light, PlayerEntityRenderState state, float limbAngle, float limbDistance) {
			matrices.push();

			// Translate to center above the player's head
			matrices.translate(-0.5F, -state.height + 0.25F, -0.5F);
			// Render a diamond block above the player's head
			commandQueue.getBatchingQueue(0).submitBlock(matrices, Blocks.DIAMOND_BLOCK.getDefaultState(), light, OverlayTexture.DEFAULT_UV, 0);

			matrices.pop();
		}
	}
}
