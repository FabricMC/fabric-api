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
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import org.joml.Matrix4f;
import org.joml.Vector4f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.CloudRenderMode;
import net.minecraft.client.render.BufferBuilderStorage;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.DefaultFramebufferSet;
import net.minecraft.client.render.FrameGraphBuilder;
import net.minecraft.client.render.FramePass;
import net.minecraft.client.render.Frustum;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.util.ObjectAllocator;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

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
	@Final
	@Shadow
	private MinecraftClient client;
	@Shadow
	@Final
	private DefaultFramebufferSet framebufferSet;
	@Unique private final WorldRenderContextImpl context = new WorldRenderContextImpl();

	@Inject(method = "render", at = @At("HEAD"))
	private void beforeRender(ObjectAllocator objectAllocator, RenderTickCounter tickCounter, boolean renderBlockOutline, Camera camera, Matrix4f positionMatrix, Matrix4f projectionMatrix, GpuBufferSlice slice, Vector4f skyColor, boolean thinFog, CallbackInfo ci) {
		context.prepare((WorldRenderer) (Object) this, tickCounter, renderBlockOutline, camera, this.client.gameRenderer, positionMatrix, projectionMatrix, bufferBuilders.getEntityVertexConsumers(), MinecraftClient.isFabulousGraphicsOrBetter(), world);
		WorldRenderEvents.START.invoker().onStart(context);
	}

	@Inject(method = "setupTerrain", at = @At("RETURN"))
	private void afterTerrainSetup(Camera camera, Frustum frustum, boolean hasForcedFrustum, boolean spectator, CallbackInfo ci) {
		context.setFrustum(frustum);
	}

	@Inject(
			method = "method_62214",
			at = @At(
				value = "INVOKE_STRING",
				target = "Lnet/minecraft/util/profiler/Profiler;push(Ljava/lang/String;)V",
				args = "ldc=terrain",
				shift = Shift.AFTER
			) // Points to after profiler.push("terrain");
	)
	private void beforeTerrainSolid(CallbackInfo ci) {
		WorldRenderEvents.AFTER_SETUP.invoker().afterSetup(context);
	}

	@Inject(
			method = "method_62214",
			at = @At(
				value = "INVOKE",
				target = "Lnet/minecraft/client/render/WorldRenderer;renderEntities(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider$Immediate;Lnet/minecraft/client/render/Camera;Lnet/minecraft/client/render/RenderTickCounter;Ljava/util/List;)V"
			)
	)
	private void afterTerrainSolid(CallbackInfo ci) {
		WorldRenderEvents.BEFORE_ENTITIES.invoker().beforeEntities(context);
	}

	@ModifyExpressionValue(method = "method_62214", at = @At(value = "NEW", target = "net/minecraft/client/util/math/MatrixStack"))
	private MatrixStack setMatrixStack(MatrixStack matrixStack) {
		context.setMatrixStack(matrixStack);
		return matrixStack;
	}

	@Inject(method = "method_62214", at = @At(value = "CONSTANT", args = "stringValue=blockentities", ordinal = 0))
	private void afterEntities(CallbackInfo ci) {
		WorldRenderEvents.AFTER_ENTITIES.invoker().afterEntities(context);
	}

	@Inject(method = "renderTargetBlockOutline", at = @At("HEAD"))
	private void beforeRenderOutline(Camera camera, VertexConsumerProvider.Immediate vertexConsumers, MatrixStack matrices, boolean translucent, CallbackInfo ci) {
		context.setTranslucentBlockOutline(translucent);
		context.renderBlockOutline = WorldRenderEvents.BEFORE_BLOCK_OUTLINE.invoker().beforeBlockOutline(context, client.crosshairTarget);
	}

	@SuppressWarnings("ConstantConditions")
	@Inject(method = "renderTargetBlockOutline", at = @At(value = "INVOKE", target = "net/minecraft/client/option/GameOptions.getHighContrastBlockOutline()Lnet/minecraft/client/option/SimpleOption;"), cancellable = true)
	private void onDrawBlockOutline(Camera camera, VertexConsumerProvider.Immediate vertexConsumers, MatrixStack matrices, boolean translucent, CallbackInfo ci, @Local BlockPos blockPos, @Local BlockState blockState, @Local Vec3d cameraPos) {
		if (!context.renderBlockOutline) {
			// Was cancelled before we got here, so do not
			// fire the BLOCK_OUTLINE event per contract of the API.
			ci.cancel();
			return;
		}

		context.prepareBlockOutline(camera.getFocusedEntity(), cameraPos.x, cameraPos.y, cameraPos.z, blockPos, blockState);

		if (!WorldRenderEvents.BLOCK_OUTLINE.invoker().onBlockOutline(context, context)) {
			vertexConsumers.drawCurrentLayer();
			ci.cancel();
		}
	}

	@Inject(
			method = "method_62214",
			at = @At(
				value = "INVOKE",
				target = "Lnet/minecraft/client/render/debug/DebugRenderer;render(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/Frustum;Lnet/minecraft/client/render/VertexConsumerProvider$Immediate;DDD)V",
				ordinal = 0
			)
	)
	private void beforeDebugRender(CallbackInfo ci) {
		WorldRenderEvents.BEFORE_DEBUG_RENDER.invoker().beforeDebugRender(context);
	}

	@Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/option/GameOptions;getCloudRenderModeValue()Lnet/minecraft/client/option/CloudRenderMode;"))
	private void beforeClouds(CallbackInfo ci, @Local FrameGraphBuilder frameGraphBuilder) {
		FramePass afterTranslucentPass = frameGraphBuilder.createPass("afterTranslucent");
		framebufferSet.mainFramebuffer = afterTranslucentPass.transfer(framebufferSet.mainFramebuffer);

		afterTranslucentPass.setRenderer(() -> WorldRenderEvents.AFTER_TRANSLUCENT.invoker().afterTranslucent(context));
	}

	@Inject(method = "method_62214", at = @At("RETURN"))
	private void onFinishWritingFramebuffer(CallbackInfo ci) {
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
	private void renderWeather(FrameGraphBuilder frameGraphBuilder, Vec3d vec3d, float f, GpuBufferSlice fog, CallbackInfo info) {
		if (this.client.world != null) {
			DimensionRenderingRegistry.WeatherRenderer renderer = DimensionRenderingRegistry.getWeatherRenderer(world.getRegistryKey());

			if (renderer != null) {
				renderer.render(context);
				info.cancel();
			}
		}
	}

	@Inject(at = @At("HEAD"), method = "renderClouds", cancellable = true)
	private void renderCloud(FrameGraphBuilder frameGraphBuilder, CloudRenderMode cloudRenderMode, Vec3d vec3d, float f, int i, float g, CallbackInfo info) {
		if (this.client.world != null) {
			DimensionRenderingRegistry.CloudRenderer renderer = DimensionRenderingRegistry.getCloudRenderer(world.getRegistryKey());

			if (renderer != null) {
				renderer.render(context);
				info.cancel();
			}
		}
	}

	@Inject(at = @At(value = "HEAD"), method = "renderSky", cancellable = true)
	private void renderSky(FrameGraphBuilder frameGraphBuilder, Camera camera, float tickDelta, GpuBufferSlice fog, CallbackInfo info) {
		if (this.client.world != null) {
			DimensionRenderingRegistry.SkyRenderer renderer = DimensionRenderingRegistry.getSkyRenderer(world.getRegistryKey());

			if (renderer != null) {
				renderer.render(context);
				info.cancel();
			}
		}
	}
}
