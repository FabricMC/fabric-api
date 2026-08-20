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

import net.minecraft.client.renderer.block.BuiltInBlockModels;
import net.minecraft.world.level.block.Blocks;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.BuiltInBlockModelsCallback;

public class BuiltInBlockModelsTest implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		BuiltInBlockModelsCallback.EVENT.register(builder -> {
			// Overrides the yellow shulker box built-in block model with an empty one.
			// This can be tested in-game e.g., by checking out a minecart with that block.
			// summon minecraft:minecart ~ ~ ~ {DisplayState:{Name:"minecraft:yellow_shulker_box"}}
			BuiltInBlockModels.createAir(builder, Blocks.DYED_SHULKER_BOX.yellow());
		});
	}
}
