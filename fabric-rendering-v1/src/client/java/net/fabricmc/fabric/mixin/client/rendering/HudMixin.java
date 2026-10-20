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

package net.fabricmc.fabric.mixin.client.rendering;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.Hud;
import net.minecraft.client.gui.components.PlayerTabOverlay;
import net.minecraft.client.gui.contextualbar.ContextualBar;
import net.minecraft.world.entity.player.Player;

import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.fabricmc.fabric.impl.client.rendering.hud.HudElementRegistryImpl;

@Mixin(Hud.class)
abstract class HudMixin {
	@WrapOperation(method = "extractRenderState", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Hud;extractCameraOverlays(Lnet/minecraft/client/gui/GuiGraphicsExtractor;)V"))
	private void wrapMiscOverlays(Hud instance, GuiGraphicsExtractor graphics, Operation<Void> renderVanilla) {
		HudElementRegistryImpl.getRoot(VanillaHudElements.MISC_OVERLAYS).extractRenderState(
				graphics,
				ctx -> renderVanilla.call(instance, ctx));
	}

	@WrapOperation(method = "extractRenderState", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Hud;extractCrosshair(Lnet/minecraft/client/gui/GuiGraphicsExtractor;)V"))
	private void wrapCrosshair(Hud instance, GuiGraphicsExtractor graphics, Operation<Void> renderVanilla) {
		HudElementRegistryImpl.getRoot(VanillaHudElements.CROSSHAIR).extractRenderState(
				graphics,
				ctx -> renderVanilla.call(instance, ctx));
	}

	@WrapOperation(method = "extractHotbarAndDecorations", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Hud;extractItemHotbar(Lnet/minecraft/client/gui/GuiGraphicsExtractor;)V"))
	private void wrapHotbar(Hud instance, GuiGraphicsExtractor graphics, Operation<Void> renderVanilla) {
		HudElementRegistryImpl.getRoot(VanillaHudElements.HOTBAR).extractRenderState(
				graphics,
				ctx -> renderVanilla.call(instance, ctx));
	}

	@WrapOperation(method = "extractPlayerHealth", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Hud;extractArmor(Lnet/minecraft/client/gui/GuiGraphicsExtractor;Lnet/minecraft/world/entity/player/Player;IIII)V"))
	private void wrapArmorBar(GuiGraphicsExtractor graphics, Player player, int i, int j, int k, int x, Operation<Void> renderVanilla) {
		HudElementRegistryImpl.getRoot(VanillaHudElements.ARMOR_BAR).extractRenderState(
				graphics, ctx -> renderVanilla.call(ctx, player, i, j, k, x));
	}

	@WrapOperation(method = "extractPlayerHealth", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Hud;extractHearts(Lnet/minecraft/client/gui/GuiGraphicsExtractor;Lnet/minecraft/world/entity/player/Player;IIIIFIIIZ)V"))
	private void wrapHealthBar(Hud instance, GuiGraphicsExtractor graphics, Player player, int x, int y, int lines, int regeneratingHeartIndex, float maxHealth, int lastHealth, int health, int absorption, boolean blinking, Operation<Void> renderVanilla) {
		HudElementRegistryImpl.getRoot(VanillaHudElements.HEALTH_BAR).extractRenderState(
				graphics, ctx -> renderVanilla.call(instance, ctx, player, x, y, lines, regeneratingHeartIndex, maxHealth, lastHealth, health, absorption, blinking));
	}

	@WrapOperation(method = "extractPlayerHealth", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Hud;extractFood(Lnet/minecraft/client/gui/GuiGraphicsExtractor;Lnet/minecraft/world/entity/player/Player;II)V"))
	private void wrapFoodBar(Hud instance, GuiGraphicsExtractor graphics, Player player, int top, int right, Operation<Void> renderVanilla) {
		HudElementRegistryImpl.getRoot(VanillaHudElements.FOOD_BAR).extractRenderState(
				graphics, ctx -> renderVanilla.call(instance, ctx, player, top, right));
	}

	@WrapOperation(method = "extractPlayerHealth", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Hud;extractAirBubbles(Lnet/minecraft/client/gui/GuiGraphicsExtractor;Lnet/minecraft/world/entity/player/Player;III)V"))
	private void wrapAirBar(Hud instance, GuiGraphicsExtractor graphics, Player player, int heartCount, int top, int left, Operation<Void> renderVanilla) {
		HudElementRegistryImpl.getRoot(VanillaHudElements.AIR_BAR).extractRenderState(
				graphics, ctx -> renderVanilla.call(instance, ctx, player, heartCount, top, left));
	}

	@WrapOperation(method = "extractHotbarAndDecorations", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Hud;extractVehicleHealth(Lnet/minecraft/client/gui/GuiGraphicsExtractor;)V"))
	private void wrapMountHealth(Hud instance, GuiGraphicsExtractor graphics, Operation<Void> renderVanilla) {
		HudElementRegistryImpl.getRoot(VanillaHudElements.MOUNT_HEALTH).extractRenderState(
				graphics,
				ctx -> renderVanilla.call(instance, ctx));
	}

	@WrapOperation(method = "extractHotbarAndDecorations", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/contextualbar/ContextualBar;extractBackground(Lnet/minecraft/client/gui/GuiGraphicsExtractor;)V"))
	private void wrapExtractInfoBar(ContextualBar instance, GuiGraphicsExtractor graphics, Operation<Void> renderVanilla) {
		HudElementRegistryImpl.getRoot(VanillaHudElements.INFO_BAR).extractRenderState(
				graphics,
				ctx -> renderVanilla.call(instance, ctx));
	}

	@WrapOperation(method = "extractHotbarAndDecorations", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/contextualbar/ContextualBar;extractExperienceLevel(Lnet/minecraft/client/gui/GuiGraphicsExtractor;Lnet/minecraft/client/gui/Font;I)V"))
	private void wrapExperienceLevel(GuiGraphicsExtractor graphics, Font font, int level, Operation<Void> renderVanilla) {
		HudElementRegistryImpl.getRoot(VanillaHudElements.EXPERIENCE_LEVEL).extractRenderState(
				graphics,
				ctx -> renderVanilla.call(ctx, font, level));
	}

	@WrapOperation(method = "extractHotbarAndDecorations", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Hud;extractSelectedItemName(Lnet/minecraft/client/gui/GuiGraphicsExtractor;)V"))
	private void wrapHeldItemTooltip(Hud instance, GuiGraphicsExtractor graphics, Operation<Void> renderVanilla) {
		HudElementRegistryImpl.getRoot(VanillaHudElements.HELD_ITEM_TOOLTIP).extractRenderState(
				graphics,
				ctx -> renderVanilla.call(instance, ctx));
	}

	@WrapOperation(method = "extractRenderState", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Hud;extractEffects(Lnet/minecraft/client/gui/GuiGraphicsExtractor;)V"))
	private void wrapMobEffectOverlay(Hud instance, GuiGraphicsExtractor graphics, Operation<Void> renderVanilla) {
		HudElementRegistryImpl.getRoot(VanillaHudElements.MOB_EFFECTS).extractRenderState(
				graphics,
				ctx -> renderVanilla.call(instance, ctx));
	}

	@WrapOperation(method = "extractRenderState", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Hud;extractBossOverlay(Lnet/minecraft/client/gui/GuiGraphicsExtractor;)V"))
	private void wrapBossHealthOverlay(Hud instance, GuiGraphicsExtractor graphics, Operation<Void> renderVanilla) {
		HudElementRegistryImpl.getRoot(VanillaHudElements.BOSS_BAR).extractRenderState(
				graphics,
				ctx -> renderVanilla.call(instance, ctx));
	}

	@WrapOperation(method = "extractRenderState", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Hud;extractSleepOverlay(Lnet/minecraft/client/gui/GuiGraphicsExtractor;)V"))
	private void wrapSleepOverlay(Hud instance, GuiGraphicsExtractor graphics, Operation<Void> renderVanilla) {
		HudElementRegistryImpl.getRoot(VanillaHudElements.SLEEP).extractRenderState(graphics,
				ctx -> renderVanilla.call(instance, ctx));
	}

	@WrapOperation(method = "extractRenderState", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Hud;extractDemoOverlay(Lnet/minecraft/client/gui/GuiGraphicsExtractor;)V"))
	private void wrapDemoTimer(Hud instance, GuiGraphicsExtractor graphics, Operation<Void> renderVanilla) {
		HudElementRegistryImpl.getRoot(VanillaHudElements.DEMO_TIMER).extractRenderState(
				graphics,
				ctx -> renderVanilla.call(instance, ctx));
	}

	@WrapOperation(method = "extractRenderState", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Hud;extractScoreboardSidebar(Lnet/minecraft/client/gui/GuiGraphicsExtractor;)V"))
	private void wrapScoreboardSidebar(Hud instance, GuiGraphicsExtractor graphics, Operation<Void> renderVanilla) {
		HudElementRegistryImpl.getRoot(VanillaHudElements.SCOREBOARD).extractRenderState(
				graphics,
				ctx -> renderVanilla.call(instance, ctx));
	}

	@WrapOperation(method = "extractRenderState", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Hud;extractOverlayMessage(Lnet/minecraft/client/gui/GuiGraphicsExtractor;)V"))
	private void wrapOverlayMessage(Hud instance, GuiGraphicsExtractor graphics, Operation<Void> renderVanilla) {
		HudElementRegistryImpl.getRoot(VanillaHudElements.OVERLAY_MESSAGE).extractRenderState(
				graphics,
				ctx -> renderVanilla.call(instance, ctx));
	}

	@WrapOperation(method = "extractRenderState", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Hud;extractTitle(Lnet/minecraft/client/gui/GuiGraphicsExtractor;)V"))
	private void wrapTitleAndSubtitle(Hud instance, GuiGraphicsExtractor graphics, Operation<Void> renderVanilla) {
		HudElementRegistryImpl.getRoot(VanillaHudElements.TITLE_AND_SUBTITLE).extractRenderState(
				graphics,
				ctx -> renderVanilla.call(instance, ctx));
	}

	@WrapOperation(method = "extractRenderState", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Hud;extractChat(Lnet/minecraft/client/gui/GuiGraphicsExtractor;)V"))
	private void wrapChat(Hud instance, GuiGraphicsExtractor graphics, Operation<Void> renderVanilla) {
		HudElementRegistryImpl.getRoot(VanillaHudElements.CHAT).extractRenderState(
				graphics,
				ctx -> renderVanilla.call(instance, ctx));
	}

	@WrapOperation(method = "extractRenderState", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/components/PlayerTabOverlay;extractRenderState(Lnet/minecraft/client/gui/GuiGraphicsExtractor;)V"))
	private void wrapPlayerList(PlayerTabOverlay instance, GuiGraphicsExtractor graphics, Operation<Void> renderVanilla) {
		HudElementRegistryImpl.getRoot(VanillaHudElements.PLAYER_LIST).extractRenderState(
				graphics,
				ctx -> renderVanilla.call(instance, ctx));
	}
}
