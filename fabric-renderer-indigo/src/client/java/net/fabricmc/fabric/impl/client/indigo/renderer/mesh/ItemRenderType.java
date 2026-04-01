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

package net.fabricmc.fabric.impl.client.indigo.renderer.mesh;

import java.util.Arrays;
import java.util.Map;

import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.rendertype.RenderType;

import net.fabricmc.fabric.api.client.renderer.v1.mesh.MutableQuadView;

/**
 * Allowed values for {@link MutableQuadView#itemRenderType(RenderType)}.
 */
enum ItemRenderType {
	CUTOUT(Sheets.cutoutItemSheet()),
	TRANSLUCENT(Sheets.translucentItemSheet()),
	CUTOUT_BLOCK(Sheets.cutoutBlockItemSheet()),
	TRANSLUCENT_BLOCK(Sheets.translucentBlockItemSheet());

	static final RenderType[] RENDER_TYPES = Arrays.stream(ItemRenderType.values()).map(t -> t.renderType).toArray(RenderType[]::new);
	static final Map<RenderType, ItemRenderType> RENDER_TYPE_2_ENUM;

	static {
		RENDER_TYPE_2_ENUM = Map.of(
				CUTOUT.renderType, CUTOUT,
				TRANSLUCENT.renderType, TRANSLUCENT,
				CUTOUT_BLOCK.renderType, CUTOUT_BLOCK,
				TRANSLUCENT_BLOCK.renderType, TRANSLUCENT_BLOCK
		);
	}

	// The atlas of the default render type should match the default QuadAtlas, which is currently BLOCK.
	static final ItemRenderType DEFAULT = ItemRenderType.CUTOUT_BLOCK;

	final RenderType renderType;

	ItemRenderType(RenderType renderType) {
		this.renderType = renderType;
	}
}
