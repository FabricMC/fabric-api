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

import java.util.function.Supplier;

import net.minecraft.client.renderer.feature.FeatureRenderer;
import net.minecraft.client.renderer.feature.FeatureRendererType;
import net.minecraft.client.renderer.feature.submit.SubmitNode;

import net.fabricmc.fabric.impl.client.rendering.FeatureRendererRegistryImpl;

/**
 * The registry for custom {@link FeatureRenderer}s. Custom feature renderers must be registered
 * during client initialization for them to be usable with
 * {@link FabricOrderedSubmitNodeCollector#submitCustom(SubmitRenderPhase, SubmitNode)}.
 */
public final class FeatureRendererRegistry {
	private FeatureRendererRegistry() {
	}

	/**
	 * Register a custom {@link FeatureRenderer} for the given {@link FeatureRendererType}.
	 *
	 * @param type     The {@link FeatureRendererType} to register a renderer for.
	 * @param renderer A factory to create the new feature renderer.
	 * @param <T>      The type of node to render.
	 * @see FabricOrderedSubmitNodeCollector#submitCustom(SubmitRenderPhase, SubmitNode)
	 * @see SubmitNode#featureType()
	 */
	public static <T extends SubmitNode> void register(FeatureRendererType<T> type, Supplier<FeatureRenderer<T>> renderer) {
		FeatureRendererRegistryImpl.register(type, renderer);
	}
}
