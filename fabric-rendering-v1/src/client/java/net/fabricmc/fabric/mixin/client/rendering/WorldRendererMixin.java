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
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Vector4f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.BlockRenderLayerGroup;
import net.minecraft.client.render.BufferBuilderStorage;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.Frustum;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.render.SectionRenderState;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.WorldBorderRendering;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.render.command.OrderedRenderCommandQueueImpl;
import net.minecraft.client.render.debug.DebugRenderer;
import net.minecraft.client.render.state.WorldBorderRenderState;
import net.minecraft.client.render.state.WorldRenderState;
import net.minecraft.client.util.ObjectAllocator;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.border.WorldBorder;

import net.fabricmc.fabric.api.client.rendering.v1.InvalidateRenderStateCallback;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderEvents;
import net.fabricmc.fabric.impl.client.rendering.world.WorldRenderContextImpl;
import net.fabricmc.fabric.impl.client.rendering.world.WorldRendererHooks;

@Mixin(WorldRenderer.class)
public abstract class WorldRendererMixin implements WorldRendererHooks {
	@Shadow
	@Final
	private MinecraftClient client;
	@Shadow
	@Final
	private BufferBuilderStorage bufferBuilders;
	@Shadow
	@Final
	private WorldRenderState worldRenderState;
	@Shadow
	@Nullable
	private ClientWorld world;
	@Shadow
	@Final
	private OrderedRenderCommandQueueImpl entityRenderCommandQueue;

	@Unique
	private final WorldRenderContextImpl context = new WorldRenderContextImpl();
	@Unique
	private boolean isRendering = false;

	@Inject(method = "render", at = @At("HEAD"))
	private void beforeRender(ObjectAllocator allocator, RenderTickCounter tickCounter, boolean renderBlockOutline, Camera camera, Matrix4f positionMatrix, Matrix4f matrix4f, Matrix4f projectionMatrix, GpuBufferSlice fogBuffer, Vector4f fogColor, boolean renderSky, CallbackInfo ci) {
		context.prepare(client.gameRenderer, (WorldRenderer) (Object) this, worldRenderState, world, tickCounter, renderBlockOutline, camera, positionMatrix, projectionMatrix, entityRenderCommandQueue, bufferBuilders.getEntityVertexConsumers());
		isRendering = true;
	}

