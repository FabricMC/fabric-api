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

import net.minecraft.world.level.ColorResolver;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.ColorResolverRegistry;
import net.fabricmc.fabric.test.rendering.CustomColorResolverTestInit;

public class CustomColorResolverTest implements ClientModInitializer {
	public static final ColorResolver TEST_COLOR_RESOLVER = (biome, x, z) -> {
		if (biome.hasPrecipitation()) {
			return 0xFFFF00FF;
		} else {
			return 0xFFFFFF00;
		}
	};

	@Override
	public void onInitializeClient() {
		ColorResolverRegistry.register(TEST_COLOR_RESOLVER);

		ColorProviderRegistry.BLOCK.register((state, world, pos, tintIndex) -> {
			if (world != null && pos != null) {
				return world.getBlockTint(pos, TEST_COLOR_RESOLVER);
			} else {
				return -1;
			}
		}, CustomColorResolverTestInit.CUSTOM_COLOR_BLOCK);
	}
}
