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

import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.extract.LevelExtractor;
import net.minecraft.client.renderer.state.level.LevelRenderState;

import net.fabricmc.fabric.api.client.rendering.v1.InvalidateRenderStateCallback;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelExtractionEvents;
import net.fabricmc.fabric.impl.client.rendering.LevelRendererExtensions;
import net.fabricmc.fabric.impl.client.rendering.level.LevelExtractionContextImpl;

@Mixin(LevelExtractor.class)
public class LevelExtractorMixin implements LevelRendererExtensions {
	@Shadow
	@Final
	private Minecraft minecraft;
	@Shadow
	@Final
	private LevelRenderState levelRenderState;
	@Shadow
	private @Nullable ClientLevel level;
	@Unique
	private final LevelExtractionContextImpl extractionContext = new LevelExtractionContextImpl();

	@Override
	public void fabric_prepareLevelExtractionContext(DeltaTracker deltaTracker) {
		extractionContext.prepare(
				minecraft.gameRenderer,
				minecraft.levelRenderer,
				levelRenderState,
				level,
				deltaTracker,
				minecraft.gameRenderer.mainCamera());
	}

	@Inject(method = "extractBlockOutline", at = @At("RETURN"))
	private void afterBlockOutlineExtraction(Camera camera, LevelRenderState renderStates, CallbackInfo ci) {
		LevelExtractionEvents.AFTER_BLOCK_OUTLINE_EXTRACTION.invoker().afterBlockOutlineExtraction(extractionContext, minecraft.hitResult);
	}

	@Inject(method = "extract", at = @At("RETURN"))
	private void afterExtractLevel(DeltaTracker deltaTracker, Camera camera, float deltaPartialTick, CallbackInfo ci) {
		LevelExtractionEvents.END_EXTRACTION.invoker().endExtraction(extractionContext);
	}

	@Inject(method = "allChanged", at = @At("HEAD"))
	private void onReload(CallbackInfo ci) {
		InvalidateRenderStateCallback.EVENT.invoker().onInvalidate();
	}
}
