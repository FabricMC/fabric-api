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

import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Blocks;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandlerRegistry;
import net.fabricmc.fabric.api.client.render.fluid.v1.SimpleFluidRenderHandler;

public class FabricFluidRenderingTestModClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		// Doors now will have overlay textures to the side
		FluidRenderHandlerRegistry.INSTANCE.setBlockTransparency(Blocks.ACACIA_DOOR, true);
		FluidRenderHandlerRegistry.INSTANCE.setBlockTransparency(Blocks.DARK_OAK_DOOR, true);
		FluidRenderHandlerRegistry.INSTANCE.setBlockTransparency(Blocks.BIRCH_DOOR, true);
		FluidRenderHandlerRegistry.INSTANCE.setBlockTransparency(Blocks.CRIMSON_DOOR, true);
		FluidRenderHandlerRegistry.INSTANCE.setBlockTransparency(Blocks.IRON_DOOR, true);
		FluidRenderHandlerRegistry.INSTANCE.setBlockTransparency(Blocks.JUNGLE_DOOR, true);
		FluidRenderHandlerRegistry.INSTANCE.setBlockTransparency(Blocks.OAK_DOOR, true);
		FluidRenderHandlerRegistry.INSTANCE.setBlockTransparency(Blocks.SPRUCE_DOOR, true);
		FluidRenderHandlerRegistry.INSTANCE.setBlockTransparency(Blocks.WARPED_DOOR, true);

		// Red stained glass will have falling fluid textures to the side
		FluidRenderHandlerRegistry.INSTANCE.setBlockTransparency(Blocks.RED_STAINED_GLASS, false);

		FluidRenderHandlerRegistry.INSTANCE.register(TestFluids.NO_OVERLAY, TestFluids.NO_OVERLAY_FLOWING, new SimpleFluidRenderHandler(
				Identifier.fromNamespaceAndPath("fabric-rendering-fluids-v1-testmod", "block/test_fluid_still"),
				Identifier.fromNamespaceAndPath("fabric-rendering-fluids-v1-testmod", "block/test_fluid_flowing"),
				0xFF5555
		));

		FluidRenderHandlerRegistry.INSTANCE.register(TestFluids.OVERLAY, TestFluids.OVERLAY_FLOWING, new SimpleFluidRenderHandler(
				Identifier.fromNamespaceAndPath("fabric-rendering-fluids-v1-testmod", "block/test_fluid_still"),
				Identifier.fromNamespaceAndPath("fabric-rendering-fluids-v1-testmod", "block/test_fluid_flowing"),
				Identifier.fromNamespaceAndPath("fabric-rendering-fluids-v1-testmod", "block/test_fluid_overlay"),
				0x5555FF
		));

		FluidRenderHandlerRegistry.INSTANCE.register(TestFluids.CUSTOM, TestFluids.CUSTOM_FLOWING, new CustomizedFluidRenderer(
				Identifier.fromNamespaceAndPath("fabric-rendering-fluids-v1-testmod", "block/test_fluid_overlay")
		));
	}
}
