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

import java.util.Optional;
import java.util.function.Supplier;

import com.mojang.blaze3d.pipeline.RenderPipeline;

public final class FabricRenderPipelineInternals {
	private static final ThreadLocal<Optional<Boolean>> SCOPED_SNIPPET_USE_PIPELINE_VERTEX_FORMAT_FOR_GUI = ThreadLocal.withInitial(Optional::empty);

	private FabricRenderPipelineInternals() {
	}

	public static RenderPipeline.Snippet withSnippetUsePipelineVertexFormatForGui(Supplier<RenderPipeline.Snippet> factory, Optional<Boolean> usePipelineVertexFormat) {
		Optional<Boolean> original = SCOPED_SNIPPET_USE_PIPELINE_VERTEX_FORMAT_FOR_GUI.get();

		try {
			SCOPED_SNIPPET_USE_PIPELINE_VERTEX_FORMAT_FOR_GUI.set(usePipelineVertexFormat);
			return factory.get();
		} finally {
			if (original.isEmpty()) {
				SCOPED_SNIPPET_USE_PIPELINE_VERTEX_FORMAT_FOR_GUI.remove();
			} else {
				SCOPED_SNIPPET_USE_PIPELINE_VERTEX_FORMAT_FOR_GUI.set(original);
			}
		}
	}

	public static Optional<Boolean> getScopedUsePipelineVertexFormatForGui() {
		return SCOPED_SNIPPET_USE_PIPELINE_VERTEX_FORMAT_FOR_GUI.get();
	}
}
