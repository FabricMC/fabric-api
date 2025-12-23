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
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.spectator.SpectatorGui;
import net.minecraft.client.gui.contextualbar.ContextualBarRenderer;
import net.minecraft.world.entity.player.Player;

import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.fabricmc.fabric.impl.client.rendering.hud.HudElementRegistryImpl;

@Mixin(Gui.class)
abstract class GuiMixin {
	@Shadow
	@Final
	private Minecraft minecraft;

	@Inject(method = "render", at = @At(value = "TAIL"))
	public void render(GuiGraphics graphics, DeltaTracker deltaTracker, CallbackInfo callbackInfo) {
		HudRenderCallback.EVENT.invoker().onHudRender(graphics, deltaTracker);
	}

	@WrapOperation(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Gui;renderCameraOverlays(Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/client/DeltaTracker;)V"))
	private void wrapMiscOverlays(Gui instance, GuiGraphics graphics, DeltaTracker deltaTracker, Operation<Void> renderVanilla) {
		HudElementRegistryImpl.getRoot(VanillaHudElements.MISC_OVERLAYS).render(
				graphics,
				deltaTracker, (ctx, dt) -> renderVanilla.call(instance, ctx, dt));
	}

	@WrapOperation(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Gui;renderCrosshair(Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/client/DeltaTracker;)V"))
	private void wrapCrosshair(Gui instance, GuiGraphics graphics, DeltaTracker deltaTracker, Operation<Void> renderVanilla) {
		HudElementRegistryImpl.getRoot(VanillaHudElements.CROSSHAIR).render(
				graphics,
				deltaTracker, (ctx, dt) -> renderVanilla.call(instance, ctx, dt));
	}

	@WrapOperation(method = "renderHotbarAndDecorations", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/components/spectator/SpectatorGui;renderHotbar(Lnet/minecraft/client/gui/GuiGraphics;)V"))
	private void wrapSpectatorMenu(SpectatorGui instance, GuiGraphics graphics, Operation<Void> renderVanilla, @Local(argsOnly = true) DeltaTracker deltaTracker) {
		HudElementRegistryImpl.getRoot(VanillaHudElements.SPECTATOR_MENU).render(
				graphics,
				deltaTracker, (ctx, _) -> renderVanilla.call(instance, ctx));
	}

	@WrapOperation(method = "renderHotbarAndDecorations", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Gui;renderItemHotbar(Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/client/DeltaTracker;)V"))
	private void wrapHotbar(Gui instance, GuiGraphics graphics, DeltaTracker deltaTracker, Operation<Void> renderVanilla) {
		HudElementRegistryImpl.getRoot(VanillaHudElements.HOTBAR).render(
				graphics,
				deltaTracker, (ctx, dt) -> renderVanilla.call(instance, ctx, dt));
	}

	@WrapOperation(method = "renderPlayerHealth", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Gui;renderArmor(Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/world/entity/player/Player;IIII)V"))
	private void wrapArmorBar(GuiGraphics graphics, Player player, int i, int j, int k, int x, Operation<Void> renderVanilla) {
		HudElementRegistryImpl.getRoot(VanillaHudElements.ARMOR_BAR).render(
				graphics, minecraft.getDeltaTracker(), (ctx, _) -> renderVanilla.call(ctx, player, i, j, k, x));
	}

	@WrapOperation(method = "renderPlayerHealth", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Gui;renderHearts(Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/world/entity/player/Player;IIIIFIIIZ)V"))
	private void wrapHealthBar(Gui instance, GuiGraphics graphics, Player player, int x, int y, int lines, int regeneratingHeartIndex, float maxHealth, int lastHealth, int health, int absorption, boolean blinking, Operation<Void> renderVanilla) {
		HudElementRegistryImpl.getRoot(VanillaHudElements.HEALTH_BAR).render(
				graphics, minecraft.getDeltaTracker(), (ctx, _) -> renderVanilla.call(instance, ctx, player, x, y, lines, regeneratingHeartIndex, maxHealth, lastHealth, health, absorption, blinking));
	}

	@WrapOperation(method = "renderPlayerHealth", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Gui;renderFood(Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/world/entity/player/Player;II)V"))
	private void wrapFoodBar(Gui instance, GuiGraphics graphics, Player player, int top, int right, Operation<Void> renderVanilla) {
		HudElementRegistryImpl.getRoot(VanillaHudElements.FOOD_BAR).render(
				graphics, minecraft.getDeltaTracker(), (ctx, _) -> renderVanilla.call(instance, ctx, player, top, right));
	}

	@WrapOperation(method = "renderPlayerHealth", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Gui;renderAirBubbles(Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/world/entity/player/Player;III)V"))
	private void wrapAirBar(Gui instance, GuiGraphics graphics, Player player, int heartCount, int top, int left, Operation<Void> renderVanilla) {
		HudElementRegistryImpl.getRoot(VanillaHudElements.AIR_BAR).render(
				graphics, minecraft.getDeltaTracker(), (ctx, _) -> renderVanilla.call(instance, ctx, player, heartCount, top, left));
	}

	@WrapOperation(method = "renderHotbarAndDecorations", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Gui;renderVehicleHealth(Lnet/minecraft/client/gui/GuiGraphics;)V"))
	private void wrapMountHealth(Gui instance, GuiGraphics graphics, Operation<Void> renderVanilla, @Local(argsOnly = true) DeltaTracker deltaTracker) {
		HudElementRegistryImpl.getRoot(VanillaHudElements.MOUNT_HEALTH).render(
				graphics,
				deltaTracker, (ctx, _) -> renderVanilla.call(instance, ctx));
	}

	@WrapOperation(method = "renderHotbarAndDecorations", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/contextualbar/ContextualBarRenderer;renderBackground(Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/client/DeltaTracker;)V"))
	private void wrapRenderBar(ContextualBarRenderer instance, GuiGraphics graphics, DeltaTracker deltaTracker, Operation<Void> renderVanilla) {
		HudElementRegistryImpl.getRoot(VanillaHudElements.INFO_BAR).render(
				graphics,
				deltaTracker, (ctx, dt) -> renderVanilla.call(instance, ctx, dt));
	}

	@WrapOperation(method = "renderHotbarAndDecorations", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/contextualbar/ContextualBarRenderer;renderExperienceLevel(Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/client/gui/Font;I)V"))
	private void wrapExperienceLevel(GuiGraphics graphics, Font font, int level, Operation<Void> renderVanilla, @Local(argsOnly = true) DeltaTracker deltaTracker) {
		HudElementRegistryImpl.getRoot(VanillaHudElements.EXPERIENCE_LEVEL).render(
				graphics,
				deltaTracker, (ctx, _) -> renderVanilla.call(ctx, font, level));
	}

	@WrapOperation(method = "renderHotbarAndDecorations", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Gui;renderSelectedItemName(Lnet/minecraft/client/gui/GuiGraphics;)V"))
	private void wrapHeldItemTooltip(Gui instance, GuiGraphics graphics, Operation<Void> renderVanilla, @Local(argsOnly = true) DeltaTracker deltaTracker) {
		HudElementRegistryImpl.getRoot(VanillaHudElements.HELD_ITEM_TOOLTIP).render(
				graphics,
				deltaTracker, (ctx, _) -> renderVanilla.call(instance, ctx));
	}

	@WrapOperation(method = "renderHotbarAndDecorations", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/components/spectator/SpectatorGui;renderAction(Lnet/minecraft/client/gui/GuiGraphics;)V"))
	private void wrapRenderSpectatorGui(SpectatorGui instance, GuiGraphics graphics, Operation<Void> renderVanilla, @Local(argsOnly = true) DeltaTracker deltaTracker) {
		HudElementRegistryImpl.getRoot(VanillaHudElements.SPECTATOR_TOOLTIP).render(
				graphics,
				deltaTracker, (ctx, _) -> renderVanilla.call(instance, ctx));
	}

	@WrapOperation(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Gui;renderEffects(Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/client/DeltaTracker;)V"))
	private void wrapMobEffectOverlay(Gui instance, GuiGraphics graphics, DeltaTracker deltaTracker, Operation<Void> renderVanilla) {
		HudElementRegistryImpl.getRoot(VanillaHudElements.MOB_EFFECTS).render(
				graphics,
				deltaTracker, (ctx, dt) -> renderVanilla.call(instance, ctx, dt));
	}

	@WrapOperation(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Gui;renderBossOverlay(Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/client/DeltaTracker;)V"))
	private void wrapBossHealthOverlay(Gui instance, GuiGraphics graphics, DeltaTracker deltaTracker, Operation<Void> renderVanilla) {
		HudElementRegistryImpl.getRoot(VanillaHudElements.BOSS_BAR).render(
				graphics,
				deltaTracker, (ctx, dt) -> renderVanilla.call(instance, ctx, dt));
	}

	@WrapOperation(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Gui;renderSleepOverlay(Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/client/DeltaTracker;)V"))
	private void wrapSleepOverlay(Gui instance, GuiGraphics graphics, DeltaTracker deltaTracker, Operation<Void> renderVanilla) {
		HudElementRegistryImpl.getRoot(VanillaHudElements.SLEEP).render(graphics,
				deltaTracker, (ctx, dt) -> renderVanilla.call(instance, ctx, dt));
	}

	@WrapOperation(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Gui;renderDemoOverlay(Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/client/DeltaTracker;)V"))
	private void wrapDemoTimer(Gui instance, GuiGraphics graphics, DeltaTracker deltaTracker, Operation<Void> renderVanilla) {
		HudElementRegistryImpl.getRoot(VanillaHudElements.DEMO_TIMER).render(
				graphics,
				deltaTracker, (ctx, dt) -> renderVanilla.call(instance, ctx, dt));
	}

	@WrapOperation(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Gui;renderScoreboardSidebar(Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/client/DeltaTracker;)V"))
	private void wrapScoreboardSidebar(Gui instance, GuiGraphics graphics, DeltaTracker deltaTracker, Operation<Void> renderVanilla) {
		HudElementRegistryImpl.getRoot(VanillaHudElements.SCOREBOARD).render(
				graphics,
				deltaTracker, (ctx, dt) -> renderVanilla.call(instance, ctx, dt));
	}

	@WrapOperation(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Gui;renderOverlayMessage(Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/client/DeltaTracker;)V"))
	private void wrapOverlayMessage(Gui instance, GuiGraphics graphics, DeltaTracker deltaTracker, Operation<Void> renderVanilla) {
		HudElementRegistryImpl.getRoot(VanillaHudElements.OVERLAY_MESSAGE).render(
				graphics,
				deltaTracker, (ctx, dt) -> renderVanilla.call(instance, ctx, dt));
	}

	@WrapOperation(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Gui;renderTitle(Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/client/DeltaTracker;)V"))
	private void wrapTitleAndSubtitle(Gui instance, GuiGraphics graphics, DeltaTracker deltaTracker, Operation<Void> renderVanilla) {
		HudElementRegistryImpl.getRoot(VanillaHudElements.TITLE_AND_SUBTITLE).render(
				graphics,
				deltaTracker, (ctx, dt) -> renderVanilla.call(instance, ctx, dt));
	}

	@WrapOperation(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Gui;renderChat(Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/client/DeltaTracker;)V"))
	private void wrapChat(Gui instance, GuiGraphics graphics, DeltaTracker deltaTracker, Operation<Void> renderVanilla) {
		HudElementRegistryImpl.getRoot(VanillaHudElements.CHAT).render(
				graphics,
				deltaTracker, (ctx, dt) -> renderVanilla.call(instance, ctx,
				dt
		));
	}

	@WrapOperation(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Gui;renderTabList(Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/client/DeltaTracker;)V"))
	private void wrapPlayerList(Gui instance, GuiGraphics graphics, DeltaTracker deltaTracker, Operation<Void> renderVanilla) {
		HudElementRegistryImpl.getRoot(VanillaHudElements.PLAYER_LIST).render(
				graphics,
				deltaTracker, (ctx, dt) -> renderVanilla.call(instance, ctx,
				dt
		));
	}

	@WrapOperation(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Gui;renderSubtitleOverlay(Lnet/minecraft/client/gui/GuiGraphics;Z)V"))
	private void wrapSubtitleOverlay(Gui instance, GuiGraphics graphics, boolean deferRendering, Operation<Void> renderVanilla, @Local(argsOnly = true) DeltaTracker deltaTracker) {
		HudElementRegistryImpl.getRoot(VanillaHudElements.SUBTITLES).render(
				graphics,
				deltaTracker, (ctx, _) -> renderVanilla.call(instance, ctx,
						deferRendering
				));
	}
}
