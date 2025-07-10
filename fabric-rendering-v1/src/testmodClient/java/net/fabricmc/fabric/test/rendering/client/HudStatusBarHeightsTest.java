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
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElement;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudStatusBarHeightRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.fabricmc.fabric.mixin.client.rendering.InGameHudAccessor;

public class HudStatusBarHeightsTest implements ClientModInitializer {
	private static final Identifier HEART_CONTAINER_TEXTURE = Identifier.ofVanilla("hud/heart/container");
	private static final Identifier HEART_HALF_TEXTURE = Identifier.ofVanilla("hud/heart/absorbing_half");
	private static final Identifier HEART_FULL_TEXTURE = Identifier.ofVanilla("hud/heart/absorbing_full");
	private static final Identifier ARMOR_EMPTY_TEXTURE = Identifier.ofVanilla("hud/armor_empty");
	private static final Identifier ARMOR_HALF_TEXTURE = Identifier.ofVanilla("hud/armor_half");
	private static final Identifier ARMOR_FULL_TEXTURE = Identifier.ofVanilla("hud/armor_full");
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
		testHealthBar();
		testArmorBar();
		testToughnessBar();
		testStaminaBar();
	}

	private static void testHealthBar() {
		// register a custom health bar with a different height for large max health;
		// ideally tested together with a custom armor bar
		HudElementRegistry.replaceElement(VanillaHudElements.HEALTH_BAR,
				(HudElement element) -> (DrawContext context, RenderTickCounter tickCounter) -> {
					MinecraftClient minecraft = MinecraftClient.getInstance();

					if (minecraft.interactionManager.hasStatusBars()) {
						InGameHud hud = minecraft.inGameHud;
						int width = context.getScaledWindowWidth() / 2 - 91;
						int height = context.getScaledWindowHeight() - HudStatusBarHeightRegistry.getHeight(
								VanillaHudElements.HEALTH_BAR);
						PlayerEntity player = ((InGameHudAccessor) hud).fabric$callGetCameraPlayer();
						renderHealth(context, player, height, 0, 10, width);
					}
				});
		HudStatusBarHeightRegistry.addLeft(VanillaHudElements.HEALTH_BAR, (PlayerEntity player) -> {
			MinecraftClient minecraft = MinecraftClient.getInstance();
			return minecraft.interactionManager.hasStatusBars() ? 10 : 0;
		});
	}

	private static void testArmorBar() {
		// register a custom armor bar with slightly altered rendering compared to the vanilla bar
		HudElementRegistry.replaceElement(VanillaHudElements.ARMOR_BAR,
				(HudElement element) -> (DrawContext context, RenderTickCounter tickCounter) -> {
					MinecraftClient minecraft = MinecraftClient.getInstance();

					if (minecraft.interactionManager.hasStatusBars()) {
						InGameHud hud = minecraft.inGameHud;
						int width = context.getScaledWindowWidth() / 2 - 91;
						int height = context.getScaledWindowHeight() - HudStatusBarHeightRegistry.getHeight(
								VanillaHudElements.ARMOR_BAR);
						PlayerEntity player = ((InGameHudAccessor) hud).fabric$callGetCameraPlayer();
						renderArmor(context, player, height, 0, 10, width);
					}
				});

		// it does not matter whether this is registered, as it supplies the same values as the vanilla behavior
		if (false) {
			HudStatusBarHeightRegistry.addLeft(VanillaHudElements.ARMOR_BAR, (PlayerEntity player) -> {
				MinecraftClient minecraft = MinecraftClient.getInstance();
				return minecraft.interactionManager.hasStatusBars() && player.getArmor() > 0 ? 10 : 0;
			});
		}
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
						PlayerEntity player = ((InGameHudAccessor) hud).fabric$callGetCameraPlayer();
						renderToughness(context, player, height, 0, 10, width);
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
						LivingEntity livingEntity = ((InGameHudAccessor) hud).fabric$callGetRiddenEntity();

						if (((InGameHudAccessor) hud).fabric$callGetHeartCount(livingEntity) == 0) {
							int width = context.getScaledWindowWidth() / 2 + 91;
							int height = context.getScaledWindowHeight() - HudStatusBarHeightRegistry.getHeight(id);
							renderStamina(context,
									((InGameHudAccessor) hud).fabric$callGetCameraPlayer(),
									height,
									width);
						}
					}
				});
		HudStatusBarHeightRegistry.addRight(id, (PlayerEntity player) -> {
			MinecraftClient minecraft = MinecraftClient.getInstance();

			if (minecraft.interactionManager.hasStatusBars()) {
				InGameHud hud = minecraft.inGameHud;
				LivingEntity livingEntity = ((InGameHudAccessor) hud).fabric$callGetRiddenEntity();

				if (((InGameHudAccessor) hud).fabric$callGetHeartCount(livingEntity) == 0) {
					return 10;
				}
			}

			return 0;
		});
	}

	/**
	 * @see InGameHud#renderArmor(DrawContext, PlayerEntity, int, int, int, int)
	 */
	private static void renderHealth(DrawContext context, PlayerEntity player, int y, int heartRows, int height, int x) {
		int l = MathHelper.floor(player.getHealth());

		if (l > 0) {
			int m = y - (heartRows - 1) * height - 10;

			for (int n = 0; n < 10; ++n) {
				int o = x + n * 8;
				context.drawGuiTexture(RenderPipelines.GUI_TEXTURED, HEART_CONTAINER_TEXTURE, o, m, 9, 9);

				if (n * 2 + 1 < l) {
					context.drawGuiTexture(RenderPipelines.GUI_TEXTURED, HEART_FULL_TEXTURE, o, m, 9, 9);
				}

				if (n * 2 + 1 == l) {
					context.drawGuiTexture(RenderPipelines.GUI_TEXTURED, HEART_HALF_TEXTURE, o, m, 9, 9);
				}
			}
		}
	}

	/**
	 * @see InGameHud#renderArmor(DrawContext, PlayerEntity, int, int, int, int)
	 */
	private static void renderArmor(DrawContext context, PlayerEntity player, int y, int heartRows, int height, int x) {
		int l = player.getArmor();

		if (l > 0) {
			int m = y - (heartRows - 1) * height - 10;

			for (int n = 0; n < 10; ++n) {
				int o = x + n * 8;

				if (n * 2 + 1 < l) {
					context.drawGuiTexture(RenderPipelines.GUI_TEXTURED, ARMOR_FULL_TEXTURE, o, m, 9, 9);
				}

				if (n * 2 + 1 == l) {
					context.drawGuiTexture(RenderPipelines.GUI_TEXTURED, ARMOR_HALF_TEXTURE, o, m, 9, 9);
				}
			}
		}
	}

	/**
	 * @see InGameHud#renderArmor(DrawContext, PlayerEntity, int, int, int, int)
	 */
	private static void renderToughness(DrawContext context, PlayerEntity player, int y, int heartRows, int height, int x) {
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
	private static void renderStamina(DrawContext context, PlayerEntity player, int y, int x) {
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
