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

package net.fabricmc.fabric.test.dimension.client;

import java.util.Optional;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.attribute.EnvironmentAttributes;
import net.minecraft.world.clock.ClockTimeMarkers;
import net.minecraft.world.clock.WorldClock;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.BuiltinDimensionTypes;

import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.fabricmc.fabric.api.dimension.v1.DimensionEvents;

public class FabricDimensionClientTest implements FabricClientGameTest {
	public static final int PURPLE = 0xFFE580FF;

	@Override
	public void runTest(ClientGameTestContext context) {
		DimensionEvents.MODIFY_ATTRIBUTES.register((dimension, attributes, _) -> {
			if (dimension.is(BuiltinDimensionTypes.OVERWORLD)) {
				attributes.set(EnvironmentAttributes.CLOUD_COLOR, PURPLE);
			}
		});

		try (TestSingleplayerContext spContext = context.worldBuilder().create()) {
			spContext.getServer().runOnServer(server -> {
				ServerLevel overworld = server.getLevel(Level.OVERWORLD);
				Optional<Holder<WorldClock>> defaultClock = overworld.dimensionType().defaultClock();
				overworld.getServer().clockManager().moveToTimeMarker(defaultClock.get(), ClockTimeMarkers.NOON);
				int overworldCloudColor = overworld.environmentAttributes().getValue(EnvironmentAttributes.CLOUD_COLOR, BlockPos.ZERO);

				if (overworldCloudColor != PURPLE) {
					throw new AssertionError("Expected overworld cloud color to be (%d) but was (%d)".formatted(PURPLE, overworldCloudColor));
				}
			});
		}
	}
}
