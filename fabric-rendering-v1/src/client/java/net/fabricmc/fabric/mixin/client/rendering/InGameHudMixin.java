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

import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.render.RenderTickCounter;

import net.fabricmc.fabric.api.client.rendering.v1.HudLayerRegistrationCallback;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.client.rendering.v1.IdentifiedLayer;
import net.fabricmc.fabric.impl.client.rendering.LayeredDrawerWrapperImpl;

@Mixin(InGameHud.class)
public abstract class InGameHudMixin {
	@Unique
	private LayeredDrawerWrapperImpl layeredDrawerWrapper;

	@Shadow
	protected abstract void renderMiscOverlays(DrawContext context, RenderTickCounter tickCounter);

	@Shadow
	protected abstract void renderCrosshair(DrawContext context, RenderTickCounter tickCounter);

	@Shadow
	protected abstract void renderMainHud(DrawContext context, RenderTickCounter tickCounter);

	@Shadow
	protected abstract void renderStatusEffectOverlay(DrawContext context, RenderTickCounter tickCounter);

	@Shadow
	protected abstract void renderBossBarHud(DrawContext context, RenderTickCounter tickCounter);

	@Shadow
	protected abstract void renderSleepOverlay(DrawContext context, RenderTickCounter tickCounter);

	@Shadow
	protected abstract void renderDemoTimer(DrawContext context, RenderTickCounter tickCounter);

	@Shadow
	protected abstract void renderDebugHud(DrawContext context, RenderTickCounter tickCounter);

	@Shadow
	protected abstract void renderScoreboardSidebar(DrawContext context, RenderTickCounter tickCounter);

	@Shadow
	protected abstract void renderOverlayMessage(DrawContext context, RenderTickCounter tickCounter);

	@Shadow
	protected abstract void renderTitleAndSubtitle(DrawContext context, RenderTickCounter tickCounter);

	@Shadow
	protected abstract void renderChat(DrawContext context, RenderTickCounter tickCounter);

	@Shadow
	protected abstract void renderPlayerList(DrawContext context, RenderTickCounter tickCounter);

	@Shadow
	protected abstract void renderSubtitlesHud(DrawContext context, RenderTickCounter tickCounter);

	@Inject(method = "render", at = @At(value = "TAIL"))
	public void render(DrawContext drawContext, RenderTickCounter tickCounter, CallbackInfo callbackInfo) {
		HudRenderCallback.EVENT.invoker().onHudRender(drawContext, tickCounter);
	}

	@Inject(method = "<init>", at = @At("RETURN"))
	private void registerLayers(CallbackInfo ci) {
		layeredDrawerWrapper = new LayeredDrawerWrapperImpl(List.of(
				new LayeredDrawerWrapperImpl.VanillaLayer(IdentifiedLayer.MISC_OVERLAYS, this::renderMiscOverlays),
				new LayeredDrawerWrapperImpl.VanillaLayer(IdentifiedLayer.CROSSHAIR, this::renderCrosshair),
				new LayeredDrawerWrapperImpl.VanillaLayer(IdentifiedLayer.HOTBAR_AND_BARS, this::renderMainHud),
				new LayeredDrawerWrapperImpl.VanillaLayer(IdentifiedLayer.STATUS_EFFECTS, this::renderStatusEffectOverlay),
				new LayeredDrawerWrapperImpl.VanillaLayer(IdentifiedLayer.BOSS_BAR, this::renderBossBarHud),
				new LayeredDrawerWrapperImpl.VanillaLayer(IdentifiedLayer.SLEEP, this::renderSleepOverlay),
				new LayeredDrawerWrapperImpl.VanillaLayer(IdentifiedLayer.DEMO_TIMER, this::renderDemoTimer),
				new LayeredDrawerWrapperImpl.VanillaLayer(IdentifiedLayer.DEBUG, this::renderDebugHud),
				new LayeredDrawerWrapperImpl.VanillaLayer(IdentifiedLayer.SCOREBOARD, this::renderScoreboardSidebar),
				new LayeredDrawerWrapperImpl.VanillaLayer(IdentifiedLayer.OVERLAY_MESSAGE, this::renderOverlayMessage),
				new LayeredDrawerWrapperImpl.VanillaLayer(IdentifiedLayer.TITLE_AND_SUBTITLE, this::renderTitleAndSubtitle),
				new LayeredDrawerWrapperImpl.VanillaLayer(IdentifiedLayer.CHAT, this::renderChat),
				new LayeredDrawerWrapperImpl.VanillaLayer(IdentifiedLayer.PLAYER_LIST, this::renderPlayerList),
				new LayeredDrawerWrapperImpl.VanillaLayer(IdentifiedLayer.SUBTITLES, this::renderSubtitlesHud)
		));
		HudLayerRegistrationCallback.EVENT.invoker().register(layeredDrawerWrapper);
	}

