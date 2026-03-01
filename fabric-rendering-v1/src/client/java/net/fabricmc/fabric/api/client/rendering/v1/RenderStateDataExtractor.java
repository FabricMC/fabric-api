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

/**
 * The wrapper class for a {@link RenderStateDataExtractorCallback}. This class links the callback
 * to a specific subject class as well as a {@link RenderStateDataKey} to automically apply the
 * extracted data to.
 */
public final class RenderStateDataExtractor<S, T> {
	private final Class<S> subjectClass;
	private final RenderStateDataKey<T> key;
	private final RenderStateDataExtractorCallback<S, T> callback;

	private RenderStateDataExtractor(Class<S> subjectClass, RenderStateDataKey<T> key, RenderStateDataExtractorCallback<S, T> callback) {
		this.subjectClass = subjectClass;
		this.key = key;
		this.callback = callback;
	}

	/**
	 * Creates a new extractor.
	 * @param subjectClass The type of class that the callback will be extracting from.
	 * @param key The render state data key to apply the extracted data to.
	 * @param callback The callback in charge of extracting the data from an instance of the
	 *                 subject class.
	 * @param <T> The type of data that the callback will extract from the subject and apply to
	 *            the key.
	 * @param <S> The class of the subject.
	 * @return The newly created data key.
	 */
	public static <S, T> RenderStateDataExtractor<S, T> create(Class<S> subjectClass, RenderStateDataKey<T> key, RenderStateDataExtractorCallback<S, T> callback) {
		return new RenderStateDataExtractor<>(subjectClass, key, callback);
	}

	public RenderStateDataExtractorCallback<S, T> getCallback() {
		return callback;
	}

	public Class<S> getSubjectClass() {
		return subjectClass;
	}

	public RenderStateDataKey<T> getKey() {
		return key;
	}
}
