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
import net.minecraft.client.render.state.WorldBorderRenderState;
import net.minecraft.client.render.state.WorldRenderState;
import net.minecraft.client.util.ObjectAllocator;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.border.WorldBorder;

import net.fabricmc.fabric.api.client.rendering.v1.InvalidateRenderStateCallback;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderEvents;
import net.fabricmc.fabric.impl.client.rendering.world.WorldExtractionContextImpl;
import net.fabricmc.fabric.impl.client.rendering.world.WorldRenderContextImpl;

@Mixin(WorldRenderer.class)
public abstract class WorldRendererMixin {
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
	private final WorldRenderContextImpl renderContext = new WorldRenderContextImpl();
	@Unique
	private final WorldExtractionContextImpl extractionContext = new WorldExtractionContextImpl();

	@Inject(method = "render", at = @At("HEAD"))
	private void beforeRender(ObjectAllocator allocator, RenderTickCounter tickCounter, boolean renderBlockOutline, Camera camera, Matrix4f positionMatrix, Matrix4f matrix4f, Matrix4f projectionMatrix, GpuBufferSlice fogBuffer, Vector4f fogColor, boolean renderSky, CallbackInfo ci) {
		extractionContext.prepare(client.gameRenderer, (WorldRenderer) (Object) this, worldRenderState, world, tickCounter, renderBlockOutline, camera, positionMatrix, projectionMatrix);
	}

	@ModifyExpressionValue(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/WorldRenderer;setupFrustum(Lorg/joml/Matrix4f;Lorg/joml/Matrix4f;Lnet/minecraft/util/math/Vec3d;)Lnet/minecraft/client/render/Frustum;"))
	private Frustum onSetupFrustum(Frustum frustum) {
		extractionContext.setFrustum(frustum);
		return frustum;
	}

	@Inject(method = "fillEntityOutlineRenderStates", at = @At("RETURN"))
	private void afterBlockOutlineExtraction(Camera camera, WorldRenderState renderStates, CallbackInfo ci) {
		WorldRenderEvents.AFTER_BLOCK_OUTLINE_EXTRACTION.invoker().afterBlockOutlineExtraction(extractionContext, client.crosshairTarget);
	}

	@WrapOperation(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/WorldBorderRendering;updateRenderState(Lnet/minecraft/world/border/WorldBorder;Lnet/minecraft/util/math/Vec3d;DLnet/minecraft/client/render/state/WorldBorderRenderState;)V"))
	private void onWorldBorderExtraction(WorldBorderRendering instance, WorldBorder worldBorder, Vec3d vec3d, double d, WorldBorderRenderState worldBorderRenderState, Operation<Void> original) {
		original.call(instance, worldBorder, vec3d, d, worldBorderRenderState);
		WorldRenderEvents.END_EXTRACTION.invoker().endExtraction(extractionContext);
	}

	@ModifyExpressionValue(method = "method_62214", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/WorldRenderer;renderBlockLayers(Lorg/joml/Matrix4fc;DDD)Lnet/minecraft/client/render/SectionRenderState;"))
	private SectionRenderState onRenderBlockLayers(SectionRenderState sectionRenderState) {
		renderContext.prepare(client.gameRenderer, (WorldRenderer) (Object) this, worldRenderState, sectionRenderState, entityRenderCommandQueue, bufferBuilders.getEntityVertexConsumers());
		return sectionRenderState;
	}

	@Inject(method = "method_62214",
			slice = @Slice(from = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/WorldRenderer;renderBlockLayers(Lorg/joml/Matrix4fc;DDD)Lnet/minecraft/client/render/SectionRenderState;")),
			at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/SectionRenderState;renderSection(Lnet/minecraft/client/render/BlockRenderLayerGroup;)V", ordinal = 0)
	)
	private void beforeTerrainRender(CallbackInfo ci) {
		WorldRenderEvents.START_RENDER.invoker().startRender(renderContext);
	}

	@ModifyExpressionValue(method = "method_62214", at = @At(value = "NEW", target = "Lnet/minecraft/client/util/math/MatrixStack;"))
	private MatrixStack onCreateMatrixStack(MatrixStack matrixStack) {
		renderContext.setMatrixStack(matrixStack);
		return matrixStack;
	}

	@WrapOperation(method = "method_62214",
			slice = @Slice(from = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/command/RenderDispatcher;render()V")),
			at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/VertexConsumerProvider$Immediate;drawCurrentLayer()V", ordinal = 0)
	)
	private void onEntityRender(VertexConsumerProvider.Immediate instance, Operation<Void> original) {
		WorldRenderEvents.BEFORE_ENTITIES.invoker().beforeEntities(renderContext);
		original.call(instance);
		WorldRenderEvents.AFTER_ENTITIES.invoker().afterEntities(renderContext);
	}

	@Inject(method = "method_62214", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/debug/DebugRenderer;render(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/Frustum;Lnet/minecraft/client/render/VertexConsumerProvider$Immediate;DDDZ)V"))
	private void beforeDebugRender(CallbackInfo ci) {
		WorldRenderEvents.BEFORE_DEBUG_RENDER.invoker().beforeDebugRender(renderContext);
	}

	@WrapOperation(method = "method_62214",
			slice = @Slice(from = @At(value = "INVOKE_STRING", target = "Lnet/minecraft/util/profiler/Profiler;push(Ljava/lang/String;)V", args = "ldc=translucent")),
			at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/SectionRenderState;renderSection(Lnet/minecraft/client/render/BlockRenderLayerGroup;)V")
	)
	private void onTranslucentRender(SectionRenderState instance, BlockRenderLayerGroup group, Operation<Void> original) {
		original.call(instance, group);
		WorldRenderEvents.AFTER_TRANSLUCENT.invoker().afterTranslucent(renderContext);
	}

	@Inject(method = "renderTargetBlockOutline", at = @At(value = "FIELD", target = "Lnet/minecraft/client/render/state/CameraRenderState;pos:Lnet/minecraft/util/math/Vec3d;"), cancellable = true)
	private void onDrawBlockOutline(VertexConsumerProvider.Immediate consumers, MatrixStack matrices, boolean bl, WorldRenderState worldRenderState, CallbackInfo ci) {
		if (!WorldRenderEvents.BLOCK_OUTLINE.invoker().onBlockOutline(renderContext, renderContext.worldRenderState().outlineRenderState)) {
			consumers.drawCurrentLayer();
			ci.cancel();
		}
	}

	@Inject(method = "method_62214", at = @At(value = "INVOKE:LAST", target = "Lnet/minecraft/client/render/VertexConsumerProvider$Immediate;draw()V"))
	private void afterRender(CallbackInfo ci) {
		WorldRenderEvents.LAST.invoker().onLast(renderContext);
	}

	@Inject(method = "reload()V", at = @At("HEAD"))
	private void onReload(CallbackInfo ci) {
		InvalidateRenderStateCallback.EVENT.invoker().onInvalidate();
	}
}
