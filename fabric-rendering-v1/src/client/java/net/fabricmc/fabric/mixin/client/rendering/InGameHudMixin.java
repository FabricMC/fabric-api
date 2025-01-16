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

import static net.fabricmc.fabric.api.client.rendering.v1.IdentifiedLayer.BOSS_BAR;
import static net.fabricmc.fabric.api.client.rendering.v1.IdentifiedLayer.CHAT;
import static net.fabricmc.fabric.api.client.rendering.v1.IdentifiedLayer.CROSSHAIR;
import static net.fabricmc.fabric.api.client.rendering.v1.IdentifiedLayer.DEBUG;
import static net.fabricmc.fabric.api.client.rendering.v1.IdentifiedLayer.DEMO_TIMER;
import static net.fabricmc.fabric.api.client.rendering.v1.IdentifiedLayer.EXPERIENCE_LEVEL;
import static net.fabricmc.fabric.api.client.rendering.v1.IdentifiedLayer.HOTBAR_AND_BARS;
import static net.fabricmc.fabric.api.client.rendering.v1.IdentifiedLayer.MISC_OVERLAYS;
import static net.fabricmc.fabric.api.client.rendering.v1.IdentifiedLayer.OVERLAY_MESSAGE;
import static net.fabricmc.fabric.api.client.rendering.v1.IdentifiedLayer.PLAYER_LIST;
import static net.fabricmc.fabric.api.client.rendering.v1.IdentifiedLayer.SCOREBOARD;
import static net.fabricmc.fabric.api.client.rendering.v1.IdentifiedLayer.SLEEP;
import static net.fabricmc.fabric.api.client.rendering.v1.IdentifiedLayer.STATUS_EFFECTS;
import static net.fabricmc.fabric.api.client.rendering.v1.IdentifiedLayer.SUBTITLES;
import static net.fabricmc.fabric.api.client.rendering.v1.IdentifiedLayer.TITLE_AND_SUBTITLE;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.LayeredDrawer;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.util.Identifier;

import net.fabricmc.fabric.api.client.rendering.v1.HudLayerRegistrationCallback;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.client.rendering.v1.IdentifiedLayer;
import net.fabricmc.fabric.impl.client.rendering.LayeredDrawerWrapperImpl;

@Mixin(InGameHud.class)
public class InGameHudMixin {
	@Shadow
	@Final
	private LayeredDrawer layeredDrawer;

	@Inject(method = "render", at = @At(value = "TAIL"))
	public void render(DrawContext drawContext, RenderTickCounter tickCounter, CallbackInfo callbackInfo) {
		HudRenderCallback.EVENT.invoker().onHudRender(drawContext, tickCounter);
	}

	@Redirect(method = "<init>", at = @At(
			value = "net.fabricmc.fabric.impl.client.rendering.LayerInjectionPoint",
			target = "Lnet/minecraft/client/gui/hud/InGameHud;renderMiscOverlays(Lnet/minecraft/client/gui/DrawContext;Lnet/minecraft/client/render/RenderTickCounter;)V")
	)
	private LayeredDrawer wrapMiscOverlays(LayeredDrawer instance, LayeredDrawer.Layer layer) {
		return wrap(MISC_OVERLAYS, instance, layer);
	}

	@Redirect(method = "<init>", at = @At(
			value = "net.fabricmc.fabric.impl.client.rendering.LayerInjectionPoint",
			target = "Lnet/minecraft/client/gui/hud/InGameHud;renderCrosshair(Lnet/minecraft/client/gui/DrawContext;Lnet/minecraft/client/render/RenderTickCounter;)V")
	)
	private LayeredDrawer wrapCrosshair(LayeredDrawer instance, LayeredDrawer.Layer layer) {
		return wrap(CROSSHAIR, instance, layer);
	}

	@Redirect(method = "<init>", at = @At(
			value = "net.fabricmc.fabric.impl.client.rendering.LayerInjectionPoint",
			target = "Lnet/minecraft/client/gui/hud/InGameHud;renderMainHud(Lnet/minecraft/client/gui/DrawContext;Lnet/minecraft/client/render/RenderTickCounter;)V")
	)
	private LayeredDrawer wrapHotbarAndBars(LayeredDrawer instance, LayeredDrawer.Layer layer) {
		return wrap(HOTBAR_AND_BARS, instance, layer);
	}

	@Redirect(method = "<init>", at = @At(
			value = "net.fabricmc.fabric.impl.client.rendering.LayerInjectionPoint",
			target = "Lnet/minecraft/client/gui/hud/InGameHud;renderExperienceLevel(Lnet/minecraft/client/gui/DrawContext;Lnet/minecraft/client/render/RenderTickCounter;)V")
	)
	private LayeredDrawer wrapExperienceLevel(LayeredDrawer instance, LayeredDrawer.Layer layer) {
		return wrap(EXPERIENCE_LEVEL, instance, layer);
	}

	@Redirect(method = "<init>", at = @At(
			value = "net.fabricmc.fabric.impl.client.rendering.LayerInjectionPoint",
			target = "Lnet/minecraft/client/gui/hud/InGameHud;renderStatusEffectOverlay(Lnet/minecraft/client/gui/DrawContext;Lnet/minecraft/client/render/RenderTickCounter;)V")
	)
	private LayeredDrawer wrapStatusEffects(LayeredDrawer instance, LayeredDrawer.Layer layer) {
		return wrap(STATUS_EFFECTS, instance, layer);
	}

