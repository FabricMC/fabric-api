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

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.LayeredDrawer;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.render.RenderTickCounter;

import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderEvents;

@Mixin(InGameHud.class)
public class InGameHudMixin {
	@ModifyArg(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/LayeredDrawer;addLayer(Lnet/minecraft/client/gui/LayeredDrawer$Layer;)Lnet/minecraft/client/gui/LayeredDrawer;", ordinal = 0))
	private LayeredDrawer.Layer fabric$beforeStartAndAfterMiscOverlays(LayeredDrawer.Layer miscOverlaysLayer) {
		return (context, tickCounter) -> {
			HudRenderEvents.START.invoker().onRender(context, tickCounter);
			miscOverlaysLayer.render(context, tickCounter);
			HudRenderEvents.AFTER_MISC_OVERLAYS.invoker().onRender(context, tickCounter);
		};
	}

	@ModifyArg(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/LayeredDrawer;addLayer(Lnet/minecraft/client/gui/LayeredDrawer$Layer;)Lnet/minecraft/client/gui/LayeredDrawer;", ordinal = 3))
	private LayeredDrawer.Layer fabric$afterMainHudAndExperienceLevel(LayeredDrawer.Layer experienceLevelLayer) {
		return (context, tickCounter) -> {
			experienceLevelLayer.render(context, tickCounter);
			HudRenderEvents.AFTER_MAIN_HUD.invoker().onRender(context, tickCounter);
		};
	}

	@ModifyArg(method = "<init>", slice = @Slice(from = @At(value = "NEW", target = "Lnet/minecraft/client/gui/LayeredDrawer;", ordinal = 1)), at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/LayeredDrawer;addLayer(Lnet/minecraft/client/gui/LayeredDrawer$Layer;)Lnet/minecraft/client/gui/LayeredDrawer;", ordinal = 5))
	private LayeredDrawer.Layer fabric$beforeChat(LayeredDrawer.Layer beforeChatLayer) {
		return (context, tickCounter) -> {
			HudRenderEvents.BEFORE_CHAT.invoker().onRender(context, tickCounter);
			beforeChatLayer.render(context, tickCounter);
		};
	}

	@ModifyArg(method = "<init>", slice = @Slice(from = @At(value = "NEW", target = "Lnet/minecraft/client/gui/LayeredDrawer;", ordinal = 1)), at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/LayeredDrawer;addLayer(Lnet/minecraft/client/gui/LayeredDrawer$Layer;)Lnet/minecraft/client/gui/LayeredDrawer;", ordinal = 7))
	private LayeredDrawer.Layer fabric$AfterSubtitlesHud(LayeredDrawer.Layer subtitlesHudLayer) {
		return (context, tickCounter) -> {
			subtitlesHudLayer.render(context, tickCounter);
			HudRenderEvents.LAST.invoker().onRender(context, tickCounter);
		};
	}

	@Deprecated
	@Inject(method = "render", at = @At(value = "TAIL"))
	public void render(DrawContext drawContext, RenderTickCounter tickCounter, CallbackInfo callbackInfo) {
		HudRenderCallback.EVENT.invoker().onHudRender(drawContext, tickCounter);
	}
}
