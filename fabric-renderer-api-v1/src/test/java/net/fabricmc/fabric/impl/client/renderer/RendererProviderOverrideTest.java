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
import java.util.List;

import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;

public final class RendererProviderOverrideTest {
	@Test
	void testOrder() {
		// if it crashes, that means the ordering is #*@!ed up.
		// fabric-renderer-indigo, g, h, i, j
		RendererManager.RendererProviderNode g = new RendererManager.RendererProviderNode("g", null);
		RendererManager.RendererProviderNode h = new RendererManager.RendererProviderNode("h", null);
		RendererManager.RendererProviderNode j = new RendererManager.RendererProviderNode("j", null);
		RendererManager.RendererProviderNode i0 = new RendererManager.RendererProviderNode("i", null);
		RendererManager.RendererProviderNode indigo = new RendererManager.RendererProviderNode("fabric-renderer-indigo", null);
		RendererManager.nodeMap.put("g", g);
		RendererManager.nodeMap.put("h", h);
		RendererManager.nodeMap.put("j", j);
		RendererManager.nodeMap.put("i", i0);
		RendererManager.nodeMap.put("fabric-renderer-indigo", indigo);
		RendererManager.overrides.put("g", List.of("j"));
		RendererManager.overrides.put("h", List.of());
		RendererManager.overrides.put("j", List.of("h"));
		RendererManager.overrides.put("i", List.of());
		RendererManager.overrides.put("fabric-renderer-indigo", List.of());
		RendererManager.sortOverrides();

		// Print the ordering
		StringBuilder stringBuilder = new StringBuilder("Renderer ordering: ");

		for (int i = 0; i < RendererManager.nodes.size(); i++) {
			RendererManager.RendererProviderNode node = RendererManager.nodes.get(i);

			if (i > 0) {
				stringBuilder.append(", ");
			}

			stringBuilder.append(node.id);
		}

		LoggerFactory.getLogger(RendererProviderOverrideTest.class).info(stringBuilder.toString());

		List<String> overrides = new ArrayList<>();

		for (RendererManager.RendererProviderNode node : RendererManager.nodes) {
			overrides.add(node.id);
		}

		if (!overrides.equals(List.of("fabric-renderer-indigo", "g", "i", "j", "h"))) {
			throw new IllegalStateException("RendererProvider overrides were not ordered correctly");
		}
	}
}
