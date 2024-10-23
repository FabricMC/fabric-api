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

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.LayeredDrawer;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.render.RenderTickCounter;

import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderEvents;

@Mixin(InGameHud.class)
public class InGameHudMixin {
	@Shadow
	@Final
	private MinecraftClient client;

	// Targeting the first addLayer call of the first layered drawer, currently the misc overlays layer (renderMiscOverlays) as of 1.21.
	@ModifyArg(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/LayeredDrawer;addLayer(Lnet/minecraft/client/gui/LayeredDrawer$Layer;)Lnet/minecraft/client/gui/LayeredDrawer;", ordinal = 0))
	private LayeredDrawer.Layer fabric$beforeStartAndAfterMiscOverlays(LayeredDrawer.Layer miscOverlaysLayer) {
		return (context, tickCounter) -> {
			HudRenderEvents.START.invoker().render(context, tickCounter);
			miscOverlaysLayer.render(context, tickCounter);
			HudRenderEvents.AFTER_MISC_OVERLAYS.invoker().render(context, tickCounter);
		};
	}

	// Targeting the last addLayer call of the first layered drawer, which is after the main hud, currently the boss bar layer (bossBarHud.render) as of 1.21.
	@ModifyArg(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/LayeredDrawer;addLayer(Lnet/minecraft/client/gui/LayeredDrawer$Layer;)Lnet/minecraft/client/gui/LayeredDrawer;", ordinal = 5))
	private LayeredDrawer.Layer fabric$afterMainHudExperienceLevelStatusEffectOverlayAndBossBar(LayeredDrawer.Layer experienceLevelLayer) {
		return (context, tickCounter) -> {
			experienceLevelLayer.render(context, tickCounter);
			HudRenderEvents.AFTER_MAIN_HUD.invoker().render(context, tickCounter);
		};
	}

	// Targeting the first addLayer call of the second layered drawer, currently the demo timer layer (renderDemoTimer) as of 1.21.
	@ModifyArg(method = "<init>", slice = @Slice(from = @At(value = "NEW", target = "Lnet/minecraft/client/gui/LayeredDrawer;", ordinal = 2)), at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/LayeredDrawer;addLayer(Lnet/minecraft/client/gui/LayeredDrawer$Layer;)Lnet/minecraft/client/gui/LayeredDrawer;", ordinal = 0))
	private LayeredDrawer.Layer fabric$afterSleepOverlay(LayeredDrawer.Layer demoTimerLayer) {
		return (context, tickCounter) -> {
			HudRenderEvents.AFTER_SLEEP_OVERLAY.invoker().render(context, tickCounter);
			demoTimerLayer.render(context, tickCounter);
		};
	}

	// Targeting the chat layer (renderChat), currently the sixth addLayer call of the second layered drawer as of 1.21.
	@ModifyArg(method = "<init>", slice = @Slice(from = @At(value = "NEW", target = "Lnet/minecraft/client/gui/LayeredDrawer;", ordinal = 2)), at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/LayeredDrawer;addLayer(Lnet/minecraft/client/gui/LayeredDrawer$Layer;)Lnet/minecraft/client/gui/LayeredDrawer;", ordinal = 5))
	private LayeredDrawer.Layer fabric$beforeChat(LayeredDrawer.Layer beforeChatLayer) {
		return (context, tickCounter) -> {
			HudRenderEvents.BEFORE_CHAT.invoker().render(context, tickCounter);
			beforeChatLayer.render(context, tickCounter);
		};
	}

	// Targeting the last addLayer call of the second layered drawer, currently the subtitles hud layer (subtitlesHud.render) as of 1.21.
	@ModifyArg(method = "<init>", slice = @Slice(from = @At(value = "NEW", target = "Lnet/minecraft/client/gui/LayeredDrawer;", ordinal = 2)), at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/LayeredDrawer;addLayer(Lnet/minecraft/client/gui/LayeredDrawer$Layer;)Lnet/minecraft/client/gui/LayeredDrawer;", ordinal = 7))
	private LayeredDrawer.Layer fabric$AfterSubtitlesHud(LayeredDrawer.Layer subtitlesHudLayer) {
		return (context, tickCounter) -> {
			subtitlesHudLayer.render(context, tickCounter);
			HudRenderEvents.LAST.invoker().render(context, tickCounter);
		};
	}

	// Inject after the HUD is rendered. Deprecated in favor of HudRenderEvents.
	@Deprecated
	@Inject(method = "render", at = @At(value = "TAIL"))
	public void render(DrawContext drawContext, RenderTickCounter tickCounter, CallbackInfo callbackInfo) {
		HudRenderCallback.EVENT.invoker().onHudRender(drawContext, tickCounter);
	}
}
