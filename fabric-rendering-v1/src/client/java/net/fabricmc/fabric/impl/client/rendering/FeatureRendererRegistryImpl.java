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
import java.util.function.Supplier;

import net.minecraft.client.renderer.feature.FeatureRenderer;
import net.minecraft.client.renderer.feature.FeatureRendererMap;
import net.minecraft.client.renderer.feature.FeatureRendererType;
import net.minecraft.client.renderer.feature.submit.SubmitNode;

public class FeatureRendererRegistryImpl {
	private static final List<FeatureRendererRegistration<?>> featureRenderers = new ArrayList<>();

	public static <T extends SubmitNode> void register(FeatureRendererType<T> type, Supplier<FeatureRenderer<T>> renderer) {
		featureRenderers.add(new FeatureRendererRegistration<>(type, renderer));
	}

	public static void registerRenderers(FeatureRendererMap map) {
		for (FeatureRendererRegistration<?> feature : featureRenderers) {
			feature.register(map);
		}
	}

	private record FeatureRendererRegistration<T extends SubmitNode>(
			FeatureRendererType<T> type, Supplier<FeatureRenderer<T>> renderer
	) {
		private void register(FeatureRendererMap map) {
			map.put(type(), renderer().get());
		}
	}
}
