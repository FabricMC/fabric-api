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

package net.fabricmc.fabric.test.object.builder.client;

import org.jetbrains.annotations.Nullable;

import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.client.render.entity.model.ChickenEntityModel;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.state.ChickenEntityRenderState;
import net.minecraft.client.texture.MissingSprite;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.EntityAttachmentType;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import net.fabricmc.fabric.test.object.builder.TrackStackEntity;

public class TrackStackEntityRenderer extends MobEntityRenderer<TrackStackEntity, TrackStackEntityRenderer.RenderState, ChickenEntityModel> {
	public TrackStackEntityRenderer(EntityRendererFactory.Context context) {
		super(context, new ChickenEntityModel(context.getPart(EntityModelLayers.CHICKEN)), 0.3f);
	}

	@Override
	public void render(RenderState renderState, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light) {
		super.render(renderState, matrices, vertexConsumers, light);
		Iterable<Text> labelLines = renderState.labelLines;

		if (labelLines == null) {
			return;
		}

		matrices.push();
		matrices.translate(0, -2, 0);

		for (Text line : labelLines) {
			this.renderLabelIfPresent(renderState, line, matrices, vertexConsumers, light);
			matrices.translate(0, 0.25875f, 0);
		}

		matrices.pop();
	}

	@Override
	public Identifier getTexture(RenderState renderState) {
		return MissingSprite.getMissingSpriteId();
	}

	@Override
	public RenderState createRenderState() {
		return new RenderState();
	}

	@Override
	public void updateRenderState(TrackStackEntity entity, RenderState renderState, float tickProgress) {
		super.updateRenderState(entity, renderState, tickProgress);
		renderState.labelLines = entity.getLabelLines();

		if (renderState.nameLabelPos == null) {
			renderState.nameLabelPos = entity.getAttachments().getPointNullable(EntityAttachmentType.NAME_TAG, 0, entity.getLerpedYaw(tickProgress));
		}
	}

	public static class RenderState extends ChickenEntityRenderState {
		@Nullable
		public Iterable<Text> labelLines;
	}
}
