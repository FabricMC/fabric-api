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

package net.fabricmc.fabric.mixin.client.indigo.renderer;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.OutlineBufferSource;
import net.minecraft.client.renderer.SubmitNodeCollection;
import net.minecraft.client.renderer.feature.ItemFeatureRenderer;

import net.fabricmc.fabric.api.client.renderer.v1.render.FabricSubmitNodeCollection;
import net.fabricmc.fabric.impl.client.indigo.renderer.render.AltItemRenderer;

@Mixin(ItemFeatureRenderer.class)
abstract class ItemFeatureRendererMixin {
	@Unique
	private final AltItemRenderer altItemRenderer = new AltItemRenderer();

	@Inject(method = "renderSolid", at = @At("RETURN"))
	private void onReturnRenderSolid(SubmitNodeCollection nodeCollection, MultiBufferSource.BufferSource bufferSource, OutlineBufferSource outlineBufferSource, CallbackInfo ci) {
		altItemRenderer.prepare(bufferSource, outlineBufferSource, false);

		for (FabricSubmitNodeCollection.ExtendedItemSubmit submit : nodeCollection.getExtendedItemSubmits()) {
			altItemRenderer.renderItem(submit);
		}

		altItemRenderer.clear();
	}

	@Inject(method = "renderTranslucent", at = @At("RETURN"))
	private void onReturnRenderTranslucent(SubmitNodeCollection nodeCollection, MultiBufferSource.BufferSource bufferSource, OutlineBufferSource outlineBufferSource, CallbackInfo ci) {
		altItemRenderer.prepare(bufferSource, outlineBufferSource, true);

		for (FabricSubmitNodeCollection.ExtendedItemSubmit submit : nodeCollection.getExtendedItemSubmits()) {
			altItemRenderer.renderItem(submit);
		}

		altItemRenderer.clear();
	}
}
