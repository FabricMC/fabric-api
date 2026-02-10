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

import java.util.function.Function;
import java.util.function.Predicate;

import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.client.renderer.rendertype.RenderType;

import net.fabricmc.fabric.api.client.renderer.v1.render.BlockMultiBufferSource;

public class DelegatingBlockMultiBufferSourceImpl implements BlockMultiBufferSource, Predicate<ChunkSectionLayer> {
	public final boolean translucent;

	public MultiBufferSource multiBufferSource;
	public Function<ChunkSectionLayer, RenderType> renderTypeFunction;

	public DelegatingBlockMultiBufferSourceImpl(boolean translucent) {
		this.translucent = translucent;
	}

	@Override
	public VertexConsumer getBuffer(ChunkSectionLayer layer) {
		return multiBufferSource.getBuffer(renderTypeFunction.apply(layer));
	}

	@Override
	public boolean test(ChunkSectionLayer layer) {
		return renderTypeFunction.apply(layer).hasBlending() == translucent;
	}
}
