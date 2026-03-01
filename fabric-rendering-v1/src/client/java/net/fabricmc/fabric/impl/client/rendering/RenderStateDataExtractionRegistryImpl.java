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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.fabricmc.fabric.api.client.rendering.v1.FabricRenderState;
import net.fabricmc.fabric.api.client.rendering.v1.RenderStateDataExtractor;

public final class RenderStateDataExtractionRegistryImpl {
	private static final Map<Class<?>, List<RenderStateDataExtractor<?, ?>>> EXTRACTORS = new HashMap<>();

	public static void register(Class<?> rendererClass, RenderStateDataExtractor<?, ?> extractor) {
		EXTRACTORS.computeIfAbsent(rendererClass, _ -> new ArrayList<>()).add(extractor);
	}

	public static void processExtractors(Class<?> rendererClass, Object subject, FabricRenderState state) {
		List<RenderStateDataExtractor<?, ?>> extractors = EXTRACTORS.get(rendererClass);
		if (extractors == null) return;
		for (RenderStateDataExtractor<?, ?> extractor : extractors) {
			processExtractor(extractor, subject, state);
		}
	}

	private static <S, T> void processExtractor(RenderStateDataExtractor<S, T> extractor, Object subject, FabricRenderState state) {
		S castedSubject;
		try {
			//The user may provide a subject class type that is different from the actual
			// subject class, so a cast check is required.
			castedSubject = extractor.getSubjectClass().cast(subject);
		} catch (ClassCastException e) {
			throw new IllegalArgumentException("Subject class " + extractor.getSubjectClass() + " for extractor with " + extractor.getKey() + " is not assignable from " + subject.getClass(), e);
		}

		T extractedValue = extractor.getCallback().onExtractRenderState(castedSubject);
		state.setData(extractor.getKey(), extractedValue);
	}
}