	@Redirect(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/hud/InGameHud;renderMiscOverlays(Lnet/minecraft/client/gui/DrawContext;Lnet/minecraft/client/render/RenderTickCounter;)V"))
	private void wrapMiscOverlays(InGameHud instance, DrawContext context, RenderTickCounter tickCounter) {
		layeredDrawerWrapper.getVanillaLayer(IdentifiedLayer.MISC_OVERLAYS).render(context, tickCounter);
	}

	@Redirect(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/hud/InGameHud;renderCrosshair(Lnet/minecraft/client/gui/DrawContext;Lnet/minecraft/client/render/RenderTickCounter;)V"))
	private void wrapCrosshair(InGameHud instance, DrawContext context, RenderTickCounter tickCounter) {
		layeredDrawerWrapper.getVanillaLayer(IdentifiedLayer.CROSSHAIR).render(context, tickCounter);
	}

	@Redirect(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/hud/InGameHud;renderMainHud(Lnet/minecraft/client/gui/DrawContext;Lnet/minecraft/client/render/RenderTickCounter;)V"))
	private void wrapMainHud(InGameHud instance, DrawContext context, RenderTickCounter tickCounter) {
		layeredDrawerWrapper.getVanillaLayer(IdentifiedLayer.HOTBAR_AND_BARS).render(context, tickCounter);
	}

	@Redirect(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/hud/InGameHud;renderStatusEffectOverlay(Lnet/minecraft/client/gui/DrawContext;Lnet/minecraft/client/render/RenderTickCounter;)V"))
	private void wrapStatusEffectOverlay(InGameHud instance, DrawContext context, RenderTickCounter tickCounter) {
		layeredDrawerWrapper.getVanillaLayer(IdentifiedLayer.STATUS_EFFECTS).render(context, tickCounter);
	}

	@Redirect(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/hud/InGameHud;renderBossBarHud(Lnet/minecraft/client/gui/DrawContext;Lnet/minecraft/client/render/RenderTickCounter;)V"))
	private void wrapBossBarHud(InGameHud instance, DrawContext context, RenderTickCounter tickCounter) {
		layeredDrawerWrapper.getVanillaLayer(IdentifiedLayer.BOSS_BAR).render(context, tickCounter);
	}

	@Redirect(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/hud/InGameHud;renderSleepOverlay(Lnet/minecraft/client/gui/DrawContext;Lnet/minecraft/client/render/RenderTickCounter;)V"))
	private void wrapSleepOverlay(InGameHud instance, DrawContext context, RenderTickCounter tickCounter) {
		layeredDrawerWrapper.getVanillaLayer(IdentifiedLayer.SLEEP).render(context, tickCounter);
	}

	@Redirect(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/hud/InGameHud;renderDemoTimer(Lnet/minecraft/client/gui/DrawContext;Lnet/minecraft/client/render/RenderTickCounter;)V"))
	private void wrapDemoTimer(InGameHud instance, DrawContext context, RenderTickCounter tickCounter) {
		layeredDrawerWrapper.getVanillaLayer(IdentifiedLayer.DEMO_TIMER).render(context, tickCounter);
	}

	@Redirect(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/hud/InGameHud;renderDebugHud(Lnet/minecraft/client/gui/DrawContext;Lnet/minecraft/client/render/RenderTickCounter;)V"))
	private void wrapDebugHud(InGameHud instance, DrawContext context, RenderTickCounter tickCounter) {
		layeredDrawerWrapper.getVanillaLayer(IdentifiedLayer.DEBUG).render(context, tickCounter);
	}

	@Redirect(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/hud/InGameHud;renderScoreboardSidebar(Lnet/minecraft/client/gui/DrawContext;Lnet/minecraft/client/render/RenderTickCounter;)V"))
	private void wrapScoreboardSidebar(InGameHud instance, DrawContext context, RenderTickCounter tickCounter) {
		layeredDrawerWrapper.getVanillaLayer(IdentifiedLayer.SCOREBOARD).render(context, tickCounter);
	}

	@Redirect(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/hud/InGameHud;renderOverlayMessage(Lnet/minecraft/client/gui/DrawContext;Lnet/minecraft/client/render/RenderTickCounter;)V"))
	private void wrapOverlayMessage(InGameHud instance, DrawContext context, RenderTickCounter tickCounter) {
		layeredDrawerWrapper.getVanillaLayer(IdentifiedLayer.OVERLAY_MESSAGE).render(context, tickCounter);
	}

	@Redirect(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/hud/InGameHud;renderTitleAndSubtitle(Lnet/minecraft/client/gui/DrawContext;Lnet/minecraft/client/render/RenderTickCounter;)V"))
	private void wrapTitleAndSubtitle(InGameHud instance, DrawContext context, RenderTickCounter tickCounter) {
		layeredDrawerWrapper.getVanillaLayer(IdentifiedLayer.TITLE_AND_SUBTITLE).render(context, tickCounter);
	}

	@Redirect(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/hud/InGameHud;renderChat(Lnet/minecraft/client/gui/DrawContext;Lnet/minecraft/client/render/RenderTickCounter;)V"))
	private void wrapChat(InGameHud instance, DrawContext context, RenderTickCounter tickCounter) {
		layeredDrawerWrapper.getVanillaLayer(IdentifiedLayer.CHAT).render(context, tickCounter);
	}

	@Redirect(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/hud/InGameHud;renderPlayerList(Lnet/minecraft/client/gui/DrawContext;Lnet/minecraft/client/render/RenderTickCounter;)V"))
	private void wrapPlayerList(InGameHud instance, DrawContext context, RenderTickCounter tickCounter) {
		layeredDrawerWrapper.getVanillaLayer(IdentifiedLayer.PLAYER_LIST).render(context, tickCounter);
	}

	@Redirect(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/hud/InGameHud;renderSubtitlesHud(Lnet/minecraft/client/gui/DrawContext;Lnet/minecraft/client/render/RenderTickCounter;)V"))
	private void wrapSubtitlesHud(InGameHud instance, DrawContext context, RenderTickCounter tickCounter) {
		layeredDrawerWrapper.getVanillaLayer(IdentifiedLayer.SUBTITLES).render(context, tickCounter);
	}
}
