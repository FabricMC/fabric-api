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
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.render.RenderTickCounter;

import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.fabricmc.fabric.impl.client.rendering.hud.HudElementRegistryImpl;

@Mixin(InGameHud.class)
public abstract class InGameHudMixin {
	@Inject(method = "render", at = @At(value = "TAIL"))
	public void render(DrawContext drawContext, RenderTickCounter tickCounter, CallbackInfo callbackInfo) {
		HudRenderCallback.EVENT.invoker().onHudRender(drawContext, tickCounter);
	}

	@WrapOperation(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/hud/InGameHud;renderMiscOverlays(Lnet/minecraft/client/gui/DrawContext;Lnet/minecraft/client/render/RenderTickCounter;)V"))
	private void wrapMiscOverlays(InGameHud instance, DrawContext context, RenderTickCounter tickCounter, Operation<Void> renderVanilla) {
		HudElementRegistryImpl.getRoot(VanillaHudElements.MISC_OVERLAYS).render(instance, context, tickCounter, renderVanilla);
	}

	@WrapOperation(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/hud/InGameHud;renderCrosshair(Lnet/minecraft/client/gui/DrawContext;Lnet/minecraft/client/render/RenderTickCounter;)V"))
	private void wrapCrosshair(InGameHud instance, DrawContext context, RenderTickCounter tickCounter, Operation<Void> renderVanilla) {
		HudElementRegistryImpl.getRoot(VanillaHudElements.CROSSHAIR).render(instance, context, tickCounter, renderVanilla);
	}

	@WrapOperation(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/hud/InGameHud;renderMainHud(Lnet/minecraft/client/gui/DrawContext;Lnet/minecraft/client/render/RenderTickCounter;)V"))
	private void wrapMainHud(InGameHud instance, DrawContext context, RenderTickCounter tickCounter, Operation<Void> renderVanilla) {
		HudElementRegistryImpl.getRoot(VanillaHudElements.HOTBAR_AND_BARS).render(instance, context, tickCounter, renderVanilla);
	}

	@WrapOperation(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/hud/InGameHud;renderStatusEffectOverlay(Lnet/minecraft/client/gui/DrawContext;Lnet/minecraft/client/render/RenderTickCounter;)V"))
	private void wrapStatusEffectOverlay(InGameHud instance, DrawContext context, RenderTickCounter tickCounter, Operation<Void> renderVanilla) {
		HudElementRegistryImpl.getRoot(VanillaHudElements.STATUS_EFFECTS).render(instance, context, tickCounter, renderVanilla);
	}

	@WrapOperation(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/hud/InGameHud;renderBossBarHud(Lnet/minecraft/client/gui/DrawContext;Lnet/minecraft/client/render/RenderTickCounter;)V"))
	private void wrapBossBarHud(InGameHud instance, DrawContext context, RenderTickCounter tickCounter, Operation<Void> renderVanilla) {
		HudElementRegistryImpl.getRoot(VanillaHudElements.BOSS_BAR).render(instance, context, tickCounter, renderVanilla);
	}

	@WrapOperation(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/hud/InGameHud;renderSleepOverlay(Lnet/minecraft/client/gui/DrawContext;Lnet/minecraft/client/render/RenderTickCounter;)V"))
	private void wrapSleepOverlay(InGameHud instance, DrawContext context, RenderTickCounter tickCounter, Operation<Void> renderVanilla) {
		HudElementRegistryImpl.getRoot(VanillaHudElements.SLEEP).render(instance, context, tickCounter, renderVanilla);
	}

	@WrapOperation(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/hud/InGameHud;renderDemoTimer(Lnet/minecraft/client/gui/DrawContext;Lnet/minecraft/client/render/RenderTickCounter;)V"))
	private void wrapDemoTimer(InGameHud instance, DrawContext context, RenderTickCounter tickCounter, Operation<Void> renderVanilla) {
		HudElementRegistryImpl.getRoot(VanillaHudElements.DEMO_TIMER).render(instance, context, tickCounter, renderVanilla);
	}

	@WrapOperation(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/hud/InGameHud;renderDebugHud(Lnet/minecraft/client/gui/DrawContext;Lnet/minecraft/client/render/RenderTickCounter;)V"))
	private void wrapDebugHud(InGameHud instance, DrawContext context, RenderTickCounter tickCounter, Operation<Void> renderVanilla) {
		HudElementRegistryImpl.getRoot(VanillaHudElements.DEBUG).render(instance, context, tickCounter, renderVanilla);
	}

	@WrapOperation(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/hud/InGameHud;renderScoreboardSidebar(Lnet/minecraft/client/gui/DrawContext;Lnet/minecraft/client/render/RenderTickCounter;)V"))
	private void wrapScoreboardSidebar(InGameHud instance, DrawContext context, RenderTickCounter tickCounter, Operation<Void> renderVanilla) {
		HudElementRegistryImpl.getRoot(VanillaHudElements.SCOREBOARD).render(instance, context, tickCounter, renderVanilla);
	}

	@WrapOperation(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/hud/InGameHud;renderOverlayMessage(Lnet/minecraft/client/gui/DrawContext;Lnet/minecraft/client/render/RenderTickCounter;)V"))
	private void wrapOverlayMessage(InGameHud instance, DrawContext context, RenderTickCounter tickCounter, Operation<Void> renderVanilla) {
		HudElementRegistryImpl.getRoot(VanillaHudElements.OVERLAY_MESSAGE).render(instance, context, tickCounter, renderVanilla);
	}

	@WrapOperation(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/hud/InGameHud;renderTitleAndSubtitle(Lnet/minecraft/client/gui/DrawContext;Lnet/minecraft/client/render/RenderTickCounter;)V"))
	private void wrapTitleAndSubtitle(InGameHud instance, DrawContext context, RenderTickCounter tickCounter, Operation<Void> renderVanilla) {
		HudElementRegistryImpl.getRoot(VanillaHudElements.TITLE_AND_SUBTITLE).render(instance, context, tickCounter, renderVanilla);
	}

	@WrapOperation(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/hud/InGameHud;renderChat(Lnet/minecraft/client/gui/DrawContext;Lnet/minecraft/client/render/RenderTickCounter;)V"))
	private void wrapChat(InGameHud instance, DrawContext context, RenderTickCounter tickCounter, Operation<Void> renderVanilla) {
		HudElementRegistryImpl.getRoot(VanillaHudElements.CHAT).render(instance, context, tickCounter, renderVanilla);
	}

	@WrapOperation(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/hud/InGameHud;renderPlayerList(Lnet/minecraft/client/gui/DrawContext;Lnet/minecraft/client/render/RenderTickCounter;)V"))
	private void wrapPlayerList(InGameHud instance, DrawContext context, RenderTickCounter tickCounter, Operation<Void> renderVanilla) {
		HudElementRegistryImpl.getRoot(VanillaHudElements.PLAYER_LIST).render(instance, context, tickCounter, renderVanilla);
	}

	@WrapOperation(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/hud/InGameHud;renderSubtitlesHud(Lnet/minecraft/client/gui/DrawContext;Lnet/minecraft/client/render/RenderTickCounter;)V"))
	private void wrapSubtitlesHud(InGameHud instance, DrawContext context, RenderTickCounter tickCounter, Operation<Void> renderVanilla) {
		HudElementRegistryImpl.getRoot(VanillaHudElements.SUBTITLES).render(instance, context, tickCounter, renderVanilla);
	}
}
