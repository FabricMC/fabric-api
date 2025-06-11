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

package net.fabricmc.fabric.impl.client.rendering.hud;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.joml.Matrix3x2fStack;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.util.Identifier;

import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElement;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;

public class HudElementRegistryTest {
	private final List<String> drawnLayers = new ArrayList<>();

	@SuppressWarnings("MisorderedAssertEqualsArguments")
	@Test
	void assertVanillaIds() {
		// Make sure HudElementRegistryImpl.VANILLA_ELEMENT_IDS is correct and in sync with VanillaHudElements
		Assertions.assertEquals(Arrays.stream(VanillaHudElements.class.getDeclaredFields()).map(f -> {
			try {
				return f.get(null);
			} catch (IllegalAccessException e) {
				throw new RuntimeException("Failed to access VanillaHudElements field: " + f.getName(), e);
			}
		}).toList(), HudElementRegistryImpl.VANILLA_ELEMENT_IDS);
	}

	@Test
	void addLayer() {
		HudElementRegistry.addLast(testIdentifier("layer1"), testElement("layer1"));
		HudElementRegistry.addLast(testIdentifier("layer2"), testElement("layer2"));
		HudElementRegistry.addLast(testIdentifier("layer3"), testElement("layer3"));

		assertOrder(List.of("layer1", "layer2", "layer3"));
	}

	@Test
	void addBefore() {
		HudElementRegistry.addLast(testIdentifier("layer1"), testElement("layer1"));
		HudElementRegistry.addLast(testIdentifier("layer2"), testElement("layer2"));

		HudElementRegistry.attachElementBefore(testIdentifier("layer1"), testIdentifier("before1"), testElement("before1"));

		assertOrder(List.of("before1", "layer1", "layer2"));
	}

	@Test
	void addAfter() {
		HudElementRegistry.addLast(testIdentifier("layer1"), testElement("layer1"));
		HudElementRegistry.addLast(testIdentifier("layer2"), testElement("layer2"));

		HudElementRegistry.attachElementAfter(testIdentifier("layer1"), testIdentifier("after1"), testElement("after1"));

		assertOrder(List.of("layer1", "after1", "layer2"));
	}

	@Test
	void removeLayer() {
		HudElementRegistry.addLast(testIdentifier("layer1"), testElement("layer1"));
		HudElementRegistry.addLast(testIdentifier("layer2"), testElement("layer2"));
		HudElementRegistry.addLast(testIdentifier("layer3"), testElement("layer3"));
		HudElementRegistry.addLast(testIdentifier("layer4"), testElement("layer4"));

		HudElementRegistry.removeElement(testIdentifier("layer2"));
		HudElementRegistry.removeElement(testIdentifier("layer4"));

		assertOrder(List.of("layer1", "layer3"));
	}

	@Test
	void replaceLayer() {
		HudElementRegistry.addLast(testIdentifier("layer1"), testElement("layer1"));
		HudElementRegistry.addLast(testIdentifier("layer2"), testElement("layer2"));
		HudElementRegistry.addLast(testIdentifier("layer3"), testElement("layer3"));

		HudElementRegistry.replaceElement(testIdentifier("layer2"), layer -> testElement("replaced"));

		assertOrder(List.of("layer1", "replaced", "layer3"));
	}

	@Test
	void replaceVanillaLayer() {
		HudElementRegistry.replaceElement(VanillaHudElements.CHAT, layer -> testElement("replaced"));

		assertOrder(List.of("replaced"));
	}

	@Test
	void validateUnique() {
		HudElementRegistry.addLast(testIdentifier("layer1"), testElement("layer1"));
		HudElementRegistry.addLast(testIdentifier("layer2"), testElement("layer2"));
		HudElementRegistry.addLast(testIdentifier("layer3"), testElement("layer3"));

		Assertions.assertDoesNotThrow(() -> HudElementRegistryImpl.validateUnique(testIdentifier("layer4")));
		Assertions.assertThrows(IllegalArgumentException.class, () -> HudElementRegistryImpl.validateUnique(testIdentifier("layer2")));
	}

	@Test
	void findLayer() {
		HudElementRegistry.addLast(testIdentifier("layer1"), testElement("layer1"));
		HudElementRegistry.addLast(testIdentifier("layer2"), testElement("layer2"));
		HudElementRegistry.addLast(testIdentifier("layer3"), testElement("layer3"));

		Assertions.assertTrue(HudElementRegistryImpl.findLayer(testIdentifier("layer2"), (layer, iterator) -> {
			iterator.add(HudLayer.ofElement(testIdentifier("found"), testElement("found")));
			return true;
		}));

		assertOrder(List.of("layer1", "layer2", "found", "layer3"));
	}

	@Test
	void visitLayers() {
		HudElementRegistry.addLast(testIdentifier("layer1"), testElement("layer1"));
		HudElementRegistry.addLast(testIdentifier("layer2"), testElement("layer2"));
		HudElementRegistry.addLast(testIdentifier("layer3"), testElement("layer3"));

		Assertions.assertTrue(HudElementRegistryImpl.visitLayers((layer, iterator) -> {
			// Skip vanilla layers
			if ("minecraft".equals(layer.id().getNamespace())) {
				return false;
			}

			String path = layer.id().getPath();
			String name = "visited" + path.substring(path.length() - 1);
			iterator.add(HudLayer.ofElement(testIdentifier(name), testElement(name)));
			return true;
		}));

		assertOrder(List.of("layer1", "visited1", "layer2", "visited2", "layer3", "visited3"));
	}

	private HudElement testElement(String name) {
		return (context, tickCounter) -> drawnLayers.add(name);
	}

	private Identifier testIdentifier(String name) {
		return Identifier.of("test", name);
	}

	private void assertOrder(List<String> expectedLayers) {
		DrawContext drawContext = mock(DrawContext.class);
		RenderTickCounter tickCounter = mock(RenderTickCounter.class);
		Matrix3x2fStack matrixStack = mock(Matrix3x2fStack.class);

		when(drawContext.getMatrices()).thenReturn(matrixStack);

		drawnLayers.clear();

		for (Identifier id : HudElementRegistryImpl.VANILLA_ELEMENT_IDS) {
			HudElementRegistryImpl.ROOT_ELEMENTS.get(id).render(drawContext, tickCounter, (ctx, tc) -> { });
		}

		assertEquals(expectedLayers, drawnLayers);
	}

	@AfterEach
	void cleanUpLayers() {
		HudElementRegistryImpl.visitLayers((layer, iterator) -> {
			if (!"minecraft".equals(layer.id().getNamespace())) {
				iterator.remove();
			}

			return true;
		});
	}
}
