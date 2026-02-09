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

package net.fabricmc.fabric.test.environment.attribute.client;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.attribute.EnvironmentAttributes;
import net.minecraft.world.level.Level;

import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.fabricmc.fabric.api.environment.attribute.v0.AttributeLayerPosition;
import net.fabricmc.fabric.api.environment.attribute.v0.EnvironmentAttributeEvents;
import net.fabricmc.fabric.test.environment.attribute.FabricEnvironmentAttributesTest;

public class FabricEnvironmentAttributesClientTest implements FabricClientGameTest {
	public static final int TEST_COLOR = 0xFFFF00FF;

	@Override
	public void runTest(ClientGameTestContext context) {
		EnvironmentAttributeEvents.insertLayersEvent(AttributeLayerPosition.BEFORE_ALL).register((systemBuilder, level) -> {
			// Test color is not overridden in any way, we should see the layer
			systemBuilder.addConstantLayer(FabricEnvironmentAttributesTest.TEST_COLOR, base -> TEST_COLOR);

			// Cloud color is overridden in overworld dimension, we should not see it
			systemBuilder.addConstantLayer(EnvironmentAttributes.CLOUD_COLOR, base -> TEST_COLOR);
		});

		EnvironmentAttributeEvents.insertLayersEvent(AttributeLayerPosition.AFTER_ALL).register((systemBuilder, level) -> {
			systemBuilder.addConstantLayer(EnvironmentAttributes.SKY_COLOR, base -> TEST_COLOR);
		});

		try (TestSingleplayerContext spContext = context.worldBuilder().create()) {
			spContext.getServer().runOnServer(server -> {
				ServerLevel overworld = server.getLevel(Level.OVERWORLD);
				int testColor = overworld.environmentAttributes().getValue(FabricEnvironmentAttributesTest.TEST_COLOR, BlockPos.ZERO);
				int cloudColor = overworld.environmentAttributes().getValue(EnvironmentAttributes.CLOUD_COLOR, BlockPos.ZERO);
				int skyColor = overworld.environmentAttributes().getValue(EnvironmentAttributes.SKY_COLOR, BlockPos.ZERO);

				if (testColor != TEST_COLOR) {
					throw new AssertionError("Expected test color to be (%d) but was (%d)".formatted(TEST_COLOR, testColor));
				}

				if (cloudColor == TEST_COLOR) {
					throw new AssertionError("Expected cloud color to not be (%d), but it was".formatted(TEST_COLOR));
				}

				if (skyColor != TEST_COLOR) {
					throw new AssertionError("Expected sky color to be (%d) but was (%d)".formatted(TEST_COLOR, skyColor));
				}
			});
		}
	}
}
