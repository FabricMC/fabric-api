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

package net.fabricmc.fabric.test.dimension;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.attribute.EnvironmentAttributes;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.BuiltinDimensionTypes;
import net.minecraft.world.level.dimension.LevelStem;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.dimension.v1.DimensionEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;

public class FabricDimensionTest implements ModInitializer {
	// The level stem refers to the JSON-file in the dimension subfolder of the data pack,
	// which will always share its ID with the level that is created from it
	private static final ResourceKey<LevelStem> DIMENSION_KEY = ResourceKey.create(Registries.LEVEL_STEM, Identifier.fromNamespaceAndPath("fabric_dimension", "void"));
	private static final int PURPLE = 0xFFE580FF;

	@Override
	public void onInitialize() {
		Registry.register(BuiltInRegistries.CHUNK_GENERATOR, Identifier.fromNamespaceAndPath("fabric_dimension", "void"), VoidChunkGenerator.CODEC);

		DimensionEvents.MODIFY_ATTRIBUTES.register((dimension, attributes, _) -> {
			if (dimension.is(BuiltinDimensionTypes.OVERWORLD)) {
				attributes.set(EnvironmentAttributes.CLOUD_COLOR, PURPLE);
			}
		});

		ServerLifecycleEvents.SERVER_STARTED.register(server -> {
			int overworldCloudColor = server.getLevel(Level.OVERWORLD).environmentAttributes().getValue(EnvironmentAttributes.CLOUD_COLOR, BlockPos.ZERO);

			if (overworldCloudColor != PURPLE) {
				throw new AssertionError("Expected overworld cloud color to be (%d) but was (%d)".formatted(PURPLE, overworldCloudColor));
			}
		});
	}
}
