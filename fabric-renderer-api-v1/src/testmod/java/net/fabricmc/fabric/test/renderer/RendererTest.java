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

package net.fabricmc.fabric.test.renderer;

import net.minecraft.resources.Identifier;

import net.fabricmc.api.ModInitializer;

/**
 * The testmod for the Fabric Renderer API. These tests are used to validate that
 * Indigo's implementation is correct, but they may also be useful for other
 * implementations of the Fabric Renderer API.
 *
 * <h3>Tests</h3>
 *
 * <ul>
 *     <li>Frame blocks display another block inside, scaled down and made translucent.
 *     Blocks that provide a block entity cannot be placed inside frames.
 *
 *     <li>Pillars connect vertically with each other by changing textures. They also
 *     connect vertically to frame blocks containing a pillar, and vice versa.
 *
 *     <li>Octagonal columns have irregular faces to test enhanced AO and normal shade. The
 *     octagonal item column has glint force enabled on all faces except the top and bottom
 *     faces.
 *
 *     <li>Riverstone blocks look like stone normally, but turn to gold in river biomes
 *     (biomes tagged with #minecraft:is_river).
 * </ul>
 */
public final class RendererTest implements ModInitializer {
	@Override
	public void onInitialize() {
		Registration.init();
	}

	public static Identifier id(String path) {
		return Identifier.fromNamespaceAndPath("fabric-renderer-api-v1-testmod", path);
	}
}
