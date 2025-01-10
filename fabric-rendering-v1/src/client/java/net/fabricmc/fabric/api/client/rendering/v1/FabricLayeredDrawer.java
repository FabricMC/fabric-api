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

package net.fabricmc.fabric.api.client.rendering.v1;

import java.util.function.BooleanSupplier;
import java.util.function.Function;

import net.minecraft.client.gui.LayeredDrawer;
import net.minecraft.util.Identifier;

/**
 * A layered drawer that has an identifier attached to each layer and methods to add layers in specific positions.
 */
public interface FabricLayeredDrawer {
	FabricLayeredDrawer addLayer(IdentifiedLayer layer);

	FabricLayeredDrawer addLayerBefore(Identifier before, IdentifiedLayer layer);

	default FabricLayeredDrawer addLayerBefore(Identifier before, Identifier identifier, LayeredDrawer.Layer layer) {
		return addLayerBefore(before, IdentifiedLayer.wrapping(identifier, layer));
	}

	FabricLayeredDrawer addLayerAfter(Identifier after, IdentifiedLayer layer);

	default FabricLayeredDrawer addLayerAfter(Identifier after, Identifier identifier, LayeredDrawer.Layer layer) {
		return addLayerAfter(after, IdentifiedLayer.wrapping(identifier, layer));
	}

	FabricLayeredDrawer removeLayer(Identifier identifier);

	FabricLayeredDrawer replaceLayer(Identifier identifier, Function<IdentifiedLayer, IdentifiedLayer> replacer);

	FabricLayeredDrawer addSubDrawer(Identifier identifier, LayeredDrawer drawer, BooleanSupplier shouldRender);
}
