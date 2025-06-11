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

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.gui.hud.SpectatorHud;
import net.minecraft.client.gui.hud.bar.Bar;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.entity.player.PlayerEntity;

import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.fabricmc.fabric.impl.client.rendering.hud.HudElementRegistryImpl;

@Mixin(InGameHud.class)
abstract class InGameHudMixin {
	@Shadow
	@Final
	private MinecraftClient client;

	@Inject(method = "render", at = @At(value = "TAIL"))
	public void render(DrawContext drawContext, RenderTickCounter tickCounter, CallbackInfo callbackInfo) {
		HudRenderCallback.EVENT.invoker().onHudRender(drawContext, tickCounter);
	}

	@WrapOperation(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/hud/InGameHud;renderMiscOverlays(Lnet/minecraft/client/gui/DrawContext;Lnet/minecraft/client/render/RenderTickCounter;)V"))
	private void wrapMiscOverlays(InGameHud instance, DrawContext context, RenderTickCounter tickCounter, Operation<Void> renderVanilla) {
		HudElementRegistryImpl.getRoot(VanillaHudElements.MISC_OVERLAYS).render(context, tickCounter, (ctx, tc) -> renderVanilla.call(instance, ctx, tc));
	}

	@WrapOperation(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/hud/InGameHud;renderCrosshair(Lnet/minecraft/client/gui/DrawContext;Lnet/minecraft/client/render/RenderTickCounter;)V"))
	private void wrapCrosshair(InGameHud instance, DrawContext context, RenderTickCounter tickCounter, Operation<Void> renderVanilla) {
		HudElementRegistryImpl.getRoot(VanillaHudElements.CROSSHAIR).render(context, tickCounter, (ctx, tc) -> renderVanilla.call(instance, ctx, tc));
	}

	@WrapOperation(method = "renderMainHud", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/hud/SpectatorHud;renderSpectatorMenu(Lnet/minecraft/client/gui/DrawContext;)V"))
	private void wrapSpectatorMenu(SpectatorHud instance, DrawContext context, Operation<Void> renderVanilla, @Local(argsOnly = true) RenderTickCounter tickCounter) {
		HudElementRegistryImpl.getRoot(VanillaHudElements.SPECTATOR_MENU).render(context, tickCounter, (ctx, tc) -> renderVanilla.call(instance, ctx));
	}

	@WrapOperation(method = "renderMainHud", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/hud/InGameHud;renderHotbar(Lnet/minecraft/client/gui/DrawContext;Lnet/minecraft/client/render/RenderTickCounter;)V"))
	private void wrapHotbar(InGameHud instance, DrawContext context, RenderTickCounter tickCounter, Operation<Void> renderVanilla) {
		HudElementRegistryImpl.getRoot(VanillaHudElements.HOTBAR).render(context, tickCounter, (ctx, tc) -> renderVanilla.call(instance, ctx, tc));
	}

	@WrapOperation(method = "renderStatusBars", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/hud/InGameHud;renderArmor(Lnet/minecraft/client/gui/DrawContext;Lnet/minecraft/entity/player/PlayerEntity;IIII)V"))
	private void wrapArmorBar(DrawContext context, PlayerEntity player, int i, int j, int k, int x, Operation<Void> renderVanilla) {
		HudElementRegistryImpl.getRoot(VanillaHudElements.ARMOR_BAR).render(context, client.getRenderTickCounter(), (ctx, tc) -> renderVanilla.call(ctx, player, i, j, k, x));
	}

	@WrapOperation(method = "renderStatusBars", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/hud/InGameHud;renderHealthBar(Lnet/minecraft/client/gui/DrawContext;Lnet/minecraft/entity/player/PlayerEntity;IIIIFIIIZ)V"))
	private void wrapHealthBar(InGameHud instance, DrawContext context, PlayerEntity player, int x, int y, int lines, int regeneratingHeartIndex, float maxHealth, int lastHealth, int health, int absorption, boolean blinking, Operation<Void> renderVanilla) {
		HudElementRegistryImpl.getRoot(VanillaHudElements.HEALTH_BAR).render(context, client.getRenderTickCounter(), (ctx, tc) -> renderVanilla.call(instance, ctx, player, x, y, lines, regeneratingHeartIndex, maxHealth, lastHealth, health, absorption, blinking));
	}

	@WrapOperation(method = "renderStatusBars", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/hud/InGameHud;renderFood(Lnet/minecraft/client/gui/DrawContext;Lnet/minecraft/entity/player/PlayerEntity;II)V"))
	private void wrapFoodBar(InGameHud instance, DrawContext context, PlayerEntity player, int top, int right, Operation<Void> renderVanilla) {
		HudElementRegistryImpl.getRoot(VanillaHudElements.FOOD_BAR).render(context, client.getRenderTickCounter(), (ctx, tc) -> renderVanilla.call(instance, ctx, player, top, right));
	}

	@WrapOperation(method = "renderStatusBars", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/hud/InGameHud;renderAirBubbles(Lnet/minecraft/client/gui/DrawContext;Lnet/minecraft/entity/player/PlayerEntity;III)V"))
	private void wrapAirBar(InGameHud instance, DrawContext context, PlayerEntity player, int heartCount, int top, int left, Operation<Void> renderVanilla) {
		HudElementRegistryImpl.getRoot(VanillaHudElements.AIR_BAR).render(context, client.getRenderTickCounter(), (ctx, tc) -> renderVanilla.call(instance, ctx, player, heartCount, top, left));
	}

	@WrapOperation(method = "renderMainHud", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/hud/InGameHud;renderMountHealth(Lnet/minecraft/client/gui/DrawContext;)V"))
	private void wrapMountHealth(InGameHud instance, DrawContext context, Operation<Void> renderVanilla, @Local(argsOnly = true) RenderTickCounter tickCounter) {
		HudElementRegistryImpl.getRoot(VanillaHudElements.MOUNT_HEALTH).render(context, tickCounter, (ctx, tc) -> renderVanilla.call(instance, ctx));
	}

	@WrapOperation(method = "renderMainHud", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/hud/bar/Bar;renderBar(Lnet/minecraft/client/gui/DrawContext;Lnet/minecraft/client/render/RenderTickCounter;)V"))
	private void wrapRenderBar(Bar instance, DrawContext context, RenderTickCounter tickCounter, Operation<Void> renderVanilla) {
		HudElementRegistryImpl.getRoot(VanillaHudElements.INFO_BAR).render(context, tickCounter, (ctx, tc) -> renderVanilla.call(instance, ctx, tc));
	}

	@WrapOperation(method = "renderMainHud", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/hud/bar/Bar;drawExperienceLevel(Lnet/minecraft/client/gui/DrawContext;Lnet/minecraft/client/font/TextRenderer;I)V"))
	private void wrapExperienceLevel(DrawContext context, TextRenderer textRenderer, int level, Operation<Void> renderVanilla, @Local(argsOnly = true) RenderTickCounter tickCounter) {
		HudElementRegistryImpl.getRoot(VanillaHudElements.EXPERIENCE_LEVEL).render(context, tickCounter, (ctx, tc) -> renderVanilla.call(ctx, textRenderer, level));
	}

	@WrapOperation(method = "renderMainHud", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/hud/InGameHud;renderHeldItemTooltip(Lnet/minecraft/client/gui/DrawContext;)V"))
	private void wrapHeldItemTooltip(InGameHud instance, DrawContext context, Operation<Void> renderVanilla, @Local(argsOnly = true) RenderTickCounter tickCounter) {
		HudElementRegistryImpl.getRoot(VanillaHudElements.HELD_ITEM_TOOLTIP).render(context, tickCounter, (ctx, tc) -> renderVanilla.call(instance, ctx));
	}

	@WrapOperation(method = "renderMainHud", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/hud/SpectatorHud;render(Lnet/minecraft/client/gui/DrawContext;)V"))
	private void wrapRenderSpectatorHud(SpectatorHud instance, DrawContext context, Operation<Void> renderVanilla, @Local(argsOnly = true) RenderTickCounter tickCounter) {
		HudElementRegistryImpl.getRoot(VanillaHudElements.SPECTATOR_TOOLTIP).render(context, tickCounter, (ctx, tc) -> renderVanilla.call(instance, ctx));
	}

	@WrapOperation(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/hud/InGameHud;renderStatusEffectOverlay(Lnet/minecraft/client/gui/DrawContext;Lnet/minecraft/client/render/RenderTickCounter;)V"))
	private void wrapStatusEffectOverlay(InGameHud instance, DrawContext context, RenderTickCounter tickCounter, Operation<Void> renderVanilla) {
		HudElementRegistryImpl.getRoot(VanillaHudElements.STATUS_EFFECTS).render(context, tickCounter, (ctx, tc) -> renderVanilla.call(instance, ctx, tc));
	}

	@WrapOperation(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/hud/InGameHud;renderBossBarHud(Lnet/minecraft/client/gui/DrawContext;Lnet/minecraft/client/render/RenderTickCounter;)V"))
	private void wrapBossBarHud(InGameHud instance, DrawContext context, RenderTickCounter tickCounter, Operation<Void> renderVanilla) {
		HudElementRegistryImpl.getRoot(VanillaHudElements.BOSS_BAR).render(context, tickCounter, (ctx, tc) -> renderVanilla.call(instance, ctx, tc));
	}

	@WrapOperation(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/hud/InGameHud;renderSleepOverlay(Lnet/minecraft/client/gui/DrawContext;Lnet/minecraft/client/render/RenderTickCounter;)V"))
	private void wrapSleepOverlay(InGameHud instance, DrawContext context, RenderTickCounter tickCounter, Operation<Void> renderVanilla) {
		HudElementRegistryImpl.getRoot(VanillaHudElements.SLEEP).render(context, tickCounter, (ctx, tc) -> renderVanilla.call(instance, ctx, tc));
	}

	@WrapOperation(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/hud/InGameHud;renderDemoTimer(Lnet/minecraft/client/gui/DrawContext;Lnet/minecraft/client/render/RenderTickCounter;)V"))
	private void wrapDemoTimer(InGameHud instance, DrawContext context, RenderTickCounter tickCounter, Operation<Void> renderVanilla) {
		HudElementRegistryImpl.getRoot(VanillaHudElements.DEMO_TIMER).render(context, tickCounter, (ctx, tc) -> renderVanilla.call(instance, ctx, tc));
	}

	@WrapOperation(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/hud/InGameHud;renderDebugHud(Lnet/minecraft/client/gui/DrawContext;Lnet/minecraft/client/render/RenderTickCounter;)V"))
	private void wrapDebugHud(InGameHud instance, DrawContext context, RenderTickCounter tickCounter, Operation<Void> renderVanilla) {
		HudElementRegistryImpl.getRoot(VanillaHudElements.DEBUG).render(context, tickCounter, (ctx, tc) -> renderVanilla.call(instance, ctx, tc));
	}

	@WrapOperation(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/hud/InGameHud;renderScoreboardSidebar(Lnet/minecraft/client/gui/DrawContext;Lnet/minecraft/client/render/RenderTickCounter;)V"))
	private void wrapScoreboardSidebar(InGameHud instance, DrawContext context, RenderTickCounter tickCounter, Operation<Void> renderVanilla) {
		HudElementRegistryImpl.getRoot(VanillaHudElements.SCOREBOARD).render(context, tickCounter, (ctx, tc) -> renderVanilla.call(instance, ctx, tc));
	}

	@WrapOperation(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/hud/InGameHud;renderOverlayMessage(Lnet/minecraft/client/gui/DrawContext;Lnet/minecraft/client/render/RenderTickCounter;)V"))
	private void wrapOverlayMessage(InGameHud instance, DrawContext context, RenderTickCounter tickCounter, Operation<Void> renderVanilla) {
		HudElementRegistryImpl.getRoot(VanillaHudElements.OVERLAY_MESSAGE).render(context, tickCounter, (ctx, tc) -> renderVanilla.call(instance, ctx, tc));
	}

	@WrapOperation(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/hud/InGameHud;renderTitleAndSubtitle(Lnet/minecraft/client/gui/DrawContext;Lnet/minecraft/client/render/RenderTickCounter;)V"))
	private void wrapTitleAndSubtitle(InGameHud instance, DrawContext context, RenderTickCounter tickCounter, Operation<Void> renderVanilla) {
		HudElementRegistryImpl.getRoot(VanillaHudElements.TITLE_AND_SUBTITLE).render(context, tickCounter, (ctx, tc) -> renderVanilla.call(instance, ctx, tc));
	}

	@WrapOperation(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/hud/InGameHud;renderChat(Lnet/minecraft/client/gui/DrawContext;Lnet/minecraft/client/render/RenderTickCounter;)V"))
	private void wrapChat(InGameHud instance, DrawContext context, RenderTickCounter tickCounter, Operation<Void> renderVanilla) {
		HudElementRegistryImpl.getRoot(VanillaHudElements.CHAT).render(context, tickCounter, (ctx, tc) -> renderVanilla.call(instance, ctx, tc));
	}

	@WrapOperation(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/hud/InGameHud;renderPlayerList(Lnet/minecraft/client/gui/DrawContext;Lnet/minecraft/client/render/RenderTickCounter;)V"))
	private void wrapPlayerList(InGameHud instance, DrawContext context, RenderTickCounter tickCounter, Operation<Void> renderVanilla) {
		HudElementRegistryImpl.getRoot(VanillaHudElements.PLAYER_LIST).render(context, tickCounter, (ctx, tc) -> renderVanilla.call(instance, ctx, tc));
	}

	@WrapOperation(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/hud/InGameHud;renderSubtitlesHud(Lnet/minecraft/client/gui/DrawContext;Lnet/minecraft/client/render/RenderTickCounter;)V"))
	private void wrapSubtitlesHud(InGameHud instance, DrawContext context, RenderTickCounter tickCounter, Operation<Void> renderVanilla) {
		HudElementRegistryImpl.getRoot(VanillaHudElements.SUBTITLES).render(context, tickCounter, (ctx, tc) -> renderVanilla.call(instance, ctx, tc));
	}
}
