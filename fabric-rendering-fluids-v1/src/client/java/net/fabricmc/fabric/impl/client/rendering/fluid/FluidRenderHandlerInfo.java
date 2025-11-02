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

import org.jspecify.annotations.Nullable;

import net.minecraft.client.texture.Sprite;
import net.minecraft.fluid.FluidState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockRenderView;

import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandler;

public class FluidRenderHandlerInfo {
	public final Sprite[] sprites = new Sprite[2];
	@Nullable
	public FluidRenderHandler handler;
	public boolean hasOverlay;
	public Sprite overlaySprite;

	public void setup(FluidRenderHandler handler, BlockRenderView world, BlockPos pos, FluidState fluidState) {
		this.handler = handler;

		Sprite[] sprites = handler.getFluidSprites(world, pos, fluidState);

		this.sprites[0] = sprites[0];
		this.sprites[1] = sprites[1];

		if (sprites.length > 2) {
			hasOverlay = true;
			overlaySprite = sprites[2];
		}
	}

	public void clear() {
		sprites[0] = null;
		sprites[1] = null;
		handler = null;
		hasOverlay = false;
		overlaySprite = null;
	}
}
