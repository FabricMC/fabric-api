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

package net.fabricmc.fabric.mixin.client.indigo.renderer;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import net.minecraft.client.render.chunk.ChunkRendererRegion;

import net.fabricmc.fabric.impl.client.indigo.renderer.accessor.AccessChunkRendererRegion;
import net.fabricmc.fabric.impl.client.indigo.renderer.render.TerrainRenderContext;

@Mixin(ChunkRendererRegion.class)
abstract class ChunkRendererRegionMixin implements AccessChunkRendererRegion {
	@Unique
	private TerrainRenderContext fabric_renderer;

	@Override
	public TerrainRenderContext fabric_getRenderer() {
		return fabric_renderer;
	}

	@Override
	public void fabric_setRenderer(TerrainRenderContext renderer) {
		fabric_renderer = renderer;
	}
}
