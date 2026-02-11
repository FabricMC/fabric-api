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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jetbrains.annotations.VisibleForTesting;

import net.fabricmc.fabric.api.client.renderer.v1.Renderer;
import net.fabricmc.fabric.api.client.renderer.v1.RendererProvider;
import net.fabricmc.fabric.impl.base.toposort.NodeSorting;
import net.fabricmc.fabric.impl.base.toposort.SortableNode;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.entrypoint.EntrypointContainer;

public final class RendererManager {
	@VisibleForTesting
	public static List<RendererProviderNode> nodes = new ArrayList<>();
	@VisibleForTesting
	public static final Map<String, EntrypointContainer<RendererProvider>> ENTRYPOINTS = new HashMap<>();
	@VisibleForTesting
	public static final Map<String, RendererProviderNode> NODE_MAP = new HashMap<>();
	@VisibleForTesting
	public static final Map<String, Collection<String>> OVERRIDES = new HashMap<>();
	private static EntrypointContainer<RendererProvider> chosenRendererProvider;
	private static Renderer activeRenderer;

	private RendererManager() {
	}

	public static Renderer getRenderer() {
		if (activeRenderer != null) {
			return activeRenderer;
		}

		activeRenderer = getOrLoadRendererProvider().getEntrypoint().getRenderer();
		return activeRenderer;
	}

	public static EntrypointContainer<RendererProvider> getOrLoadRendererProvider() {
		if (chosenRendererProvider != null) {
			return chosenRendererProvider;
		}

		List<EntrypointContainer<RendererProvider>> entrypoints = FabricLoader.getInstance()
				.getEntrypointContainers("fabric-renderer-api-v1:renderer_provider", RendererProvider.class);

		// Collect orderings
		for (EntrypointContainer<RendererProvider> next : entrypoints) {
			String id = next.getProvider().getMetadata().getId();
			RendererManager.ENTRYPOINTS.put(id, next);
			RendererProviderNode node = new RendererProviderNode(id, next.getEntrypoint());
			NODE_MAP.put(id, node);
			OVERRIDES.put(id, node.rendererProvider.getOverrides());
		}

		sortOverrides();

		if (!nodes.isEmpty()) {
			EntrypointContainer<RendererProvider> rendererProvider = RendererManager.ENTRYPOINTS.get(nodes.getFirst().id);
			chosenRendererProvider = rendererProvider;
			return rendererProvider;
		} else {
			throw new NullPointerException("A renderer plug-in has not been provided before Minecraft has loaded. This is unsupported.");
		}
	}

	@VisibleForTesting
	public static void sortOverrides() {
		// Sort orderings
		for (Map.Entry<String, Collection<String>> entry : OVERRIDES.entrySet()) {
			RendererProviderNode providerNode = NODE_MAP.get(entry.getKey());

			for (String overrideId : entry.getValue()) {
				RendererProviderNode overrideNode = NODE_MAP.get(overrideId);

				if (overrideNode == null) {
					continue;
				}

				RendererProviderNode.link(providerNode, overrideNode);
			}

			nodes = new ArrayList<>(NODE_MAP.values());
			NodeSorting.sort(nodes, "RendererProvider", Comparator.comparing(RendererProviderNode::getDescription));
		}
	}

	@VisibleForTesting
	public static class RendererProviderNode extends SortableNode<RendererProviderNode> {
		public final String id;
		public final RendererProvider rendererProvider;

		public RendererProviderNode(String id, RendererProvider rendererProvider) {
			this.id = id;
			this.rendererProvider = rendererProvider;
		}

		@Override
		protected String getDescription() {
			return id;
		}
	}
}
