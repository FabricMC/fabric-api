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

import java.util.Objects;

import net.fabricmc.fabric.impl.client.rendering.RenderStateDataExtractionRegistryImpl;

/**
 * Allows registering {@link RenderStateDataExtractor} which are used as an entrypoint for
 * extracting render state data from a given subject. The extracted data is automatically applied
 * to the {@linkplain RenderStateDataKey render state data key} associated with the extractor.
 */
public final class RenderStateDataExtractionRegistry {
	/**
	 * Assigns the given {@link RenderStateDataExtractor} to the given renderer class.
	 */
	public static <R, S, T> void register(Class<R> rendererClass, RenderStateDataExtractor<S, T> extractor) {
		Objects.requireNonNull(rendererClass, "rendererClass");
		Objects.requireNonNull(extractor, "extractor");
		RenderStateDataExtractionRegistryImpl.register(rendererClass, extractor);
	}
}
