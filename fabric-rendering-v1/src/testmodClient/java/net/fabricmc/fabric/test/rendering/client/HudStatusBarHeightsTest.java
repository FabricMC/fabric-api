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
		Identifier resourceLocation = Identifier.of("fabric-rendering-v1-testmod", "toughness_bar");
		HudElementRegistry.attachElementBefore(VanillaHudElements.HEALTH_BAR,
				resourceLocation,
				(DrawContext guiGraphics, RenderTickCounter deltaTracker) -> {
					MinecraftClient minecraft = MinecraftClient.getInstance();
					if (minecraft.interactionManager.hasStatusBars()) {
						InGameHud gui = minecraft.inGameHud;
						int width = guiGraphics.getScaledWindowWidth() / 2 - 91;
						int height = guiGraphics.getScaledWindowHeight() -
								HudStatusBarHeightRegistry.getHeight(resourceLocation);
						PlayerEntity player = gui.getCameraPlayer();
						renderArmor(guiGraphics, player, height, 0, 10, width);
					}
				});
		HudStatusBarHeightRegistry.addLeft(resourceLocation, (PlayerEntity player) -> {
			MinecraftClient minecraft = MinecraftClient.getInstance();
			return minecraft.interactionManager.hasStatusBars() &&
					MathHelper.floor(player.getAttributeValue(EntityAttributes.ARMOR_TOUGHNESS)) > 0 ? 10 : 0;
		});
	}

	/**
	 * @see InGameHud#renderArmor(DrawContext, PlayerEntity, int, int, int, int)
	 */
	private static void renderArmor(DrawContext guiGraphics, PlayerEntity player, int y, int heartRows, int height, int x) {
		int i = MathHelper.floor(player.getAttributeValue(EntityAttributes.ARMOR_TOUGHNESS));
		if (i > 0) {
			int j = y - (heartRows - 1) * height - 10;

			for (int k = 0; k < 10; k++) {
				int l = x + k * 8;
				if (k * 2 + 1 < i) {
					guiGraphics.drawGuiTexture(RenderPipelines.GUI_TEXTURED, TOUGHNESS_FULL_SPRITE, l, j, 9, 9);
				}

				if (k * 2 + 1 == i) {
					guiGraphics.drawGuiTexture(RenderPipelines.GUI_TEXTURED, TOUGHNESS_HALF_SPRITE, l, j, 9, 9);
				}

				if (k * 2 + 1 > i) {
					guiGraphics.drawGuiTexture(RenderPipelines.GUI_TEXTURED, TOUGHNESS_EMPTY_SPRITE, l, j, 9, 9);
				}
			}
		}
	}

	private static void testStaminaBar() {
		// register a stamina bar showing above the vanilla food bar
		Identifier resourceLocation = Identifier.of("fabric-rendering-v1-testmod", "stamina_bar");
		HudElementRegistry.attachElementAfter(VanillaHudElements.FOOD_BAR,
				resourceLocation,
				(DrawContext guiGraphics, RenderTickCounter deltaTracker) -> {
					MinecraftClient minecraft = MinecraftClient.getInstance();
					if (minecraft.interactionManager.hasStatusBars()) {
						InGameHud gui = minecraft.inGameHud;
						LivingEntity livingEntity = gui.getRiddenEntity();
						if (gui.getHeartCount(livingEntity) == 0) {
							int width = guiGraphics.getScaledWindowWidth() / 2 + 91;
							int height = guiGraphics.getScaledWindowHeight() -
									HudStatusBarHeightRegistry.getHeight(resourceLocation);
							renderFood(guiGraphics, gui.getCameraPlayer(), height, width);
						}
					}
				});
		HudStatusBarHeightRegistry.addRight(resourceLocation, (PlayerEntity player) -> {
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
	 * @see InGameHud#renderFood(DrawContext, PlayerEntity, int, int)
	 */
	private static void renderFood(DrawContext guiGraphics, PlayerEntity player, int y, int x) {
		int k = player.getHungerManager().getFoodLevel();
		for (int l = 0; l < 10; l++) {
			int n = x - l * 8 - 9;
			guiGraphics.drawGuiTexture(RenderPipelines.GUI_TEXTURED, STAMINA_EMPTY_SPRITE, n, y, 9, 9);
			if (l * 2 + 1 < k) {
				guiGraphics.drawGuiTexture(RenderPipelines.GUI_TEXTURED, STAMINA_FULL_SPRITE, n, y, 9, 9);
			}

			if (l * 2 + 1 == k) {
				guiGraphics.drawGuiTexture(RenderPipelines.GUI_TEXTURED, STAMINA_HALF_SPRITE, n, y, 9, 9);
			}
		}
	}
}
