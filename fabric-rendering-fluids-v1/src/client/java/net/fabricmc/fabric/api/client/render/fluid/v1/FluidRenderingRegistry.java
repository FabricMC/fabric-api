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

import net.minecraft.client.renderer.block.FluidModel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.TransparentBlock;
import net.minecraft.world.level.material.Fluid;

import net.fabricmc.fabric.impl.client.rendering.fluid.FluidRenderingRegistryImpl;

/**
 * Registry for {@link FluidModel} and {@link FluidRenderHandler} instances.
 *
 * <p>Notably, this supports querying, overriding and wrapping vanilla fluid
 * rendering.
 */
public final class FluidRenderingRegistry {
	private FluidRenderingRegistry() {
	}

	/**
	 * Get a {@link FluidRenderHandler} for a given Fluid.
	 *
	 * <p>Returns null if no handler is registered for the fluid.
	 *
	 * @param fluid The Fluid.
	 * @return The FluidRenderHandler.
	 */
	public static FluidRenderHandler get(Fluid fluid) {
		return FluidRenderingRegistryImpl.get(fluid);
	}

	/**
	 * Get a {@link FluidRenderHandler} for a given Fluid, if it is not the
	 * default implementation. Supports vanilla and Fabric fluids.
	 *
	 * @param fluid The Fluid.
	 * @return The FluidRenderHandler.
	 */
	@Nullable
	public static FluidRenderHandler getOverride(Fluid fluid) {
		return FluidRenderingRegistryImpl.getOverride(fluid);
	}

	/**
	 * Register a {@link FluidModel.Unbaked} and {@link FluidRenderHandler} for a given Fluid.
	 *
	 * <p>Note that most fluids have a still and a flowing type, and a
	 * FluidRenderHandler must be registered for each type separately. To easily
	 * register a render handler for a pair of still and flowing fluids, use
	 * {@link #register(Fluid, Fluid, FluidModel.Unbaked, FluidRenderHandler)}.
	 *
	 * @param fluid The Fluid.
	 * @param model The {@link FluidModel.Unbaked} to use for the fluid.
	 * @param renderer The FluidRenderHandler.
	 */
	public static void register(Fluid fluid, FluidModel.Unbaked model, FluidRenderHandler renderer) {
		FluidRenderingRegistryImpl.register(fluid, model, renderer);
	}

	/**
	 * Register a {@link FluidModel.Unbaked} for a given Fluid.
	 *
	 * <p>Note that most fluids have a still and a flowing type, and a
	 * model must be registered for each type separately. To easily
	 * register a render handler for a pair of still and flowing fluids, use
	 * {@link #register(Fluid, Fluid, FluidModel.Unbaked)}.
	 *
	 * @param fluid The Fluid.
	 * @param model The {@link FluidModel.Unbaked} to use for the fluid.
	 */
	public static void register(Fluid fluid, FluidModel.Unbaked model) {
		FluidRenderingRegistryImpl.register(fluid, model);
	}

	/**
	 * Register a {@link FluidModel.Unbaked} and {@link FluidRenderHandler} for two given Fluids, usually a
	 * pair of a still and a flowing fluid type that use the same fluid
	 * renderer.
	 *
	 * @param still The still Fluid.
	 * @param flow The flowing Fluid.
	 * @param model The {@link FluidModel.Unbaked} to use for the fluid.
	 * @param renderer The FluidRenderHandler.
	 */
	public static void register(Fluid still, Fluid flow, FluidModel.Unbaked model, FluidRenderHandler renderer) {
		register(still, model, renderer);
		register(flow, model, renderer);
	}

	/**
	 * Register a {@link FluidModel.Unbaked} for two given Fluids, usually a
	 * pair of a still and a flowing fluid type that use the same fluid
	 * renderer.
	 *
	 * @param still The still Fluid.
	 * @param flow The flowing Fluid.
	 * @param model The {@link FluidModel.Unbaked} to use for the fluid.
	 */
	public static void register(Fluid still, Fluid flow, FluidModel.Unbaked model) {
		register(still, model);
		register(flow, model);
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
	public static void setBlockTransparency(Block block, boolean transparent) {
		FluidRenderingRegistryImpl.setBlockTransparency(block, transparent);
	}

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
	public static boolean isBlockTransparent(Block block) {
		return FluidRenderingRegistryImpl.isBlockTransparent(block);
	}
}
