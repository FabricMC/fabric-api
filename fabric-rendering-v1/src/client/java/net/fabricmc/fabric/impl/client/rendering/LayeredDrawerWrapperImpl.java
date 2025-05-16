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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.SequencedMap;
import java.util.function.Function;
import java.util.stream.Collectors;

import net.fabricmc.fabric.api.client.rendering.v1.HudLayer;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;

import org.apache.commons.lang3.mutable.MutableBoolean;
import org.jetbrains.annotations.VisibleForTesting;

import net.minecraft.util.Identifier;

import net.fabricmc.fabric.api.client.rendering.v1.IdentifiedLayer;
import net.fabricmc.fabric.api.client.rendering.v1.LayeredDrawerWrapper;

public final class LayeredDrawerWrapperImpl implements LayeredDrawerWrapper {
	private final SequencedMap<Identifier, VanillaLayer> vanillaLayers;

	public LayeredDrawerWrapperImpl(List<VanillaLayer> vanillaLayers) {
		this.vanillaLayers = vanillaLayers.stream().collect(Collectors.toMap(VanillaLayer::id, Function.identity(), (a, b) -> a, LinkedHashMap::new));
	}

	public VanillaLayer getVanillaLayer(Identifier id) {
		return vanillaLayers.get(id);
	}

	@Override
	public LayeredDrawerWrapper addLayer(IdentifiedLayer layer) {
		validateUnique(layer);
		vanillaLayers.lastEntry().getValue().layers().add(layer);
		return this;
	}

	@Override
	public LayeredDrawerWrapper attachLayerAfter(Identifier afterThis, IdentifiedLayer layer) {
		validateUnique(layer);

		boolean didChange = findLayer(afterThis, (l, iterator) -> {
			iterator.add(layer);
			return true;
		});

		if (!didChange) {
			throw new IllegalArgumentException("Layer with identifier " + afterThis + " not found");
		}

		return this;
	}

	@Override
	public LayeredDrawerWrapper attachLayerBefore(Identifier beforeThis, IdentifiedLayer layer) {
		validateUnique(layer);
		boolean didChange = findLayer(beforeThis, (l, iterator) -> {
			iterator.previous();
			iterator.add(layer);
			iterator.next();
			return true;
		});

		if (!didChange) {
			throw new IllegalArgumentException("Layer with identifier " + beforeThis + " not found");
		}

		return this;
	}

	@Override
	public LayeredDrawerWrapper removeLayer(Identifier identifier) {
		boolean didChange = findLayer(identifier, (l, iterator) -> {
			iterator.remove();
			return true;
		});

		if (!didChange) {
			throw new IllegalArgumentException("Layer with identifier " + identifier + " not found");
		}

		return this;
	}

	@Override
	public LayeredDrawerWrapper replaceLayer(Identifier identifier, Function<IdentifiedLayer, IdentifiedLayer> replacer) {
		boolean didChange = findLayer(identifier, (l, iterator) -> {
			iterator.set(replacer.apply((IdentifiedLayer) l));
			return true;
		});

		if (!didChange) {
			throw new IllegalArgumentException("Layer with identifier " + identifier + " not found");
		}

		return this;
	}

	@VisibleForTesting
	void validateUnique(IdentifiedLayer layer) {
		visitLayers((l, iterator) -> {
			if (matchesIdentifier(l, layer.id())) {
				throw new IllegalArgumentException("Layer with identifier " + layer.id() + " already exists");
			}

			return false;
		});
	}

	/**
	 * @return true if a layer with the given identifier was found
	 */
	@VisibleForTesting
	boolean findLayer(Identifier identifier, LayerVisitor visitor) {
		MutableBoolean found = new MutableBoolean(false);

		visitLayers((l, iterator) -> {
			if (matchesIdentifier(l, identifier)) {
				found.setTrue();
				return visitor.visit(l, iterator);
			}

			return false;
		});

		return found.booleanValue();
	}

	@VisibleForTesting
	boolean visitLayers(LayerVisitor visitor) {
		for (VanillaLayer vanillaLayer : vanillaLayers.sequencedValues()) {
			if (visitLayers(vanillaLayer.layers(), visitor)) {
				return true;
			}
		}
		return false;
	}

	private boolean visitLayers(List<HudLayer> layers, LayerVisitor visitor) {
		MutableBoolean modified = new MutableBoolean(false);
		ListIterator<HudLayer> iterator = layers.listIterator();

		while (iterator.hasNext()) {
			HudLayer layer = iterator.next();

			if (visitor.visit(layer, iterator)) {
				modified.setTrue();
			}
		}

		return modified.booleanValue();
	}

	private static boolean matchesIdentifier(HudLayer layer, Identifier identifier) {
		return layer instanceof IdentifiedLayer il && il.id().equals(identifier);
	}

	@VisibleForTesting
	interface LayerVisitor {
		/**
		 * @return true if the list has been modified, false if not modified
		 */
		boolean visit(HudLayer layer, ListIterator<HudLayer> iterator);
	}

	@VisibleForTesting
	public record VanillaLayer(Identifier id, List<HudLayer> layers) implements IdentifiedLayer {
		public VanillaLayer(Identifier id, HudLayer vanillaLayer) {
			this(id, new ArrayList<>());
			layers().add(IdentifiedLayer.of(id, vanillaLayer));
		}

		@Override
		public void render(DrawContext context, RenderTickCounter tickCounter) {
			for (HudLayer layer : layers) {
				layer.render(context, tickCounter);
			}
		}
	}
}
