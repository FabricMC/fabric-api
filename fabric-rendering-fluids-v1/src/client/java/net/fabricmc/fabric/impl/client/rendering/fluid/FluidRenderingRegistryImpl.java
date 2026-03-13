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

package net.fabricmc.fabric.impl.client.rendering.fluid;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Objects;

import it.unimi.dsi.fastutil.objects.Object2BooleanMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanOpenHashMap;
import org.jspecify.annotations.Nullable;

import net.minecraft.client.renderer.block.FluidModel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HalfTransparentBlock;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.material.Fluid;

import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandler;

public final class FluidRenderingRegistryImpl {
	private static final Map<Fluid, FluidRenderHandler> HANDLERS = new IdentityHashMap<>();
	private static final Map<Fluid, FluidModel.Unbaked> MODELS = new IdentityHashMap<>();
	private static final Object2BooleanMap<Block> TRANSPARENCY_FOR_OVERLAY = new Object2BooleanOpenHashMap<>();
	private static final FluidRenderHandler DEFAULT_RENDER_HANDLER = new FluidRenderHandler() { };

	private FluidRenderingRegistryImpl() {
	}

	public static FluidRenderHandler get(Fluid fluid) {
		return HANDLERS.getOrDefault(fluid, DEFAULT_RENDER_HANDLER);
	}

	@Nullable
	public static FluidRenderHandler getOverride(Fluid fluid) {
		return HANDLERS.get(fluid);
	}

	public static void register(Fluid fluid, FluidModel.Unbaked model, FluidRenderHandler renderer) {
		Objects.requireNonNull(fluid, "fluid cannot be null");
		Objects.requireNonNull(model, "model cannot be null");
		Objects.requireNonNull(renderer, "renderer cannot be null");

		HANDLERS.put(fluid, renderer);
		MODELS.put(fluid, model);
	}

	public static void register(Fluid fluid, FluidModel.Unbaked model) {
		Objects.requireNonNull(fluid, "fluid cannot be null");
		Objects.requireNonNull(model, "model cannot be null");

		MODELS.put(fluid, model);
	}

	public static void setBlockTransparency(Block block, boolean transparent) {
		TRANSPARENCY_FOR_OVERLAY.put(block, transparent);
	}

	public static boolean isBlockTransparent(Block block) {
		return TRANSPARENCY_FOR_OVERLAY.getOrDefault(block, block instanceof HalfTransparentBlock || block instanceof LeavesBlock);
	}

	public static Map<Fluid, FluidModel.Unbaked> getUnbakedModels() {
		return Collections.unmodifiableMap(MODELS);
	}
}
