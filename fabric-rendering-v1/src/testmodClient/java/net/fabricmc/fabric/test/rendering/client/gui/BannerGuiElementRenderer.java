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

package net.fabricmc.fabric.test.rendering.client.gui;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.render.SpecialGuiElementRenderer;
import net.minecraft.client.render.DiffuseLighting;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BannerBlockEntityRenderer;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.model.ModelBaker;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.component.type.BannerPatternsComponent;
import net.minecraft.util.DyeColor;

public class BannerGuiElementRenderer extends SpecialGuiElementRenderer<BannerGuiElementRenderState> {
	protected BannerGuiElementRenderer(VertexConsumerProvider.Immediate vertexConsumers) {
		super(vertexConsumers);
	}

	@Override
	public Class<BannerGuiElementRenderState> getElementClass() {
		return BannerGuiElementRenderState.class;
	}

	@Override
	protected void render(BannerGuiElementRenderState state, MatrixStack matrices) {
		MinecraftClient.getInstance().gameRenderer.getDiffuseLighting().setShaderLights(DiffuseLighting.Type.ITEMS_FLAT);
		BannerBlockEntityRenderer.renderCanvas(matrices, vertexConsumers, 15728880, OverlayTexture.DEFAULT_UV, MinecraftClient.getInstance().getLoadedEntityModels().getModelPart(EntityModelLayers.STANDING_BANNER_FLAG).getChild("flag"), ModelBaker.BANNER_BASE, true, DyeColor.BLUE, BannerPatternsComponent.DEFAULT);
	}

	@Override
	protected String getName() {
		return "fabric test banner";
	}
}
