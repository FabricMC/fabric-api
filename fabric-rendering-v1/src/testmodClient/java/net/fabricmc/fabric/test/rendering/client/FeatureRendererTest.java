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

package net.fabricmc.fabric.test.rendering.client;

import java.util.List;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.renderer.feature.FeatureFrameContext;
import net.minecraft.client.renderer.feature.FeatureRendererType;
import net.minecraft.client.renderer.feature.RenderTypeFeatureRenderer;
import net.minecraft.client.renderer.feature.submit.SubmitNode;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.util.ARGB;
import net.minecraft.world.phys.AABB;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.FabricOrderedSubmitNodeCollector;
import net.fabricmc.fabric.api.client.rendering.v1.FeatureRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.SubmitRenderPhase;

/**
 * Tests {@link FeatureRendererRegistry} and
 * {@link FabricOrderedSubmitNodeCollector#submitCustom(SubmitRenderPhase, SubmitNode)} by rendering
 * a quad above every lectern.
 */
public class FeatureRendererTest implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		FeatureRendererRegistry.register(CustomFeatureRenderer.TYPE, CustomFeatureRenderer::new);
	}

	public record CustomSubmit(PoseStack.Pose pose) implements SubmitNode {
		@Override
		public FeatureRendererType<? extends SubmitNode> featureType() {
			return CustomFeatureRenderer.TYPE;
		}
	}

	private static class CustomFeatureRenderer extends RenderTypeFeatureRenderer<CustomSubmit> {
		private static final FeatureRendererType<CustomSubmit> TYPE = FeatureRendererType.create("custom");

		private static final AABB box = new AABB(0.25, 1.0, 0.0, 0.75, 1.5, 0.0);

		@Override
		protected void buildGroup(FeatureFrameContext context, List<CustomSubmit> customSubmits) {
			if (customSubmits.isEmpty()) return;

			VertexConsumer buffer = getVertexBuilder(RenderTypes.debugFilledBox());

			for (CustomSubmit submit : customSubmits) {
				TestRenderUtils.drawFilledBox(submit.pose(), buffer, box, ARGB.colorFromFloat(1.0f, 0, 1, 0));
			}
		}
	}
}
