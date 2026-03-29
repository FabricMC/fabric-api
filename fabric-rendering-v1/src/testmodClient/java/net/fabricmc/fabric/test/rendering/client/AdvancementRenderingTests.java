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

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.util.CommonColors;
import net.minecraft.util.Mth;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.advancement.AdvancementRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.advancement.AdvancementRenderer;

public class AdvancementRenderingTests implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		AdvancementRenderer.registerIcon(new StoryRootIconRenderer(), Identifier.withDefaultNamespace("story/root"));
		AdvancementRenderer.registerBackground(new StoryBackgroundRenderer(), Identifier.withDefaultNamespace("story/root"));
		AdvancementRenderer.registerFrame(new MineDiamondFrameRenderer(), Identifier.withDefaultNamespace("story/mine_diamond"));
	}

	static class StoryRootIconRenderer implements AdvancementRenderer.IconRenderer {
		@Override
		public void extractAdvancementIcon(AdvancementRenderContext.Icon context) {
			if (context.isHovered()) {
				context.graphics().text(Minecraft.getInstance().font, "hovered", context.x(), context.y(), -1);
			}

			if (context.isSelected()) {
				context.graphics().text(Minecraft.getInstance().font, "selected", context.x(), context.y() + 9, -1);
			}
		}

		@Override
		public boolean shouldRenderOriginalIcon() {
			return true;
		}
	}

	static class StoryBackgroundRenderer implements AdvancementRenderer.BackgroundRenderer {
		private static final Identifier BACKGROUND = Identifier.withDefaultNamespace("textures/painting/unpacked.png");

		@Override
		public void extractAdvancementBackground(AdvancementRenderContext.Background context) {
			ScreenRectangle bounds = context.bounds();
			context.graphics().blit(RenderPipelines.GUI_TEXTURED, BACKGROUND, bounds.left(), bounds.top(), 1 - Mth.floor(context.scrollX()), 3 - Mth.floor(context.scrollY()), bounds.width(), bounds.height(), 64, 64);
		}
	}

	static class MineDiamondFrameRenderer implements AdvancementRenderer.FrameRenderer {
		@Override
		public void extractAdvancementFrame(AdvancementRenderContext.Frame context) {
			int x = context.x();
			int y = context.y();
			context.graphics().fill(x, y, x + 26, y + 26, context.isObtained() ? CommonColors.GREEN : CommonColors.RED);

			if (context.isHovered()) {
				context.graphics().text(Minecraft.getInstance().font, "hovered", context.x(), context.y(), -1);
			}
		}

		@Override
		public boolean shouldRenderTooltip() {
			return false;
		}
	}
}
