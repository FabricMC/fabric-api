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

package net.fabricmc.fabric.api.transfer.v1.client.fluid;

import java.util.List;

import org.jspecify.annotations.Nullable;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.FluidModel;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.FluidState;

import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;

/**
 * Defines how {@linkplain FluidVariant fluid variants} of a given Fluid should be displayed to clients.
 * Register with {@link FluidVariantRendering#register}.
 */
public interface FluidVariantRenderHandler {
	/**
	 * Append additional tooltips to the passed list if additional information is contained in the fluid variant.
	 *
	 * <p>The name of the fluid, and its identifier if the tooltip context is advanced, should not be appended.
	 * They are already added by {@link FluidVariantRendering#getTooltip}.
	 */
	default void appendTooltip(FluidVariant fluidVariant, List<Component> tooltip, TooltipFlag tooltipFlag) {
	}

	/**
	 * Return the color to use when rendering the sprites of this fluid variant.
	 * Transparency (alpha) will generally be taken into account and should be specified as well.
	 *
	 * <p>The level and position are optional context parameters and may be {@code null}.
	 * If they are null, this method must return a location-independent color.
	 * If they are provided, this method may return a color that depends on the location.
	 * For example, water returns the biome-dependent color if the context parameters are specified, or its default color if one of them is null.
	 */
	default int getColor(FluidVariant fluidVariant, @Nullable BlockAndTintGetter level, @Nullable BlockPos pos) {
		FluidState fluidState = fluidVariant.getFluid().defaultFluidState();
		FluidModel fluidModel = Minecraft.getInstance().getModelManager().getFluidStateModelSet().get(fluidState);

		if (fluidModel.tintSource() == null) {
			return -1;
		}

		if (level != null && pos != null) {
			return fluidModel.tintSource().colorInWorld(Blocks.AIR.defaultBlockState(), level, pos);
		} else {
			return fluidModel.tintSource().color(Blocks.AIR.defaultBlockState());
		}
	}
}
