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

package net.fabricmc.fabric.impl.client.rendering.state.extractors;

import java.util.HashMap;
import java.util.Map;

import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.fabricmc.fabric.api.client.rendering.v1.FabricRenderState;
import net.fabricmc.fabric.api.client.rendering.v1.RenderStateDataExtractor;
import net.fabricmc.fabric.api.client.rendering.v1.RenderStateDataExtractorCallback;
import net.fabricmc.fabric.api.client.rendering.v1.RenderStateDataKey;
import net.fabricmc.fabric.impl.client.rendering.RenderStateDataExtractionRegistryImpl;

public class RenderStateDataExtractorTest {
	private static final RenderStateDataKey<Character> DEBUG_KEY =
			RenderStateDataKey.create(() -> "DebugKey");
	private static final RenderStateDataExtractorCallback<String, Character> DEBUG_CALLBACK =
			(string) -> string.charAt(0);
	private static final RenderStateDataExtractor<String, Character> DEBUG_EXTRACTOR =
			RenderStateDataExtractor.create(String.class, DEBUG_KEY, DEBUG_CALLBACK);
	private static final Logger log = LoggerFactory.getLogger(RenderStateDataExtractorTest.class);

	@BeforeAll
	public static void beforeAll() {
		RenderStateDataExtractionRegistryImpl.register(DebugDummyClass.class, DEBUG_EXTRACTOR);
	}

	@Test
	public void test() {
		DebugRenderState renderState = new DebugRenderState();

		//test renderer matching and successful subject casting
		RenderStateDataExtractionRegistryImpl.processExtractors(DebugDummyClass.class, "test", renderState);
		Assertions.assertEquals('t', renderState.getDataOrDefault(DEBUG_KEY, 'f'));

		//test correct renderer matching
		renderState.clearExtraData();
		RenderStateDataExtractionRegistryImpl.processExtractors(DebugDummyClass2.class, "test", renderState);
		Assertions.assertEquals('f', renderState.getDataOrDefault(DEBUG_KEY, 'f'));

		//test subject casting failure
		Exception e = Assertions.assertThrowsExactly(IllegalArgumentException.class, () -> {
			// 42 is not a String
			RenderStateDataExtractionRegistryImpl.processExtractors(DebugDummyClass.class, 42, renderState);
		});
		log.error("error thrown: ", e);
	}

	private static class DebugDummyClass { }

	private static class DebugDummyClass2 { }

	private static class DebugRenderState implements FabricRenderState {
		private final Map<RenderStateDataKey<?>, Object> MAP = new HashMap<>();

		@Override
		public <T> T getDataOrDefault(RenderStateDataKey<T> key, T defaultValue) {
			//noinspection unchecked
			return (T) MAP.getOrDefault(key, defaultValue);
		}

		@Override
		public <T> void setData(RenderStateDataKey<T> key, @Nullable T value) {
			MAP.put(key, value);
		}

		@Override
		public void clearExtraData() {
			MAP.clear();
		}
	}
}
