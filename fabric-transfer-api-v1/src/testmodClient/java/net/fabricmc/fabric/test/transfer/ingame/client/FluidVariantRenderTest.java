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

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.gui.screens.inventory.tooltip.DefaultTooltipPositioner;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.material.Fluids;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
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

		HudElementRegistry.addLast(Identifier.fromNamespaceAndPath("fabric-transfer-api-v1-testmod", "fluid_variant"), (drawContext, tickDelta) -> {
			Player player = Minecraft.getInstance().player;
			if (player == null) return;

			if (Minecraft.getInstance().debugEntries.isOverlayVisible()) return;

			int renderY = 0;
			List<FluidVariant> variants = List.of(FluidVariant.of(Fluids.WATER), FluidVariant.of(Fluids.LAVA));

			for (FluidVariant variant : variants) {
				TextureAtlasSprite[] sprites = FluidVariantRendering.getSprites(variant);
				int color = FluidVariantRendering.getColor(variant, player.level(), player.blockPosition());

				if (sprites != null) {
					drawContext.blitSprite(RenderPipelines.GUI_TEXTURED, sprites[0], 0, renderY, 16, 16, color);
					renderY += 16;
					drawContext.blitSprite(RenderPipelines.GUI_TEXTURED, sprites[1], 0, renderY, 16, 16, color);
					renderY += 16;
				}

				List<Component> tooltip = FluidVariantRendering.getTooltip(variant);
				Font textRenderer = Minecraft.getInstance().font;

				renderY += 2;

				for (Component line : tooltip) {
					renderY += 10;
					drawContext.renderTooltip(textRenderer, List.of(ClientTooltipComponent.create(line.getVisualOrderText())), -8, renderY, DefaultTooltipPositioner.INSTANCE, null);
				}
			}
		});
	}
}
