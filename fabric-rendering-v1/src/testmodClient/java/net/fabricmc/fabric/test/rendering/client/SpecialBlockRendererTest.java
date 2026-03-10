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

import java.util.function.Consumer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.mojang.serialization.MapCodec;
import org.joml.Vector3fc;

import net.minecraft.client.model.animal.allay.AllayModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.special.NoDataSpecialModelRenderer;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.client.renderer.special.SpecialModelRenderers;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Util;

import net.fabricmc.api.ClientModInitializer;

/**
 * Tests {@link SpecialBlockRendererRegistry} by rendering an allay model above TNT blocks in a minecart.
 */
public class SpecialBlockRendererTest implements ClientModInitializer {
	private static final Identifier ALLAY_TEXTURE = Identifier.withDefaultNamespace("textures/entity/allay/allay.png");
	private static final Identifier RENDERER_ID = Identifier.fromNamespaceAndPath("fabric-rendering-v1-testmod", "allay_tnt_renderer");

	@Override
	public void onInitializeClient() {
		SpecialModelRenderers.ID_MAPPER.put(RENDERER_ID, Unbaked.MAP_CODEC);
		// TODO 26.1 fix for TNT
	}

	private static class Renderer implements NoDataSpecialModelRenderer {
		private final AllayModel allayModel;

		Renderer(SpecialModelRenderer.BakingContext ctx) {
			allayModel = new AllayModel(ctx.entityModelSet().bakeLayer(ModelLayers.ALLAY));
		}

		@Override
		public void submit(PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int lightCoords, int overlayCoords, boolean hasFoil, int outlineColor) {
			poseStack.pushPose();
			poseStack.translate(0.5f, 0.0f, 0.5f);
			poseStack.translate(0, 1.46875f, 0);
			poseStack.scale(1, -1, 1);
			poseStack.mulPose(Axis.YP.rotation((float) (Util.getMillis() * 0.001)));
			poseStack.translate(0, -1.46875f, 0);
			submitNodeCollector.order(0)
					.submitCustomGeometry(poseStack, RenderTypes.solidMovingBlock(), (matricesEntry, vertexConsumer) -> allayModel.renderToBuffer(poseStack, vertexConsumer, lightCoords, overlayCoords));
			poseStack.popPose();
		}

		@Override
		public void getExtents(Consumer<Vector3fc> output) {
		}
	}

	private record Unbaked() implements NoDataSpecialModelRenderer.Unbaked {
		public static final MapCodec<Unbaked> MAP_CODEC = MapCodec.unit(new Unbaked());

		@Override
		public MapCodec<Unbaked> type() {
			return MAP_CODEC;
		}

		@Override
		public Renderer bake(final SpecialModelRenderer.BakingContext context) {
			return new Renderer(context);
		}
	}
}
