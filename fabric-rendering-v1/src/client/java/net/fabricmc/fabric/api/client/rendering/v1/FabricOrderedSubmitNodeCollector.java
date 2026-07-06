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

import net.minecraft.client.renderer.OrderedSubmitNodeCollector;
import net.minecraft.client.renderer.feature.FeatureRendererType;
import net.minecraft.client.renderer.feature.submit.SubmitNode;
import net.minecraft.client.renderer.feature.submit.TranslucentSubmit;

/**
 * General purpose Fabric extensions to the {@link OrderedSubmitNodeCollector} class.
 *
 * <p>Note: This interface is automatically implemented on all render pipelines via Mixin and interface injection.
 */
public interface FabricOrderedSubmitNodeCollector {
	/**
	 * Submit an arbitrary {@link SubmitNode} with a custom {@link FeatureRendererType}.
	 *
	 * @param phase The phase this node will be rendered with.
	 * @param node  The node to render.
	 * @param <T>   The kind of node we're rendering, either a {@link SubmitNode} or
	 *              {@link TranslucentSubmit}.
	 * @see SubmitRenderPhases Vanilla's built-in render phases.
	 * @see FeatureRendererRegistry Registry for custom feature renderers.
	 */
	default <T extends SubmitNode> void submitCustom(SubmitRenderPhase<T> phase, T node) {
		throw new UnsupportedOperationException("Implemented via mixin");
	}
}