	@Redirect(method = "<init>", at = @At(
			value = "net.fabricmc.fabric.impl.client.rendering.LayerInjectionPoint",
			target = "Lnet/minecraft/client/gui/hud/InGameHud;method_55808(Lnet/minecraft/client/gui/DrawContext;Lnet/minecraft/client/render/RenderTickCounter;)V")
	)
	private LayeredDrawer wrapBossBar(LayeredDrawer instance, LayeredDrawer.Layer layer) {
		return wrap(BOSS_BAR, instance, layer);
	}

	@Redirect(method = "<init>", at = @At(
			value = "net.fabricmc.fabric.impl.client.rendering.LayerInjectionPoint",
			target = "Lnet/minecraft/client/gui/hud/InGameHud;renderDemoTimer(Lnet/minecraft/client/gui/DrawContext;Lnet/minecraft/client/render/RenderTickCounter;)V")
	)
	private LayeredDrawer wrapDemoTimer(LayeredDrawer instance, LayeredDrawer.Layer layer) {
		return wrap(DEMO_TIMER, instance, layer);
	}

	@Redirect(method = "<init>", at = @At(
			value = "net.fabricmc.fabric.impl.client.rendering.LayerInjectionPoint",
			target = "Lnet/minecraft/client/gui/hud/InGameHud;method_55807(Lnet/minecraft/client/gui/DrawContext;Lnet/minecraft/client/render/RenderTickCounter;)V")
	)
	private LayeredDrawer wrapDebug(LayeredDrawer instance, LayeredDrawer.Layer layer) {
		return wrap(DEBUG, instance, layer);
	}

	@Redirect(method = "<init>", at = @At(
			value = "net.fabricmc.fabric.impl.client.rendering.LayerInjectionPoint",
			target = "Lnet/minecraft/client/gui/hud/InGameHud;renderScoreboardSidebar(Lnet/minecraft/client/gui/DrawContext;Lnet/minecraft/client/render/RenderTickCounter;)V")
	)
	private LayeredDrawer wrapScoreboard(LayeredDrawer instance, LayeredDrawer.Layer layer) {
		return wrap(SCOREBOARD, instance, layer);
	}

	@Redirect(method = "<init>", at = @At(
			value = "net.fabricmc.fabric.impl.client.rendering.LayerInjectionPoint",
			target = "Lnet/minecraft/client/gui/hud/InGameHud;renderOverlayMessage(Lnet/minecraft/client/gui/DrawContext;Lnet/minecraft/client/render/RenderTickCounter;)V")
	)
	private LayeredDrawer wrapOverlayMessage(LayeredDrawer instance, LayeredDrawer.Layer layer) {
		return wrap(OVERLAY_MESSAGE, instance, layer);
	}

	@Redirect(method = "<init>", at = @At(
			value = "net.fabricmc.fabric.impl.client.rendering.LayerInjectionPoint",
			target = "Lnet/minecraft/client/gui/hud/InGameHud;renderTitleAndSubtitle(Lnet/minecraft/client/gui/DrawContext;Lnet/minecraft/client/render/RenderTickCounter;)V")
	)
	private LayeredDrawer wrapTitleAndSubtitle(LayeredDrawer instance, LayeredDrawer.Layer layer) {
		return wrap(TITLE_AND_SUBTITLE, instance, layer);
	}

	@Redirect(method = "<init>", at = @At(
			value = "net.fabricmc.fabric.impl.client.rendering.LayerInjectionPoint",
			target = "Lnet/minecraft/client/gui/hud/InGameHud;renderChat(Lnet/minecraft/client/gui/DrawContext;Lnet/minecraft/client/render/RenderTickCounter;)V")
	)
	private LayeredDrawer wrapChat(LayeredDrawer instance, LayeredDrawer.Layer layer) {
		return wrap(CHAT, instance, layer);
	}

	@Redirect(method = "<init>", at = @At(
			value = "net.fabricmc.fabric.impl.client.rendering.LayerInjectionPoint",
			target = "Lnet/minecraft/client/gui/hud/InGameHud;renderPlayerList(Lnet/minecraft/client/gui/DrawContext;Lnet/minecraft/client/render/RenderTickCounter;)V")
	)
	private LayeredDrawer wrapPlayerList(LayeredDrawer instance, LayeredDrawer.Layer layer) {
		return wrap(PLAYER_LIST, instance, layer);
	}

	@Redirect(method = "<init>", at = @At(
			value = "net.fabricmc.fabric.impl.client.rendering.LayerInjectionPoint",
			target = "Lnet/minecraft/client/gui/hud/InGameHud;method_55806(Lnet/minecraft/client/gui/DrawContext;Lnet/minecraft/client/render/RenderTickCounter;)V")
	)
	private LayeredDrawer wrapSubtitlesHud(LayeredDrawer instance, LayeredDrawer.Layer layer) {
		return wrap(SUBTITLES, instance, layer);
	}

	@Redirect(method = "<init>",
			at = @At(
				value = "net.fabricmc.fabric.impl.client.rendering.LayerInjectionPoint",
				target = "Lnet/minecraft/client/gui/hud/InGameHud;renderSleepOverlay(Lnet/minecraft/client/gui/DrawContext;Lnet/minecraft/client/render/RenderTickCounter;)V"
			)
	)
	private LayeredDrawer wrapSleepOverlay(LayeredDrawer instance, LayeredDrawer.Layer layer) {
		return wrap(SLEEP, instance, layer);
	}

	@Inject(method = "<init>", at = @At("RETURN"))
	private void registerLayers(CallbackInfo ci) {
		HudLayerRegistrationCallback.EVENT.invoker().register(new LayeredDrawerWrapperImpl(layeredDrawer));
	}

	@Unique
	private static LayeredDrawer wrap(Identifier identifier, LayeredDrawer instance, LayeredDrawer.Layer layer) {
		return instance.addLayer(IdentifiedLayer.of(identifier, layer));
	}
}
