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

package net.fabricmc.fabric.mixin.client.rendering;

import java.util.Optional;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

import net.fabricmc.fabric.api.client.rendering.v1.FabricRenderPipeline;
import net.fabricmc.fabric.impl.client.rendering.FabricRenderPipelineInternals;

@Mixin(RenderPipeline.Snippet.class)
class RenderPipelineSnippetMixin implements FabricRenderPipeline.Snippet {
	@Unique
	private final Optional<Boolean> usePipelineDrawModeForGui = FabricRenderPipelineInternals.getScopedUsePipelineVertexFormatForGui();

	private RenderPipelineSnippetMixin() {
	}

	@Override
	public Optional<Boolean> usePipelineDrawModeForGui() {
		return usePipelineDrawModeForGui;
	}

	@ModifyReturnValue(
			method = "toString",
			at = @At("RETURN")
	)
	private String modifyToStringToIncludeFabricExtraData(String original) {
		return original.substring(0, original.length() - 1)
				+ ", usePipelineDrawModeForGui="
				+ usePipelineDrawModeForGui()
				+ original.substring(original.length() - 1);
	}

	@ModifyReturnValue(
			method = "equals",
			at = @At("RETURN")
	)
	private boolean modifyEqualsToIncludeFabricExtraData(boolean original, Object other) {
		return original
				&& other instanceof FabricRenderPipeline.Snippet otherSnippet
				&& usePipelineDrawModeForGui().equals(otherSnippet.usePipelineDrawModeForGui());
	}

	@ModifyReturnValue(
			method = "hashCode",
			at = @At("RETURN")
	)
	private int modifyHashCodeToIncludeFabricExtraData(int original) {
		return hashCombiner(original, usePipelineDrawModeForGui().hashCode());
	}

	// taken from java.lang.runtime.ObjectMethods.hashCombiner(int, int)
	@Unique
	private static int hashCombiner(int x, int y) {
		return x * 31 + y;
	}
}
