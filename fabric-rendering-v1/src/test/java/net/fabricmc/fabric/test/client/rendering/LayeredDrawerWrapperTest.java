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

package net.fabricmc.fabric.test.client.rendering;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.LayeredDrawer;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;

import net.fabricmc.fabric.api.client.rendering.v1.IdentifiedLayer;
import net.fabricmc.fabric.api.client.rendering.v1.LayeredDrawerWrapper;
import net.fabricmc.fabric.impl.client.rendering.LayeredDrawerWrapperImpl;

public class LayeredDrawerWrapperTest {
	private List<String> drawnLayers;
	private LayeredDrawer base;
	private LayeredDrawerWrapper layers;

	@BeforeEach
	void setUp() {
		drawnLayers = new ArrayList<>();
		base = new LayeredDrawer();
		layers = new LayeredDrawerWrapperImpl(base);
	}

	@Test
	void addLayer() {
		layers.addLayer(testLayer("layer1"))
				.addLayer(testLayer("layer2"))
				.addLayer(testLayer("layer3"));

		assertOrder(base, List.of("layer1", "layer2", "layer3"));
	}

	@Test
	void addBefore() {
		layers.addLayer(testLayer("layer1"))
				.addLayer(testLayer("layer2"));

		layers.addLayerBefore(Identifier.of("test", "layer1"), testLayer("before1"));

		assertOrder(base, List.of("before1", "layer1", "layer2"));
	}

	@Test
	void addAfter() {
		layers.addLayer(testLayer("layer1"))
				.addLayer(testLayer("layer2"));

		layers.addLayerAfter(Identifier.of("test", "layer1"), testLayer("after1"));

		assertOrder(base, List.of("layer1", "after1", "layer2"));
	}

	@Test
	void removeLayer() {
		layers.addLayer(testLayer("layer1"))
				.addLayer(testLayer("layer2"))
				.addLayer(testLayer("layer3"))
				.addLayer(testLayer("layer4"));

		layers.removeLayer(Identifier.of("test", "layer2"));
		layers.removeLayer(Identifier.of("test", "layer4"));

		assertOrder(base, List.of("layer1", "layer3"));
	}

	@Test
	void replaceLayer() {
		layers.addLayer(testLayer("layer1"))
				.addLayer(testLayer("layer2"))
				.addLayer(testLayer("layer3"));

		layers.replaceLayer(Identifier.of("test", "layer2"), layer -> testLayer("replaced"));

		assertOrder(base, List.of("layer1", "replaced", "layer3"));
	}

	private IdentifiedLayer testLayer(String name) {
		return IdentifiedLayer.of(Identifier.of("test", name), (context, tickCounter) -> drawnLayers.add(name));
	}

	private void assertOrder(LayeredDrawer drawer, List<String> expectedLayers) {
		DrawContext drawContext = mock(DrawContext.class);
		RenderTickCounter tickCounter = mock(RenderTickCounter.class);
		MatrixStack matrixStack = mock(MatrixStack.class);

		when(drawContext.getMatrices()).thenReturn(matrixStack);

		drawnLayers.clear();
		drawer.render(drawContext, tickCounter);
		assertEquals(drawnLayers, expectedLayers);
	}
}
