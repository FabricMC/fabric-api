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

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;

public class TestFluids {
	public static final String MOD_ID = "fabric-rendering-fluids-v1-testmod";
	public static final ResourceKey<Block> NO_OVERLAY_KEY = block("no_overlay");
	public static final NoOverlayFluid NO_OVERLAY = Registry.register(BuiltInRegistries.FLUID, NO_OVERLAY_KEY.identifier(), new NoOverlayFluid.Still());
	public static final NoOverlayFluid NO_OVERLAY_FLOWING = Registry.register(BuiltInRegistries.FLUID, id("no_overlay_flowing"), new NoOverlayFluid.Flowing());

	public static final LiquidBlock NO_OVERLAY_BLOCK = Registry.register(BuiltInRegistries.BLOCK, NO_OVERLAY_KEY, new LiquidBlock(NO_OVERLAY, BlockBehaviour.Properties.ofFullCopy(Blocks.WATER).setId(NO_OVERLAY_KEY)) {
	});

	public static final ResourceKey<Block> OVERLAY_KEY = block("overlay");
	public static final OverlayFluid OVERLAY = Registry.register(BuiltInRegistries.FLUID, OVERLAY_KEY.identifier(), new OverlayFluid.Still());
	public static final OverlayFluid OVERLAY_FLOWING = Registry.register(BuiltInRegistries.FLUID, id("overlay_flowing"), new OverlayFluid.Flowing());

	public static final LiquidBlock OVERLAY_BLOCK = Registry.register(BuiltInRegistries.BLOCK, OVERLAY_KEY, new LiquidBlock(OVERLAY, BlockBehaviour.Properties.ofFullCopy(Blocks.WATER).setId(OVERLAY_KEY)) {
	});

	public static final ResourceKey<Block> UNREGISTERED_KEY = block("unregistered");
	public static final UnregisteredFluid UNREGISTERED = Registry.register(BuiltInRegistries.FLUID, UNREGISTERED_KEY.identifier(), new UnregisteredFluid.Still());
	public static final UnregisteredFluid UNREGISTERED_FLOWING = Registry.register(BuiltInRegistries.FLUID, id("unregistered_flowing"), new UnregisteredFluid.Flowing());

	public static final LiquidBlock UNREGISTERED_BLOCK = Registry.register(BuiltInRegistries.BLOCK, UNREGISTERED_KEY, new LiquidBlock(UNREGISTERED, BlockBehaviour.Properties.ofFullCopy(Blocks.WATER).setId(UNREGISTERED_KEY)) {
	});

	public static final ResourceKey<Block> CUSTOM_KEY = block("custom");
	public static final CustomFluid CUSTOM = Registry.register(BuiltInRegistries.FLUID, CUSTOM_KEY.identifier(), new CustomFluid.Still());
	public static final CustomFluid CUSTOM_FLOWING = Registry.register(BuiltInRegistries.FLUID, id("custom_flowing"), new CustomFluid.Flowing());

	public static final LiquidBlock CUSTOM_BLOCK = Registry.register(BuiltInRegistries.BLOCK, CUSTOM_KEY, new LiquidBlock(CUSTOM, BlockBehaviour.Properties.ofFullCopy(Blocks.WATER).setId(CUSTOM_KEY)) {
	});

	private static Identifier id(String path) {
		return Identifier.fromNamespaceAndPath(MOD_ID, path);
	}

	private static ResourceKey<Block> block(String path) {
		return ResourceKey.create(Registries.BLOCK, id(path));
	}
}
