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

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.render.pip.PictureInPictureRenderer;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.object.banner.BannerModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BannerRenderer;
import net.minecraft.client.renderer.feature.FeatureRenderDispatcher;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.util.Unit;
import net.minecraft.world.level.block.entity.BannerPatternLayers;

public class BannerGuiElementRenderer extends PictureInPictureRenderer<BannerGuiElementRenderState> {
	protected BannerGuiElementRenderer(MultiBufferSource.BufferSource bufferSource) {
		super(bufferSource);
	}

	@Override
	public Class<BannerGuiElementRenderState> getRenderStateClass() {
		return BannerGuiElementRenderState.class;
	}

	@Override
	protected void renderToTexture(BannerGuiElementRenderState state, PoseStack poseStack) {
		Minecraft client = Minecraft.getInstance();
		client.gameRenderer.getLighting().setupFor(Lighting.Entry.ITEMS_FLAT);
		FeatureRenderDispatcher renderDispatcher = client.gameRenderer.getFeatureRenderDispatcher();
		BannerRenderer.submitPatterns(
				client.getAtlasManager(),
				poseStack,
				renderDispatcher.getSubmitNodeStorage(),
				LightCoordsUtil.FULL_BRIGHT,
				OverlayTexture.NO_OVERLAY,
				new BannerModel(Minecraft.getInstance().getEntityModels().bakeLayer(ModelLayers.STANDING_BANNER_FLAG).getChild("flag")),
				Unit.INSTANCE,
				true,
				state.color(),
				BannerPatternLayers.EMPTY,
				null);
		renderDispatcher.renderAllFeatures();
	}

	@Override
	protected String getTextureLabel() {
		return "fabric test banner";
	}
}
