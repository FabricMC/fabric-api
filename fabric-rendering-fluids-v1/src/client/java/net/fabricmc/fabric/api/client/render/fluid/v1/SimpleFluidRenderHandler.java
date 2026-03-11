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

import java.util.Objects;

import org.jspecify.annotations.Nullable;

import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.SpriteGetter;
import net.minecraft.client.resources.model.SpriteId;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.material.FluidState;

/**
 * A simple fluid render handler that uses and loads sprites given by their
 * identifiers. Most fluids don't need more than this. In fact, if a fluid just
 * needs the vanilla water texture with a custom color, {@link #coloredWater}
 * can be used to easily create a fluid render handler for that.
 *
 * <p>Note that it's assumed that the fluid textures are assumed to be
 * registered to the blocks sprite atlas. If they are not, you have to manually
 * register the fluid textures. The "fabric-textures" API may come in handy for
 * that.
 */
public class SimpleFluidRenderHandler implements FluidRenderHandler {
	protected final SpriteId stillTexture;
	protected final SpriteId flowingTexture;
	protected final SpriteId overlayTexture;

	protected final TextureAtlasSprite[] sprites;

	protected final int tint;

	/**
	 * Creates a fluid render handler with an overlay texture and a custom,
	 * fixed tint.
	 *
	 * @param stillTexture The texture for still fluid.
	 * @param flowingTexture The texture for flowing/falling fluid.
	 * @param overlayTexture The texture behind glass, leaves and other
	 * {@linkplain FluidRenderHandlerRegistry#setBlockTransparency registered
	 * transparent blocks}.
	 * @param tint The fluid color RGB. Alpha is ignored.
	 */
	public SimpleFluidRenderHandler(SpriteId stillTexture, SpriteId flowingTexture, @Nullable SpriteId overlayTexture, int tint) {
		this.stillTexture = Objects.requireNonNull(stillTexture, "stillTexture");
		this.flowingTexture = Objects.requireNonNull(flowingTexture, "flowingTexture");
		this.overlayTexture = overlayTexture;
		this.sprites = new TextureAtlasSprite[overlayTexture == null ? 2 : 3];
		this.tint = tint;
	}

	/**
	 * Creates a fluid render handler with an overlay texture and no tint.
	 *
	 * @param stillTexture The texture for still fluid.
	 * @param flowingTexture The texture for flowing/falling fluid.
	 * @param overlayTexture The texture behind glass, leaves and other
	 * {@linkplain FluidRenderHandlerRegistry#setBlockTransparency registered
	 * transparent blocks}.
	 */
	public SimpleFluidRenderHandler(SpriteId stillTexture, SpriteId flowingTexture, SpriteId overlayTexture) {
		this(stillTexture, flowingTexture, overlayTexture, -1);
	}

	/**
	 * Creates a fluid render handler without an overlay texture and a custom,
	 * fixed tint.
	 *
	 * @param stillTexture The texture for still fluid.
	 * @param flowingTexture The texture for flowing/falling fluid.
	 * @param tint The fluid color RGB. Alpha is ignored.
	 */
	public SimpleFluidRenderHandler(SpriteId stillTexture, SpriteId flowingTexture, int tint) {
		this(stillTexture, flowingTexture, null, tint);
	}

	/**
	 * Creates a fluid render handler without an overlay texture and no tint.
	 *
	 * @param stillTexture The texture for still fluid.
	 * @param flowingTexture The texture for flowing/falling fluid.
	 */
	public SimpleFluidRenderHandler(SpriteId stillTexture, SpriteId flowingTexture) {
		this(stillTexture, flowingTexture, null, -1);
	}

	/**
	 * Creates a fluid render handler that uses the vanilla water texture with a
	 * fixed, custom color.
	 *
	 * @param tint The fluid color RGB. Alpha is ignored.
	 * @see	ModelBakery#WATER_STILL
	 * @see	ModelBakery#WATER_FLOW
	 * @see ModelBakery#WATER_OVERLAY
	 */
	public static SimpleFluidRenderHandler coloredWater(int tint) {
		return new SimpleFluidRenderHandler(ModelBakery.WATER_STILL, ModelBakery.WATER_FLOW, ModelBakery.WATER_OVERLAY, tint);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public TextureAtlasSprite[] getFluidSprites(@Nullable BlockAndTintGetter level, @Nullable BlockPos pos, FluidState state) {
		return sprites;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ChunkSectionLayer reloadTextures(SpriteGetter spriteGetter) {
		sprites[0] = spriteGetter.get(stillTexture);
		sprites[1] = spriteGetter.get(flowingTexture);

		if (overlayTexture != null) {
			sprites[2] = spriteGetter.get(overlayTexture);
		}

		return ChunkSectionLayer.byTransparency(sprites[0].transparency().or(sprites[1].transparency()));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int getFluidColor(@Nullable BlockAndTintGetter level, @Nullable BlockPos pos, FluidState state) {
		return tint;
	}
}
