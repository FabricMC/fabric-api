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
import com.mojang.blaze3d.vertex.PoseStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.SkyRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.state.level.SkyRenderState;
import net.minecraft.world.level.MoonPhase;

import net.fabricmc.fabric.api.client.rendering.v1.level.sky.CelestialType;
import net.fabricmc.fabric.api.client.rendering.v1.level.sky.SkyRenderEvents;
import net.fabricmc.fabric.impl.client.rendering.level.sky.SkyExtractionContextImpl;
import net.fabricmc.fabric.impl.client.rendering.level.sky.SkyRenderContextImpl;

@Mixin(SkyRenderer.class)
public abstract class SkyRendererMixin {
	@Unique
	private final SkyRenderContextImpl skyRenderContext = new SkyRenderContextImpl();

	@Inject(method = "extractRenderState", at = @At("HEAD"))
	private void setupContext(ClientLevel level, float partialTicks, Camera camera, SkyRenderState skyRenderState, CallbackInfo ci) {
		final CameraRenderState cameraRenderState = new CameraRenderState();
		camera.extractRenderState(cameraRenderState, partialTicks);
		skyRenderContext.prepare((SkyRenderer) (Object) this, skyRenderState, cameraRenderState);
	}

	@Inject(method = "extractRenderState", at = @At("TAIL"))
	private void afterExtractSky(ClientLevel level, float partialTicks, Camera camera, SkyRenderState state, CallbackInfo ci) {
		SkyRenderEvents.END_EXTRACTION.invoker().execute(new SkyExtractionContextImpl(level, camera, state, partialTicks));
	}

	@WrapOperation(method = "renderSunMoonAndStars", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/SkyRenderer;renderSun(FLcom/mojang/blaze3d/vertex/PoseStack;)V"))
	private void onSunRender(SkyRenderer instance, float rainBrightness, PoseStack poseStack, Operation<Void> original) {
		final boolean cancelled = SkyRenderEvents.PRE_CELESTIAL.invoker().execute(skyRenderContext, CelestialType.SUN);
		if (!cancelled) {
			original.call(instance, rainBrightness, poseStack);
		}
		SkyRenderEvents.POST_CELESTIAL.invoker().execute(skyRenderContext, CelestialType.SUN, cancelled);
	}

	@WrapOperation(method = "renderSunMoonAndStars", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/SkyRenderer;renderMoon(Lnet/minecraft/world/level/MoonPhase;FLcom/mojang/blaze3d/vertex/PoseStack;)V"))
	private void onMoonRender(SkyRenderer instance, MoonPhase moonPhase, float rainBrightness, PoseStack poseStack, Operation<Void> original) {
		final boolean cancelled = SkyRenderEvents.PRE_CELESTIAL.invoker().execute(skyRenderContext, CelestialType.MOON);
		if (!cancelled) {
			original.call(instance, moonPhase, rainBrightness, poseStack);
		}
		SkyRenderEvents.POST_CELESTIAL.invoker().execute(skyRenderContext, CelestialType.MOON, cancelled);
	}

	@WrapOperation(method = "renderSunMoonAndStars", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/SkyRenderer;renderStars(FLcom/mojang/blaze3d/vertex/PoseStack;)V"))
	private void onStarsRender(SkyRenderer instance, float rainBrightness, PoseStack poseStack, Operation<Void> original) {
		final boolean cancelled = SkyRenderEvents.PRE_CELESTIAL.invoker().execute(skyRenderContext, CelestialType.STARS);
		if (!cancelled) {
			original.call(instance, rainBrightness, poseStack);
		}
		SkyRenderEvents.POST_CELESTIAL.invoker().execute(skyRenderContext, CelestialType.STARS, cancelled);
	}
}
