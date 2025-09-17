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

package net.fabricmc.fabric.impl.client.rendering;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.render.SpecialGuiElementRenderer;
import net.minecraft.client.gui.render.state.special.SpecialGuiElementRenderState;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;

public final class SpecialGuiElementRendererPool<T extends SpecialGuiElementRenderState> implements AutoCloseable {
	private int index = 0;
	private final List<SpecialGuiElementRenderer<T>> renderers = new ArrayList<>();

	public void newFrame() {
		index = 0;
	}

	public SpecialGuiElementRenderer<T> substitute(SpecialGuiElementRenderer<T> original, T elementState, MinecraftClient client, VertexConsumerProvider.Immediate immediate, OrderedRenderCommandQueue orderedRenderCommandQueue) {
		int index = this.index++;

		if (index == 0) {
			return original;
		} else if (index <= renderers.size()) {
			return renderers.get(index - 1);
		} else {
			SpecialGuiElementRenderer<T> newRenderer = SpecialGuiElementRegistryImpl.createNewRenderer(elementState, client, immediate, orderedRenderCommandQueue);

			if (newRenderer == null) {
				// This renderer has been registered in an unofficial way (using mixins rather than through FAPI).
				// We don't have a factory to create a new renderer, so don't fix in this case.
				return original;
			}

			renderers.add(newRenderer);
			return newRenderer;
		}
	}

	public void cleanUpUnusedRenderers() {
		int firstUnusedIndex = Math.max(0, index - 1);

		if (firstUnusedIndex >= renderers.size()) {
			return;
		}

		for (int i = firstUnusedIndex; i < renderers.size(); i++) {
			renderers.get(i).close();
		}

		renderers.subList(firstUnusedIndex, renderers.size()).clear();
	}

	@Override
	public void close() {
		renderers.forEach(SpecialGuiElementRenderer::close);

		index = 0;
		renderers.clear();
	}
}
