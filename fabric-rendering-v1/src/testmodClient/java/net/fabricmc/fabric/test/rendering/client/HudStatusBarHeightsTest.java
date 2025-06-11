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

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudStatusBarHeightRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;

public class HudStatusBarHeightsTest implements ClientModInitializer {
	private static final Identifier TOUGHNESS_EMPTY_SPRITE = Identifier.of("fabric-rendering-v1-testmod",
			"hud/toughness_empty");
	private static final Identifier TOUGHNESS_HALF_SPRITE = Identifier.of("fabric-rendering-v1-testmod",
			"hud/toughness_half");
	private static final Identifier TOUGHNESS_FULL_SPRITE = Identifier.of("fabric-rendering-v1-testmod",
			"hud/toughness_full");
	private static final Identifier STAMINA_EMPTY_SPRITE = Identifier.of("fabric-rendering-v1-testmod",
			"hud/stamina_empty");
	private static final Identifier STAMINA_HALF_SPRITE = Identifier.of("fabric-rendering-v1-testmod",
			"hud/stamina_half");
	private static final Identifier STAMINA_FULL_SPRITE = Identifier.of("fabric-rendering-v1-testmod",
			"hud/stamina_full");

	@Override
	public void onInitializeClient() {
		testToughnessBar();
		testStaminaBar();
	}

	private static void testToughnessBar() {
		// register a toughness bar showing below the vanilla health bar
		Identifier id = Identifier.of("fabric-rendering-v1-testmod", "toughness_bar");
		HudElementRegistry.attachElementBefore(VanillaHudElements.HEALTH_BAR,
				id,
				(DrawContext context, RenderTickCounter tickCounter) -> {
					MinecraftClient minecraft = MinecraftClient.getInstance();

					if (minecraft.interactionManager.hasStatusBars()) {
						InGameHud hud = minecraft.inGameHud;
						int width = context.getScaledWindowWidth() / 2 - 91;
						int height = context.getScaledWindowHeight() - HudStatusBarHeightRegistry.getHeight(id);
						PlayerEntity player = hud.getCameraPlayer();
						renderArmor(context, player, height, 0, 10, width);
					}
				});
		HudStatusBarHeightRegistry.addLeft(id, (PlayerEntity player) -> {
			MinecraftClient minecraft = MinecraftClient.getInstance();
			return minecraft.interactionManager.hasStatusBars()
					&& MathHelper.floor(player.getAttributeValue(EntityAttributes.ARMOR_TOUGHNESS)) > 0 ? 10 : 0;
		});
	}

	private static void testStaminaBar() {
		// register a stamina bar showing above the vanilla food bar
		Identifier id = Identifier.of("fabric-rendering-v1-testmod", "stamina_bar");
		HudElementRegistry.attachElementAfter(VanillaHudElements.FOOD_BAR,
				id,
				(DrawContext context, RenderTickCounter tickCounter) -> {
					MinecraftClient minecraft = MinecraftClient.getInstance();

					if (minecraft.interactionManager.hasStatusBars()) {
						InGameHud hud = minecraft.inGameHud;
						LivingEntity livingEntity = hud.getRiddenEntity();

						if (hud.getHeartCount(livingEntity) == 0) {
							int width = context.getScaledWindowWidth() / 2 + 91;
							int height = context.getScaledWindowHeight() - HudStatusBarHeightRegistry.getHeight(id);
							renderFood(context, hud.getCameraPlayer(), height, width);
						}
					}
				});
		HudStatusBarHeightRegistry.addRight(id, (PlayerEntity player) -> {
			MinecraftClient minecraft = MinecraftClient.getInstance();

			if (minecraft.interactionManager.hasStatusBars()) {
				LivingEntity livingEntity = minecraft.inGameHud.getRiddenEntity();

				if (minecraft.inGameHud.getHeartCount(livingEntity) == 0) {
					return 10;
				}
			}

			return 0;
		});
	}

	/**
	 * @see InGameHud#renderArmor(DrawContext, PlayerEntity, int, int, int, int)
	 */
	private static void renderArmor(DrawContext context, PlayerEntity player, int y, int heartRows, int height, int x) {
		int i = MathHelper.floor(player.getAttributeValue(EntityAttributes.ARMOR_TOUGHNESS));

		if (i > 0) {
			int j = y - (heartRows - 1) * height - 10;

			for (int k = 0; k < 10; k++) {
				int l = x + k * 8;

				if (k * 2 + 1 < i) {
					context.drawGuiTexture(RenderPipelines.GUI_TEXTURED, TOUGHNESS_FULL_SPRITE, l, j, 9, 9);
				}

				if (k * 2 + 1 == i) {
					context.drawGuiTexture(RenderPipelines.GUI_TEXTURED, TOUGHNESS_HALF_SPRITE, l, j, 9, 9);
				}

				if (k * 2 + 1 > i) {
					context.drawGuiTexture(RenderPipelines.GUI_TEXTURED, TOUGHNESS_EMPTY_SPRITE, l, j, 9, 9);
				}
			}
		}
	}

	/**
	 * @see InGameHud#renderFood(DrawContext, PlayerEntity, int, int)
	 */
	private static void renderFood(DrawContext context, PlayerEntity player, int y, int x) {
		int k = player.getHungerManager().getFoodLevel();

		for (int l = 0; l < 10; l++) {
			int n = x - l * 8 - 9;
			context.drawGuiTexture(RenderPipelines.GUI_TEXTURED, STAMINA_EMPTY_SPRITE, n, y, 9, 9);

			if (l * 2 + 1 < k) {
				context.drawGuiTexture(RenderPipelines.GUI_TEXTURED, STAMINA_FULL_SPRITE, n, y, 9, 9);
			}

			if (l * 2 + 1 == k) {
				context.drawGuiTexture(RenderPipelines.GUI_TEXTURED, STAMINA_HALF_SPRITE, n, y, 9, 9);
			}
		}
	}
}
