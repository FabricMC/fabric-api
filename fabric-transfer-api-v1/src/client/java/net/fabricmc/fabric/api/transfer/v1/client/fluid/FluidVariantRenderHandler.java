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

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.BlockAndTintGetter;

import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandler;
import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandlerRegistry;
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
	default void appendTooltip(FluidVariant fluidVariant, List<Component> tooltip, TooltipFlag tooltipType) {
	}

	/**
	 * Return an array of size at least 2 containing the sprites that should be used to render the passed fluid variant,
	 * for use in baked models, (block) entity renderers, or user interfaces.
	 * The first sprite in the array is the still sprite, and the second is the flowing sprite.
	 *
	 * <p>Null may be returned if the fluid variant should not be rendered, but if an array is returned it must have at least two entries and
	 * they may not be null.
	 */
	@Nullable
	default TextureAtlasSprite[] getSprites(FluidVariant fluidVariant) {
		// Use the fluid render handler by default.
		FluidRenderHandler fluidRenderHandler = FluidRenderHandlerRegistry.INSTANCE.get(fluidVariant.getFluid());

		if (fluidRenderHandler != null) {
			return fluidRenderHandler.getFluidSprites(null, null, fluidVariant.getFluid().defaultFluidState());
		} else {
			return null;
		}
	}

	/**
	 * Return the color to use when rendering {@linkplain #getSprites the sprites} of this fluid variant.
	 * Transparency (alpha) will generally be taken into account and should be specified as well.
	 *
	 * <p>The world and position are optional context parameters and may be {@code null}.
	 * If they are null, this method must return a location-independent color.
	 * If they are provided, this method may return a color that depends on the location.
	 * For example, water returns the biome-dependent color if the context parameters are specified, or its default color if one of them is null.
	 */
	default int getColor(FluidVariant fluidVariant, @Nullable BlockAndTintGetter view, @Nullable BlockPos pos) {
		// Use the fluid render handler by default.
		FluidRenderHandler fluidRenderHandler = FluidRenderHandlerRegistry.INSTANCE.get(fluidVariant.getFluid());

		if (fluidRenderHandler != null) {
			return fluidRenderHandler.getFluidColor(view, pos, fluidVariant.getFluid().defaultFluidState()) | 255 << 24;
		} else {
			return -1;
		}
	}
}
