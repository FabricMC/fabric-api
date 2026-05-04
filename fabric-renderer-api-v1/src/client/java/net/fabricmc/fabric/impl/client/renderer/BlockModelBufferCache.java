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

package net.fabricmc.fabric.impl.client.renderer;

import com.mojang.blaze3d.vertex.VertexConsumer;
import org.jspecify.annotations.Nullable;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.OutlineBufferSource;
import net.minecraft.client.renderer.rendertype.RenderType;

public class BlockModelBufferCache {
	private final MultiBufferSource.BufferSource bufferSource;
	private final OutlineBufferSource outlineBufferSource;

	private int outlineColor;

	@Nullable
	private RenderType lastRenderType;
	@Nullable
	private VertexConsumer lastBuffer;
	@Nullable
	private VertexConsumer lastOutlineBuffer;

	public BlockModelBufferCache(MultiBufferSource.BufferSource bufferSource, OutlineBufferSource outlineBufferSource) {
		this.bufferSource = bufferSource;
		this.outlineBufferSource = outlineBufferSource;
	}

	public void outlineColor(int outlineColor) {
		this.outlineColor = outlineColor;
		lastRenderType = null;
	}

	public VertexConsumer getBuffer(RenderType renderType) {
		if (renderType != lastRenderType) {
			update(renderType);
		}

		return lastBuffer;
	}

	@Nullable
	public VertexConsumer getOutlineBuffer(RenderType renderType) {
		if (renderType != lastRenderType) {
			update(renderType);
		}

		return lastOutlineBuffer;
	}

	private void update(RenderType renderType) {
		lastRenderType = renderType;
		lastBuffer = bufferSource.getBuffer(renderType);

		if (outlineColor != 0) {
			outlineBufferSource.setColor(outlineColor);
			lastOutlineBuffer = outlineBufferSource.getBuffer(renderType);
		} else {
			lastOutlineBuffer = null;
		}
	}
}