	@ModifyExpressionValue(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/WorldRenderer;setupFrustum(Lorg/joml/Matrix4f;Lorg/joml/Matrix4f;Lnet/minecraft/util/math/Vec3d;)Lnet/minecraft/client/render/Frustum;"))
	private Frustum onSetupFrustum(Frustum frustum) {
		context.setFrustum(frustum);
		return frustum;
	}

	@Inject(method = "fillEntityOutlineRenderStates", at = @At("RETURN"))
	private void afterBlockOutlineExtraction(Camera camera, WorldRenderState renderStates, CallbackInfo ci) {
		WorldRenderEvents.AFTER_BLOCK_OUTLINE_EXTRACTION.invoker().afterBlockOutlineExtraction(context, client.crosshairTarget);
	}

	@WrapOperation(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/WorldBorderRendering;updateRenderState(Lnet/minecraft/world/border/WorldBorder;Lnet/minecraft/util/math/Vec3d;DLnet/minecraft/client/render/state/WorldBorderRenderState;)V"))
	private void onWorldBorderExtraction(WorldBorderRendering instance, WorldBorder worldBorder, Vec3d vec3d, double d, WorldBorderRenderState worldBorderRenderState, Operation<Void> original) {
		original.call(instance, worldBorder, vec3d, d, worldBorderRenderState);
		WorldRenderEvents.END_EXTRACTION.invoker().endExtraction(context);
	}

	@WrapOperation(method = "method_62214",
			slice = @Slice(from = @At(value = "INVOKE", target = "renderBlockLayers")),
			at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/SectionRenderState;renderSection(Lnet/minecraft/client/render/BlockRenderLayerGroup;)V", ordinal = 0)
	)
	private void onTerrainRender(SectionRenderState instance, BlockRenderLayerGroup group, Operation<Void> original) {
		WorldRenderEvents.START_RENDER.invoker().startRender(context);
		original.call(instance, group);
		WorldRenderEvents.AFTER_TERRAIN_RENDER.invoker().afterTerrainRender(context);
	}

	@ModifyExpressionValue(method = "method_62214", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/WorldRenderer;renderBlockLayers(Lorg/joml/Matrix4fc;DDD)Lnet/minecraft/client/render/SectionRenderState;"))
	private SectionRenderState onRenderBlockLayers(SectionRenderState sectionRenderState) {
		context.setSectionRenderState(sectionRenderState);
		return sectionRenderState;
	}

	@ModifyExpressionValue(method = "method_62214", at = @At(value = "NEW", target = "Lnet/minecraft/client/util/math/MatrixStack;"))
	private MatrixStack onCreateMatrixStack(MatrixStack matrixStack) {
		context.setMatrixStack(matrixStack);
		return matrixStack;
	}

	@Inject(method = "method_62214", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/WorldRenderer;pushEntityRenders(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/state/WorldRenderState;Lnet/minecraft/client/render/command/OrderedRenderCommandQueue;)V"))
	private void onPushEntityRenders(CallbackInfo ci) {
		WorldRenderEvents.BEFORE_SUBMIT_ENTITY_COMMANDS.invoker().beforeSubmitEntityCommands(context);
	}

	@WrapOperation(method = "method_62214", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/WorldRenderer;renderBlockEntities(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/state/WorldRenderState;Lnet/minecraft/client/render/command/OrderedRenderCommandQueueImpl;)V"))
	private void onPushBlockEntityRenders(WorldRenderer instance, MatrixStack matrices, WorldRenderState worldRenderState, OrderedRenderCommandQueueImpl commandQueue, Operation<Void> original) {
		original.call(instance, matrices, worldRenderState, commandQueue);
		WorldRenderEvents.AFTER_SUBMIT_ENTITY_COMMANDS.invoker().afterSubmitEntityCommands(context);
	}

	@WrapOperation(method = "method_62214",
			slice = @Slice(from = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/command/RenderDispatcher;render()V")),
			at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/VertexConsumerProvider$Immediate;drawCurrentLayer()V")
	)
	private void onEntityRender(VertexConsumerProvider.Immediate instance, Operation<Void> original) {
		WorldRenderEvents.BEFORE_ENTITY_RENDER.invoker().beforeEntityRender(context);
		original.call(instance);
		WorldRenderEvents.AFTER_ENTITY_RENDER.invoker().afterEntityRender(context);
	}

	@WrapOperation(method = "method_62214", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/debug/DebugRenderer;render(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/Frustum;Lnet/minecraft/client/render/VertexConsumerProvider$Immediate;DDDZ)V"))
	private void onDebugRender(DebugRenderer instance, MatrixStack matrices, Frustum frustum, VertexConsumerProvider.Immediate consumers, double cameraX, double cameraY, double cameraZ, boolean lateDebug, Operation<Void> original) {
		original.call(instance, matrices, frustum, consumers, cameraX, cameraY, cameraZ, lateDebug);
		WorldRenderEvents.AFTER_DEBUG_RENDER.invoker().afterDebugRender(context);
	}

	@WrapOperation(method = "method_62214",
			slice = @Slice(from = @At(value = "INVOKE_STRING", target = "Lnet/minecraft/util/profiler/Profiler;push(Ljava/lang/String;)V", args = "ldc=translucent")),
			at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/SectionRenderState;renderSection(Lnet/minecraft/client/render/BlockRenderLayerGroup;)V")
	)
	private void onTranslucentRender(SectionRenderState instance, BlockRenderLayerGroup group, Operation<Void> original) {
		original.call(instance, group);
		WorldRenderEvents.AFTER_TRANSLUCENT_RENDER.invoker().afterTranslucentRender(context);
	}

	@Inject(method = "renderTargetBlockOutline", at = @At(value = "FIELD", target = "Lnet/minecraft/client/render/state/CameraRenderState;pos:Lnet/minecraft/util/math/Vec3d;"), cancellable = true)
	private void onDrawBlockOutline(VertexConsumerProvider.Immediate consumers, MatrixStack matrices, boolean bl, WorldRenderState worldRenderState, CallbackInfo ci) {
		if (!WorldRenderEvents.BEFORE_BLOCK_OUTLINE_RENDER.invoker().beforeBlockOutlineRender(context)) {
			consumers.drawCurrentLayer();
			ci.cancel();
		}
	}

	@Inject(method = "method_62214", at = @At("RETURN"))
	private void afterRender(CallbackInfo ci) {
		WorldRenderEvents.END_RENDER.invoker().endRender(context);
		isRendering = false;
	}

	@Inject(method = "reload()V", at = @At("HEAD"))
	private void onReload(CallbackInfo ci) {
		InvalidateRenderStateCallback.EVENT.invoker().onInvalidate();
	}

	@Override
	public WorldRenderContextImpl fabric$getWorldRenderContext() {
		if (!isRendering) {
			throw new IllegalStateException("WorldRenderer is not rendering");
		}

		return context;
	}
}
