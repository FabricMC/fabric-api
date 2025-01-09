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
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BooleanSupplier;
import java.util.function.Function;

import net.minecraft.client.gui.LayeredDrawer;
import net.minecraft.util.Identifier;

import net.fabricmc.fabric.api.client.rendering.v1.FabricLayeredDrawer;
import net.fabricmc.fabric.api.client.rendering.v1.IdentifiedLayer;
import net.fabricmc.fabric.mixin.client.rendering.LayeredDrawerAccessor;

public class FabricLayeredDrawerImpl implements FabricLayeredDrawer {
	private final LayeredDrawer base;

	public FabricLayeredDrawerImpl(LayeredDrawer base) {
		this.base = base;
	}

	@Override
	public FabricLayeredDrawer addLayer(IdentifiedLayer layer) {
		validateUnique(layer);
		getLayers(this.base).add(layer);
		return this;
	}

	@Override
	public FabricLayeredDrawer addLayerAfter(IdentifiedLayer layer, Identifier identifier) {
		validateUnique(layer);

		boolean didChange = findLayer(identifier, (l, iterator) -> {
			iterator.add(layer);
			return true;
		});

		if (!didChange) {
			throw new IllegalArgumentException("Layer with identifier " + identifier + " not found");
		}

		return this;
	}

	@Override
	public FabricLayeredDrawer addLayerBefore(IdentifiedLayer layer, Identifier identifier) {
		validateUnique(layer);
		boolean didChange = findLayer(identifier, (l, iterator) -> {
			iterator.previous();
			iterator.add(layer);
			iterator.next();
			return true;
		});

		if (!didChange) {
			throw new IllegalArgumentException("Layer with identifier " + identifier + " not found");
		}

		return this;
	}

	@Override
	public FabricLayeredDrawer removeLayer(Identifier identifier) {
		boolean didChange = findLayer(identifier, (l, iterator) -> {
			iterator.remove();
			return false;
		});

		if (!didChange) {
			throw new IllegalArgumentException("Layer with identifier " + identifier + " not found");
		}

		return this;
	}

	@Override
	public FabricLayeredDrawer replaceLayer(Identifier identifier, Function<IdentifiedLayer, IdentifiedLayer> replacer) {
		boolean didChange = findLayer(identifier, (l, iterator) -> {
			iterator.remove();
			iterator.add(replacer.apply((IdentifiedLayer) l));
			return false;
		});

		if (!didChange) {
			throw new IllegalArgumentException("Layer with identifier " + identifier + " not found");
		}

		return this;
	}

	@Override
	public FabricLayeredDrawer addSubDrawer(Identifier identifier, LayeredDrawer drawer, BooleanSupplier shouldRender) {
		addLayer(new SubLayer(identifier, drawer, shouldRender));
		return this;
	}

	private void validateUnique(IdentifiedLayer layer) {
		visitLayers((l, iterator) -> {
			if (matchesIdentifier(l, layer.id())) {
				throw new IllegalArgumentException("Layer with identifier " + layer.id() + " already exists");
			}

			return true;
		});
	}

	private boolean matchesIdentifier(LayeredDrawer.Layer layer, Identifier identifier) {
		return layer instanceof IdentifiedLayer il && il.id().equals(identifier);
	}

	private void visitLayers(LayerVisitor visitor) {
		visitLayers(getLayers(base), visitor);
	}

	private void visitLayers(List<LayeredDrawer.Layer> layers, LayerVisitor visitor) {
		ListIterator<LayeredDrawer.Layer> iterator = layers.listIterator();

		while (iterator.hasNext()) {
			LayeredDrawer.Layer layer = iterator.next();

			if (!visitor.visit(layer, iterator)) {
				iterator.remove();
				continue;
			}

			if (layer instanceof SubLayer subLayer) {
				visitLayers(getLayers(subLayer.delegate()), visitor);
			}
		}
	}

	private boolean findLayer(Identifier identifier, LayerVisitor visitor) {
		AtomicBoolean didFind = new AtomicBoolean(false);

		visitLayers((l, iterator) -> {
			if (l instanceof IdentifiedLayer il && il.id().equals(identifier)) {
				didFind.set(true);
				visitor.visit(l, iterator);
			}

			return true;
		});

		return didFind.get();
	}

	private static List<LayeredDrawer.Layer> getLayers(LayeredDrawer drawer) {
		return ((LayeredDrawerAccessor) drawer).getLayers();
	}

	private interface LayerVisitor {
		// When returns false remove the layer
		boolean visit(LayeredDrawer.Layer layer, ListIterator<LayeredDrawer.Layer> iterator);
	}
}
