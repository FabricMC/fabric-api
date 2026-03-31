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

package net.fabricmc.fabric.api.client.renderer.v1.render;

import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;

public final class ChunkSectionLayerHelper {
	private ChunkSectionLayerHelper() {
	}

	public static RenderType getMovingBlockRenderType(ChunkSectionLayer layer) {
		return switch (layer) {
		case SOLID -> RenderTypes.solidMovingBlock();
		case CUTOUT -> RenderTypes.cutoutMovingBlock();
		case TRANSLUCENT -> RenderTypes.translucentMovingBlock();
		};
	}

	public static RenderType getRenderType(boolean translucent) {
		return translucent ? Sheets.translucentBlockSheet() : Sheets.cutoutBlockSheet();
	}
}
