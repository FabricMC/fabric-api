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

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.PostEffectProcessor;
import net.minecraft.client.render.BufferBuilderStorage;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.Frustum;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;

import net.fabricmc.fabric.api.client.rendering.v1.DimensionRenderingRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.InvalidateRenderStateCallback;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.fabricmc.fabric.impl.client.rendering.WorldRenderContextImpl;

@Mixin(WorldRenderer.class)
public abstract class WorldRendererMixin {
	@Final
	@Shadow
	private BufferBuilderStorage bufferBuilders;
	@Shadow private ClientWorld world;
	@Shadow private PostEffectProcessor transparencyPostProcessor;
	@Final
	@Shadow
	private MinecraftClient client;
	@Unique private final WorldRenderContextImpl context = new WorldRenderContextImpl();

	@Inject(method = "render", at = @At("HEAD"))
	private void beforeRender(RenderTickCounter tickCounter, boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer, LightmapTextureManager lightmapTextureManager, Matrix4f positionMatrix, Matrix4f projectionMatrix, CallbackInfo ci) {
		context.prepare((WorldRenderer) (Object) this, tickCounter, renderBlockOutline, camera, gameRenderer, lightmapTextureManager, projectionMatrix, positionMatrix, bufferBuilders.getEntityVertexConsumers(), world.getProfiler(), transparencyPostProcessor != null, world);
		WorldRenderEvents.START.invoker().onStart(context);
	}

	@Inject(method = "setupTerrain", at = @At("RETURN"))
	private void afterTerrainSetup(Camera camera, Frustum frustum, boolean hasForcedFrustum, boolean spectator, CallbackInfo ci) {
		context.setFrustum(frustum);
		WorldRenderEvents.AFTER_SETUP.invoker().afterSetup(context);
	}

	@Inject(
			method = "render",
			at = @At(
				value = "INVOKE",
				target = "Lnet/minecraft/client/render/WorldRenderer;renderLayer(Lnet/minecraft/client/render/RenderLayer;DDDLorg/joml/Matrix4f;Lorg/joml/Matrix4f;)V",
				ordinal = 2,
				shift = Shift.AFTER
			)
	)
	private void afterTerrainSolid(CallbackInfo ci) {
		WorldRenderEvents.BEFORE_ENTITIES.invoker().beforeEntities(context);
	}

	@ModifyExpressionValue(method = "render", at = @At(value = "NEW", target = "net/minecraft/client/util/math/MatrixStack"))
	private MatrixStack setMatrixStack(MatrixStack matrixStack) {
		context.setMatrixStack(matrixStack);
		return matrixStack;
	}

	@Inject(method = "render", at = @At(value = "CONSTANT", args = "stringValue=blockentities", ordinal = 0))
	private void afterEntities(CallbackInfo ci) {
		WorldRenderEvents.AFTER_ENTITIES.invoker().afterEntities(context);
	}

	@Inject(
			method = "render",
			at = @At(
				value = "FIELD",
				target = "Lnet/minecraft/client/MinecraftClient;crosshairTarget:Lnet/minecraft/util/hit/HitResult;",
				shift = At.Shift.AFTER,
				ordinal = 1
			)
	)
	private void beforeRenderOutline(CallbackInfo ci) {
		context.renderBlockOutline = WorldRenderEvents.BEFORE_BLOCK_OUTLINE.invoker().beforeBlockOutline(context, client.crosshairTarget);
	}

	@SuppressWarnings("ConstantConditions")
	@Inject(method = "drawBlockOutline", at = @At("HEAD"), cancellable = true)
	private void onDrawBlockOutline(MatrixStack matrixStack, VertexConsumer vertexConsumer, Entity entity, double cameraX, double cameraY, double cameraZ, BlockPos blockPos, BlockState blockState, CallbackInfo ci) {
		if (!context.renderBlockOutline) {
			// Was cancelled before we got here, so do not
			// fire the BLOCK_OUTLINE event per contract of the API.
			ci.cancel();
		} else {
			context.prepareBlockOutline(entity, cameraX, cameraY, cameraZ, blockPos, blockState);

			if (!WorldRenderEvents.BLOCK_OUTLINE.invoker().onBlockOutline(context, context)) {
				ci.cancel();
			}
		}
	}

