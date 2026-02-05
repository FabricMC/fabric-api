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

package net.fabricmc.fabric.api.client.render.fluid.v1;

import org.jspecify.annotations.Nullable;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.TransparentBlock;
import net.minecraft.world.level.material.Fluid;

import net.fabricmc.fabric.impl.client.rendering.fluid.FluidRenderHandlerRegistryImpl;

/**
 * Registry for {@link FluidRenderHandler} instances.
 *
 * <p>Notably, this supports querying, overriding and wrapping vanilla fluid
 * rendering.
 */
public interface FluidRenderHandlerRegistry {
	FluidRenderHandlerRegistry INSTANCE = new FluidRenderHandlerRegistryImpl();

	/**
	 * Get a {@link FluidRenderHandler} for a given Fluid. Supports vanilla and
	 * Fabric fluids.
	 *
	 * @param fluid The Fluid.
	 * @return The FluidRenderHandler.
	 */
	@Nullable
	FluidRenderHandler get(Fluid fluid);

	/**
	 * Get a {@link FluidRenderHandler} for a given Fluid, if it is not the
	 * default implementation. Supports vanilla and Fabric fluids.
	 *
	 * @param fluid The Fluid.
	 * @return The FluidRenderHandler.
	 */
	@Nullable
	FluidRenderHandler getOverride(Fluid fluid);

	/**
	 * Register a {@link FluidRenderHandler} for a given Fluid.
	 *
	 * <p>Note that most fluids have a still and a flowing type, and a
	 * FluidRenderHandler must be registered for each type separately. To easily
	 * register a render handler for a pair of still and flowing fluids, use
	 * {@link #register(Fluid, Fluid, FluidRenderHandler)}.
	 *
	 * @param fluid The Fluid.
	 * @param renderer The FluidRenderHandler.
	 */
	void register(Fluid fluid, FluidRenderHandler renderer);

	/**
	 * Register a {@link FluidRenderHandler} for two given Fluids, usually a
	 * pair of a still and a flowing fluid type that use the same fluid
	 * renderer.
	 *
	 * @param still The still Fluid.
	 * @param flow The flowing Fluid.
	 * @param renderer The FluidRenderHandler.
	 */
	default void register(Fluid still, Fluid flow, FluidRenderHandler renderer) {
		register(still, renderer);
		register(flow, renderer);
	}

	/**
	 * Registers whether a block is transparent or not. When a block is
	 * transparent, the flowing fluid texture to the sides of that block is
	 * replaced by a special overlay texture. This happens by default with glass
	 * and leaves, and hence blocks inheriting {@link TransparentBlock} and
	 * {@link LeavesBlock} are by default transparent. Use this method to
	 * override the default behavior for a block.
	 *
	 * @param block The block to register transparency for.
	 * @param transparent Whether the block is transparent (e.g. gets the
	 * overlay textures) or not.
	 */
	void setBlockTransparency(Block block, boolean transparent);

	/**
	 * Looks up whether a block is transparent and gets a fluid overlay texture
	 * instead of a falling fluid texture. If transparency is registered for a
	 * block (via {@link #setBlockTransparency}), this method returns that
	 * registered transparency. Otherwise, this method returns whether the block
	 * is a subclass of {@link TransparentBlock} or {@link LeavesBlock}.
	 *
	 * @param block The block to get transparency for.
	 * @return Whether the block is transparent (e.g. gets the overlay textures)
	 * or not.
	 */
	boolean isBlockTransparent(Block block);
}
