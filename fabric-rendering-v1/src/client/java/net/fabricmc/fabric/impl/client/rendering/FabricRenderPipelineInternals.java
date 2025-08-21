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

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;

import com.mojang.blaze3d.pipeline.RenderPipeline;

import net.minecraft.util.Unit;

public final class FabricRenderPipelineInternals {
	private static final Map<RenderPipeline.Snippet, Optional<Boolean>> extraData = Collections.synchronizedMap(new IdentityHashMap<>());
	public static BiConsumer<RenderPipeline, Boolean> usePipelineDrawModeForGuiSetter;
	public static ThreadLocal<Unit> MIXIN_PIPELINE_MODIFICATION_REC_GUARD = ThreadLocal.withInitial(() -> null);

	private FabricRenderPipelineInternals() {
	}

	public static RenderPipeline.Snippet createSnippetWithPipelineVertexFormatForGui(RenderPipeline.Snippet base, Optional<Boolean> usePipelineVertexFormat) {
		Unit original = MIXIN_PIPELINE_MODIFICATION_REC_GUARD.get();

		try {
			MIXIN_PIPELINE_MODIFICATION_REC_GUARD.set(Unit.INSTANCE);
			RenderPipeline.Builder builder = RenderPipeline.builder(base);
			usePipelineVertexFormat.ifPresentOrElse(
					builder::withUsePipelineDrawModeForGui,
					builder::withoutUsePipelineDrawModeForGui
			);
			RenderPipeline.Snippet snippet = builder.buildSnippet();
			extraData.put(snippet, usePipelineVertexFormat);
			return snippet;
		} finally {
			MIXIN_PIPELINE_MODIFICATION_REC_GUARD.set(original);
		}
	}

	public static Optional<Boolean> getUsePipelineDrawModeForGui(RenderPipeline.Snippet snippet) {
		return extraData.computeIfAbsent(snippet, ignored -> Optional.empty());
	}
}