	@SuppressWarnings("ConstantConditions")
	@ModifyVariable(method = "drawBlockOutline", at = @At("HEAD"))
	private VertexConsumer resetBlockOutlineBuffer(VertexConsumer vertexConsumer) {
		// The original VertexConsumer may have been ended during the block outlines event, so we
		// have to re-request it to prevent a crash when the vanilla block overlay is submitted.
		return context.consumers().getBuffer(RenderLayer.getLines());
	}

	@Inject(
			method = "render",
			at = @At(
				value = "INVOKE",
				target = "Lnet/minecraft/client/render/debug/DebugRenderer;render(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider$Immediate;DDD)V",
				ordinal = 0
			)
	)
	private void beforeDebugRender(CallbackInfo ci) {
		WorldRenderEvents.BEFORE_DEBUG_RENDER.invoker().beforeDebugRender(context);
	}

	@Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/option/GameOptions;getCloudRenderModeValue()Lnet/minecraft/client/option/CloudRenderMode;"))
	private void beforeClouds(CallbackInfo ci) {
		WorldRenderEvents.AFTER_TRANSLUCENT.invoker().afterTranslucent(context);
	}

	@Inject(
			method = "render",
			at = @At(
				value = "INVOKE",
				target = "Lnet/minecraft/client/render/WorldRenderer;renderChunkDebugInfo(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;Lnet/minecraft/client/render/Camera;)V"
			)
	)
	private void onChunkDebugRender(CallbackInfo ci) {
		WorldRenderEvents.LAST.invoker().onLast(context);
	}

	@Inject(method = "render", at = @At("RETURN"))
	private void afterRender(CallbackInfo ci) {
		WorldRenderEvents.END.invoker().onEnd(context);
	}

	@Inject(method = "Lnet/minecraft/client/render/WorldRenderer;reload()V", at = @At("HEAD"))
	private void onReload(CallbackInfo ci) {
		InvalidateRenderStateCallback.EVENT.invoker().onInvalidate();
	}

	@Inject(at = @At("HEAD"), method = "renderWeather", cancellable = true)
	private void renderWeather(LightmapTextureManager manager, float tickDelta, double x, double y, double z, CallbackInfo info) {
		DimensionRenderingRegistry.WeatherRenderer renderer = DimensionRenderingRegistry.getWeatherRenderer(world.getRegistryKey());

		if (renderer != null) {
			renderer.render(context);
			info.cancel();
		} else if (this.client.world != null) {
			if (this.client.world.getDimensionEffects().renderWeather(context)) {
				info.cancel();
			}
		}
	}

	@Inject(at = @At("HEAD"), method = "renderClouds(Lnet/minecraft/client/util/math/MatrixStack;Lorg/joml/Matrix4f;Lorg/joml/Matrix4f;FDDD)V", cancellable = true)
	private void renderCloud(MatrixStack matrices, Matrix4f matrix4f, Matrix4f matrix4f2, float tickDelta, double cameraX, double cameraY, double cameraZ, CallbackInfo info) {
		DimensionRenderingRegistry.CloudRenderer renderer = DimensionRenderingRegistry.getCloudRenderer(world.getRegistryKey());

		if (renderer != null) {
			renderer.render(context);
			info.cancel();
		} else if (this.client.world != null) {
			if (this.client.world.getDimensionEffects().renderCloud(context)) {
				info.cancel();
			}
		}
	}

	@Inject(at = @At(value = "INVOKE", target = "Ljava/lang/Runnable;run()V", shift = At.Shift.AFTER, ordinal = 0), method = "renderSky(Lorg/joml/Matrix4f;Lorg/joml/Matrix4f;FLnet/minecraft/client/render/Camera;ZLjava/lang/Runnable;)V", cancellable = true)
	private void renderSky(Matrix4f matrix4f, Matrix4f matrix4f2, float tickDelta, Camera camera, boolean bl, Runnable runnable, CallbackInfo info) {
		DimensionRenderingRegistry.SkyRenderer renderer = DimensionRenderingRegistry.getSkyRenderer(world.getRegistryKey());

		if (renderer != null) {
			renderer.render(context);
			info.cancel();
		} else if (this.client.world != null) {
			if (this.client.world.getDimensionEffects().renderSky(context)) {
				info.cancel();
			}
		}
	}
}
