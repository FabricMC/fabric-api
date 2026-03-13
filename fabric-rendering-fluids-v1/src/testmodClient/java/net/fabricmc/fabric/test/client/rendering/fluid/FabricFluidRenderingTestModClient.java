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

package net.fabricmc.fabric.test.client.rendering.fluid;

import net.minecraft.client.renderer.block.FluidModel;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Blocks;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderingRegistry;

public class FabricFluidRenderingTestModClient implements ClientModInitializer {
	private static final FluidModel.Unbaked NO_OVERLAY_MODEL = new FluidModel.Unbaked(
			new Material(Identifier.fromNamespaceAndPath("fabric-rendering-fluids-v1-testmod", "block/test_fluid_still")),
			new Material(Identifier.fromNamespaceAndPath("fabric-rendering-fluids-v1-testmod", "block/test_fluid_flowing")),
			null,
			_ -> 0xFF5555
	);
	private static final FluidModel.Unbaked OVERLAY_MODEL = new FluidModel.Unbaked(
			new Material(Identifier.fromNamespaceAndPath("fabric-rendering-fluids-v1-testmod", "block/test_fluid_still")),
			new Material(Identifier.fromNamespaceAndPath("fabric-rendering-fluids-v1-testmod", "block/test_fluid_flowing")),
			new Material(Identifier.fromNamespaceAndPath("fabric-rendering-fluids-v1-testmod", "block/test_fluid_overlay")),
			_ -> 0xFF5555
	);
	private static final FluidModel.Unbaked CUSTOM_MODEL = new FluidModel.Unbaked(
			new Material(Identifier.fromNamespaceAndPath("fabric-rendering-fluids-v1-testmod", "block/test_fluid_overlay")),
			new Material(Identifier.fromNamespaceAndPath("fabric-rendering-fluids-v1-testmod", "block/test_fluid_overlay")),
			new Material(Identifier.fromNamespaceAndPath("fabric-rendering-fluids-v1-testmod", "block/test_fluid_overlay")),
			null
	);

	@Override
	public void onInitializeClient() {
		// Doors now will have overlay textures to the side
		FluidRenderingRegistry.setBlockTransparency(Blocks.ACACIA_DOOR, true);
		FluidRenderingRegistry.setBlockTransparency(Blocks.DARK_OAK_DOOR, true);
		FluidRenderingRegistry.setBlockTransparency(Blocks.BIRCH_DOOR, true);
		FluidRenderingRegistry.setBlockTransparency(Blocks.CRIMSON_DOOR, true);
		FluidRenderingRegistry.setBlockTransparency(Blocks.IRON_DOOR, true);
		FluidRenderingRegistry.setBlockTransparency(Blocks.JUNGLE_DOOR, true);
		FluidRenderingRegistry.setBlockTransparency(Blocks.OAK_DOOR, true);
		FluidRenderingRegistry.setBlockTransparency(Blocks.SPRUCE_DOOR, true);
		FluidRenderingRegistry.setBlockTransparency(Blocks.WARPED_DOOR, true);

		// Red stained glass will have falling fluid textures to the side
		FluidRenderingRegistry.setBlockTransparency(Blocks.RED_STAINED_GLASS, false);

		FluidRenderingRegistry.register(TestFluids.NO_OVERLAY, TestFluids.NO_OVERLAY_FLOWING, NO_OVERLAY_MODEL);
		FluidRenderingRegistry.register(TestFluids.OVERLAY, TestFluids.OVERLAY_FLOWING, OVERLAY_MODEL);
		FluidRenderingRegistry.register(TestFluids.CUSTOM, TestFluids.CUSTOM_FLOWING, CUSTOM_MODEL, new CustomizedFluidRenderer());
	}
}
