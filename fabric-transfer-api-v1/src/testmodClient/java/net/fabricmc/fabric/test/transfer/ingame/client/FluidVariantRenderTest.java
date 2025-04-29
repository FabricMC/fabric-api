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

package net.fabricmc.fabric.test.transfer.ingame.client;

import java.util.List;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.texture.Sprite;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluids;
import net.minecraft.text.Text;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.transfer.v1.client.fluid.FluidVariantRendering;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariantAttributes;

/**
 * Renders the water sprite in the top left of the screen, to make sure that it correctly depends on the position.
 */
public class FluidVariantRenderTest implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		FluidVariantAttributes.enableColoredVanillaFluidNames();

		HudRenderCallback.EVENT.register((drawContext, tickDelta) -> {
			PlayerEntity player = MinecraftClient.getInstance().player;
			if (player == null) return;

			if (MinecraftClient.getInstance().inGameHud.getDebugHud().shouldShowDebugHud()) return;

			int renderY = 0;
			List<FluidVariant> variants = List.of(FluidVariant.of(Fluids.WATER), FluidVariant.of(Fluids.LAVA));

			for (FluidVariant variant : variants) {
				Sprite[] sprites = FluidVariantRendering.getSprites(variant);
				int color = FluidVariantRendering.getColor(variant, player.getWorld(), player.getBlockPos());

				if (sprites != null) {
					drawContext.drawSpriteStretched(RenderPipelines.GUI_TEXTURED, sprites[0], 0, renderY, 16, 16, color);
					renderY += 16;
					drawContext.drawSpriteStretched(RenderPipelines.GUI_TEXTURED, sprites[1], 0, renderY, 16, 16, color);
					renderY += 16;
				}

				List<Text> tooltip = FluidVariantRendering.getTooltip(variant);
				TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;

				renderY += 2;

				for (Text line : tooltip) {
					renderY += 10;
					drawContext.drawTooltip(textRenderer, line, -8, renderY);
				}
			}
		});
	}
}
