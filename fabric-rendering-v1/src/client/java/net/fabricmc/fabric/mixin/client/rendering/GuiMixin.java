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
import com.llamalad7.mixinextras.sugar.Local;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.spectator.SpectatorGui;
import net.minecraft.client.gui.contextualbar.ContextualBarRenderer;
import net.minecraft.world.entity.player.Player;

import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.fabricmc.fabric.impl.client.rendering.hud.HudElementRegistryImpl;

@Mixin(Gui.class)
abstract class GuiMixin {
	@Shadow
	@Final
	private Minecraft minecraft;

	@WrapOperation(method = "extractRenderState", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Gui;extractCameraOverlays(Lnet/minecraft/client/gui/GuiGraphicsExtractor;Lnet/minecraft/client/DeltaTracker;)V"))
	private void wrapMiscOverlays(Gui instance, GuiGraphicsExtractor graphics, DeltaTracker deltaTracker, Operation<Void> renderVanilla) {
		HudElementRegistryImpl.getRoot(VanillaHudElements.MISC_OVERLAYS).extractRenderState(
				graphics,
				deltaTracker, (ctx, dt) -> renderVanilla.call(instance, ctx, dt));
	}

	@WrapOperation(method = "extractRenderState", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Gui;extractCrosshair(Lnet/minecraft/client/gui/GuiGraphicsExtractor;Lnet/minecraft/client/DeltaTracker;)V"))
	private void wrapCrosshair(Gui instance, GuiGraphicsExtractor graphics, DeltaTracker deltaTracker, Operation<Void> renderVanilla) {
		HudElementRegistryImpl.getRoot(VanillaHudElements.CROSSHAIR).extractRenderState(
				graphics,
				deltaTracker, (ctx, dt) -> renderVanilla.call(instance, ctx, dt));
	}

	@WrapOperation(method = "extractHotbarAndDecorations", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/components/spectator/SpectatorGui;extractHotbar(Lnet/minecraft/client/gui/GuiGraphicsExtractor;)V"))
	private void wrapSpectatorMenu(SpectatorGui instance, GuiGraphicsExtractor graphics, Operation<Void> renderVanilla, @Local(argsOnly = true) DeltaTracker deltaTracker) {
		HudElementRegistryImpl.getRoot(VanillaHudElements.SPECTATOR_MENU).extractRenderState(
				graphics,
				deltaTracker, (ctx, _) -> renderVanilla.call(instance, ctx));
	}

	@WrapOperation(method = "extractHotbarAndDecorations", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Gui;extractItemHotbar(Lnet/minecraft/client/gui/GuiGraphicsExtractor;Lnet/minecraft/client/DeltaTracker;)V"))
	private void wrapHotbar(Gui instance, GuiGraphicsExtractor graphics, DeltaTracker deltaTracker, Operation<Void> renderVanilla) {
		HudElementRegistryImpl.getRoot(VanillaHudElements.HOTBAR).extractRenderState(
				graphics,
				deltaTracker, (ctx, dt) -> renderVanilla.call(instance, ctx, dt));
	}

	@WrapOperation(method = "extractPlayerHealth", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Gui;extractArmor(Lnet/minecraft/client/gui/GuiGraphicsExtractor;Lnet/minecraft/world/entity/player/Player;IIII)V"))
	private void wrapArmorBar(GuiGraphicsExtractor graphics, Player player, int i, int j, int k, int x, Operation<Void> renderVanilla) {
		HudElementRegistryImpl.getRoot(VanillaHudElements.ARMOR_BAR).extractRenderState(
				graphics, minecraft.getDeltaTracker(), (ctx, _) -> renderVanilla.call(ctx, player, i, j, k, x));
	}

	@WrapOperation(method = "extractPlayerHealth", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Gui;extractHearts(Lnet/minecraft/client/gui/GuiGraphicsExtractor;Lnet/minecraft/world/entity/player/Player;IIIIFIIIZ)V"))
	private void wrapHealthBar(Gui instance, GuiGraphicsExtractor graphics, Player player, int x, int y, int lines, int regeneratingHeartIndex, float maxHealth, int lastHealth, int health, int absorption, boolean blinking, Operation<Void> renderVanilla) {
		HudElementRegistryImpl.getRoot(VanillaHudElements.HEALTH_BAR).extractRenderState(
				graphics, minecraft.getDeltaTracker(), (ctx, _) -> renderVanilla.call(instance, ctx, player, x, y, lines, regeneratingHeartIndex, maxHealth, lastHealth, health, absorption, blinking));
	}

	@WrapOperation(method = "extractPlayerHealth", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Gui;extractFood(Lnet/minecraft/client/gui/GuiGraphicsExtractor;Lnet/minecraft/world/entity/player/Player;II)V"))
	private void wrapFoodBar(Gui instance, GuiGraphicsExtractor graphics, Player player, int top, int right, Operation<Void> renderVanilla) {
		HudElementRegistryImpl.getRoot(VanillaHudElements.FOOD_BAR).extractRenderState(
				graphics, minecraft.getDeltaTracker(), (ctx, _) -> renderVanilla.call(instance, ctx, player, top, right));
	}

	@WrapOperation(method = "extractPlayerHealth", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Gui;extractAirBubbles(Lnet/minecraft/client/gui/GuiGraphicsExtractor;Lnet/minecraft/world/entity/player/Player;III)V"))
	private void wrapAirBar(Gui instance, GuiGraphicsExtractor graphics, Player player, int heartCount, int top, int left, Operation<Void> renderVanilla) {
		HudElementRegistryImpl.getRoot(VanillaHudElements.AIR_BAR).extractRenderState(
				graphics, minecraft.getDeltaTracker(), (ctx, _) -> renderVanilla.call(instance, ctx, player, heartCount, top, left));
	}

	@WrapOperation(method = "extractHotbarAndDecorations", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Gui;extractVehicleHealth(Lnet/minecraft/client/gui/GuiGraphicsExtractor;)V"))
	private void wrapMountHealth(Gui instance, GuiGraphicsExtractor graphics, Operation<Void> renderVanilla, @Local(argsOnly = true) DeltaTracker deltaTracker) {
		HudElementRegistryImpl.getRoot(VanillaHudElements.MOUNT_HEALTH).extractRenderState(
				graphics,
				deltaTracker, (ctx, _) -> renderVanilla.call(instance, ctx));
	}

	@WrapOperation(method = "extractHotbarAndDecorations", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/contextualbar/ContextualBarRenderer;extractBackground(Lnet/minecraft/client/gui/GuiGraphicsExtractor;Lnet/minecraft/client/DeltaTracker;)V"))
	private void wrapExtractInfoBar(ContextualBarRenderer instance, GuiGraphicsExtractor graphics, DeltaTracker deltaTracker, Operation<Void> renderVanilla) {
		HudElementRegistryImpl.getRoot(VanillaHudElements.INFO_BAR).extractRenderState(
				graphics,
				deltaTracker, (ctx, dt) -> renderVanilla.call(instance, ctx, dt));
	}

	@WrapOperation(method = "extractHotbarAndDecorations", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/contextualbar/ContextualBarRenderer;extractExperienceLevel(Lnet/minecraft/client/gui/GuiGraphicsExtractor;Lnet/minecraft/client/gui/Font;I)V"))
	private void wrapExperienceLevel(GuiGraphicsExtractor graphics, Font font, int level, Operation<Void> renderVanilla, @Local(argsOnly = true) DeltaTracker deltaTracker) {
		HudElementRegistryImpl.getRoot(VanillaHudElements.EXPERIENCE_LEVEL).extractRenderState(
				graphics,
				deltaTracker, (ctx, _) -> renderVanilla.call(ctx, font, level));
	}

	@WrapOperation(method = "extractHotbarAndDecorations", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Gui;extractSelectedItemName(Lnet/minecraft/client/gui/GuiGraphicsExtractor;)V"))
	private void wrapHeldItemTooltip(Gui instance, GuiGraphicsExtractor graphics, Operation<Void> renderVanilla, @Local(argsOnly = true) DeltaTracker deltaTracker) {
		HudElementRegistryImpl.getRoot(VanillaHudElements.HELD_ITEM_TOOLTIP).extractRenderState(
				graphics,
				deltaTracker, (ctx, _) -> renderVanilla.call(instance, ctx));
	}

	@WrapOperation(method = "extractHotbarAndDecorations", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/components/spectator/SpectatorGui;extractAction(Lnet/minecraft/client/gui/GuiGraphicsExtractor;)V"))
	private void wrapExtractSpectatorGui(SpectatorGui instance, GuiGraphicsExtractor graphics, Operation<Void> renderVanilla, @Local(argsOnly = true) DeltaTracker deltaTracker) {
		HudElementRegistryImpl.getRoot(VanillaHudElements.SPECTATOR_TOOLTIP).extractRenderState(
				graphics,
				deltaTracker, (ctx, _) -> renderVanilla.call(instance, ctx));
	}

	@WrapOperation(method = "extractRenderState", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Gui;extractEffects(Lnet/minecraft/client/gui/GuiGraphicsExtractor;Lnet/minecraft/client/DeltaTracker;)V"))
	private void wrapMobEffectOverlay(Gui instance, GuiGraphicsExtractor graphics, DeltaTracker deltaTracker, Operation<Void> renderVanilla) {
		HudElementRegistryImpl.getRoot(VanillaHudElements.MOB_EFFECTS).extractRenderState(
				graphics,
				deltaTracker, (ctx, dt) -> renderVanilla.call(instance, ctx, dt));
	}

	@WrapOperation(method = "extractRenderState", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Gui;extractBossOverlay(Lnet/minecraft/client/gui/GuiGraphicsExtractor;Lnet/minecraft/client/DeltaTracker;)V"))
	private void wrapBossHealthOverlay(Gui instance, GuiGraphicsExtractor graphics, DeltaTracker deltaTracker, Operation<Void> renderVanilla) {
		HudElementRegistryImpl.getRoot(VanillaHudElements.BOSS_BAR).extractRenderState(
				graphics,
				deltaTracker, (ctx, dt) -> renderVanilla.call(instance, ctx, dt));
	}

	@WrapOperation(method = "extractRenderState", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Gui;extractSleepOverlay(Lnet/minecraft/client/gui/GuiGraphicsExtractor;Lnet/minecraft/client/DeltaTracker;)V"))
	private void wrapSleepOverlay(Gui instance, GuiGraphicsExtractor graphics, DeltaTracker deltaTracker, Operation<Void> renderVanilla) {
		HudElementRegistryImpl.getRoot(VanillaHudElements.SLEEP).extractRenderState(graphics,
				deltaTracker, (ctx, dt) -> renderVanilla.call(instance, ctx, dt));
	}

	@WrapOperation(method = "extractRenderState", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Gui;extractDemoOverlay(Lnet/minecraft/client/gui/GuiGraphicsExtractor;Lnet/minecraft/client/DeltaTracker;)V"))
	private void wrapDemoTimer(Gui instance, GuiGraphicsExtractor graphics, DeltaTracker deltaTracker, Operation<Void> renderVanilla) {
		HudElementRegistryImpl.getRoot(VanillaHudElements.DEMO_TIMER).extractRenderState(
				graphics,
				deltaTracker, (ctx, dt) -> renderVanilla.call(instance, ctx, dt));
	}

	@WrapOperation(method = "extractRenderState", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Gui;extractScoreboardSidebar(Lnet/minecraft/client/gui/GuiGraphicsExtractor;Lnet/minecraft/client/DeltaTracker;)V"))
	private void wrapScoreboardSidebar(Gui instance, GuiGraphicsExtractor graphics, DeltaTracker deltaTracker, Operation<Void> renderVanilla) {
		HudElementRegistryImpl.getRoot(VanillaHudElements.SCOREBOARD).extractRenderState(
				graphics,
				deltaTracker, (ctx, dt) -> renderVanilla.call(instance, ctx, dt));
	}

	@WrapOperation(method = "extractRenderState", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Gui;extractOverlayMessage(Lnet/minecraft/client/gui/GuiGraphicsExtractor;Lnet/minecraft/client/DeltaTracker;)V"))
	private void wrapOverlayMessage(Gui instance, GuiGraphicsExtractor graphics, DeltaTracker deltaTracker, Operation<Void> renderVanilla) {
		HudElementRegistryImpl.getRoot(VanillaHudElements.OVERLAY_MESSAGE).extractRenderState(
				graphics,
				deltaTracker, (ctx, dt) -> renderVanilla.call(instance, ctx, dt));
	}

	@WrapOperation(method = "extractRenderState", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Gui;extractTitle(Lnet/minecraft/client/gui/GuiGraphicsExtractor;Lnet/minecraft/client/DeltaTracker;)V"))
	private void wrapTitleAndSubtitle(Gui instance, GuiGraphicsExtractor graphics, DeltaTracker deltaTracker, Operation<Void> renderVanilla) {
		HudElementRegistryImpl.getRoot(VanillaHudElements.TITLE_AND_SUBTITLE).extractRenderState(
				graphics,
				deltaTracker, (ctx, dt) -> renderVanilla.call(instance, ctx, dt));
	}

	@WrapOperation(method = "extractRenderState", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Gui;extractChat(Lnet/minecraft/client/gui/GuiGraphicsExtractor;Lnet/minecraft/client/DeltaTracker;)V"))
	private void wrapChat(Gui instance, GuiGraphicsExtractor graphics, DeltaTracker deltaTracker, Operation<Void> renderVanilla) {
		HudElementRegistryImpl.getRoot(VanillaHudElements.CHAT).extractRenderState(
				graphics,
				deltaTracker, (ctx, dt) -> renderVanilla.call(instance, ctx,
				dt
		));
	}

	@WrapOperation(method = "extractRenderState", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Gui;extractTabList(Lnet/minecraft/client/gui/GuiGraphicsExtractor;Lnet/minecraft/client/DeltaTracker;)V"))
	private void wrapPlayerList(Gui instance, GuiGraphicsExtractor graphics, DeltaTracker deltaTracker, Operation<Void> renderVanilla) {
		HudElementRegistryImpl.getRoot(VanillaHudElements.PLAYER_LIST).extractRenderState(
				graphics,
				deltaTracker, (ctx, dt) -> renderVanilla.call(instance, ctx,
				dt
		));
	}
}
