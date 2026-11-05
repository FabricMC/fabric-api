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

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.vertex.PoseStack;
import org.jspecify.annotations.Nullable;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.render.pip.PictureInPictureRenderer;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.object.banner.BannerFlagModel;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BannerRenderer;
import net.minecraft.client.renderer.feature.FeatureRenderDispatcher;
import net.minecraft.client.renderer.state.gui.GuiRenderState;
import net.minecraft.client.renderer.state.gui.pip.GuiBannerResultRenderState;
import net.minecraft.client.renderer.state.gui.pip.PictureInPictureRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.sprite.SpriteGetter;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.entity.BannerPatternLayers;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.PictureInPictureRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.pipline.modifier.RenderPipelineModifier;
import net.fabricmc.fabric.api.client.rendering.v1.pipline.modifier.RenderPipelineModifierManager;
import net.fabricmc.fabric.api.client.rendering.v1.pipline.modifier.RenderPipelineModifierRegistry;

public class RenderPipelineModifierTests implements ClientModInitializer {
	public static final ResourceKey<RenderPipelineModifier> MODIFIER = ResourceKey.create(RenderPipelineModifier.REGISTRY, Identifier.fromNamespaceAndPath("fabric", "modifier"));
	@Override
	public void onInitializeClient() {
		RenderPipelineModifierRegistry.register(MODIFIER, (pipeline, suggestedNewName) -> {
			return pipeline.toBuilder().withFragmentShader(Identifier.fromNamespaceAndPath("fabric-rendering-v1-testmod", "core/test_entity")).withLocation(suggestedNewName).build();
		});
		Supplier<BannerFlagModel> flag = Suppliers.memoize(() -> {
			ModelPart modelPart = Minecraft.getInstance().getEntityModels().bakeLayer(ModelLayers.STANDING_BANNER_FLAG);
			return new BannerFlagModel(modelPart);
		});
		HudElementRegistry.addFirst(Identifier.fromNamespaceAndPath("fabric", "test-test-test-test-test-test"), (graphics, deltaTracker) -> {
			Font font = Minecraft.getInstance().font;
			int startX = graphics.guiWidth() - 20;
			String label1 = "Green:";
			String label2 = "Purple:";
			int label1X = startX - font.width(label1) - 5;
			int label2X = startX - font.width(label2) - 5;
			graphics.text(font, label1, label1X, 0, 0xFFFFFFFF);
			graphics.bannerPattern(
					flag.get(), DyeColor.GREEN, BannerPatternLayers.EMPTY, startX, 0, graphics.guiWidth(), 20
			);
			graphics.text(font, label2, label2X, 30, 0xFFFFFFFF);
			/*Our custom shader changes this to be purple. If it isn't, something is broken*/
			graphics.guiRenderState.addPicturesInPictureState(new State(new GuiBannerResultRenderState(
					flag.get(), DyeColor.GREEN, BannerPatternLayers.EMPTY, startX, 30, graphics.guiWidth(), 50, graphics.scissorStack.peek())
			));
		});

		PictureInPictureRendererRegistry.register(ctx -> new Renderer(ctx.minecraft().getAtlasManager()));
	}

	record State(GuiBannerResultRenderState innerState) implements PictureInPictureRenderState {
		@Override
		public int x0() {
			return innerState.x0();
		}

		@Override
		public int x1() {
			return innerState.x1();
		}

		@Override
		public int y0() {
			return innerState.y0();
		}

		@Override
		public int y1() {
			return innerState.y1();
		}

		@Override
		public float scale() {
			return innerState.scale();
		}

		@Override
		public @Nullable ScreenRectangle scissorArea() {
			return innerState.scissorArea();
		}

		@Override
		public @Nullable ScreenRectangle bounds() {
			return innerState.bounds();
		}
	}

	public static class Renderer extends PictureInPictureRenderer<State> {
		private final SpriteGetter sprites;
		public Renderer(final SpriteGetter sprites) {
			this.sprites = sprites;
		}

		public Class<State> getRenderStateClass() {
			return State.class;
		}

		@Override
		public void prepare(State renderState, GuiRenderState guiRenderState, FeatureRenderDispatcher featureRenderDispatcher, int guiScale) {
			RenderPipelineModifierManager.renderWithModifier(MODIFIER, () -> super.prepare(renderState, guiRenderState, featureRenderDispatcher, guiScale));
		}

		protected void renderToTexture(final State renderState, final PoseStack poseStack, final SubmitNodeCollector submitNodeCollector) {
			Minecraft.getInstance().gameRenderer.lighting().setupFor(Lighting.Entry.ITEMS_FLAT);
			poseStack.translate(0.0F, 0.25F, 0.0F);
			submitNodeCollector.submitModel(renderState.innerState().flag(), 0.0F, poseStack, 15728880, OverlayTexture.NO_OVERLAY, -1, Sheets.BANNER_BASE, this.sprites, 0);
			BannerRenderer.submitPatterns(this.sprites, poseStack, submitNodeCollector, 15728880, OverlayTexture.NO_OVERLAY, renderState.innerState().flag(), 0.0F, true, renderState.innerState().baseColor(), renderState.innerState().resultBannerPatterns());
		}

		protected String getTextureLabel() {
			return "banner result";
		}
	}
}
