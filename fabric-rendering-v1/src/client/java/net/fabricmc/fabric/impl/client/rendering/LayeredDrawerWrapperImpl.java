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

import java.util.List;
import java.util.ListIterator;
import java.util.function.Function;

import org.apache.commons.lang3.mutable.MutableBoolean;
import org.jetbrains.annotations.VisibleForTesting;

import net.minecraft.client.gui.LayeredDrawer;
import net.minecraft.util.Identifier;

import net.fabricmc.fabric.api.client.rendering.v1.IdentifiedLayer;
import net.fabricmc.fabric.api.client.rendering.v1.LayeredDrawerWrapper;
import net.fabricmc.fabric.mixin.client.rendering.LayeredDrawerAccessor;

public final class LayeredDrawerWrapperImpl implements LayeredDrawerWrapper {
	private final LayeredDrawer base;

	public LayeredDrawerWrapperImpl(LayeredDrawer base) {
		this.base = base;
	}

	private static List<LayeredDrawer.Layer> getLayers(LayeredDrawer drawer) {
		return ((LayeredDrawerAccessor) drawer).getLayers();
	}

	@Override
	public LayeredDrawerWrapper addLayer(IdentifiedLayer layer) {
		validateUnique(layer);
		getLayers(this.base).add(layer);
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
		return visitLayers(getLayers(base), visitor);
	}

	private boolean visitLayers(List<LayeredDrawer.Layer> layers, LayerVisitor visitor) {
		MutableBoolean modified = new MutableBoolean(false);
		ListIterator<LayeredDrawer.Layer> iterator = layers.listIterator();

		while (iterator.hasNext()) {
			LayeredDrawer.Layer layer = iterator.next();

			if (visitor.visit(layer, iterator)) {
				modified.setTrue();
				continue; // Skip visiting children if the current layer was modified
			}

			if (layer instanceof SubLayer subLayer) {
				modified.setValue(visitLayers(getLayers(subLayer.delegate()), visitor));
			}
		}

		return modified.booleanValue();
	}

	private static boolean matchesIdentifier(LayeredDrawer.Layer layer, Identifier identifier) {
		return layer instanceof IdentifiedLayer il && il.id().equals(identifier);
	}

	@VisibleForTesting
	interface LayerVisitor {
		/**
		 * @return true if the list has been modified, false if not modified
		 */
		boolean visit(LayeredDrawer.Layer layer, ListIterator<LayeredDrawer.Layer> iterator);
	}
}
