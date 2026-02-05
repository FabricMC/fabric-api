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
import com.mojang.blaze3d.resource.GraphicsResourceAllocator;
import com.mojang.blaze3d.vertex.PoseStack;
import org.joml.Matrix4f;
import org.joml.Vector4f;
import org.jspecify.annotations.Nullable;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.OutlineBufferSource;
import net.minecraft.client.renderer.RenderBuffers;
import net.minecraft.client.renderer.SubmitNodeStorage;
import net.minecraft.client.renderer.WorldBorderRenderer;
import net.minecraft.client.renderer.chunk.ChunkSectionsToRender;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.state.LevelRenderState;
import net.minecraft.client.renderer.state.WorldBorderRenderState;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.phys.Vec3;

import net.fabricmc.fabric.api.client.rendering.v1.InvalidateRenderStateCallback;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents;
import net.fabricmc.fabric.impl.client.rendering.level.LevelExtractionContextImpl;
import net.fabricmc.fabric.impl.client.rendering.level.LevelRenderContextImpl;

@Mixin(LevelRenderer.class)
public abstract class LevelRendererMixin {
	@Shadow
	@Final
	private Minecraft minecraft;
	@Shadow
	@Final
	private RenderBuffers renderBuffers;
	@Shadow
	@Final
	private LevelRenderState levelRenderState;
	@Shadow
	@Nullable
	private ClientLevel level;
	@Shadow
	@Final
	private SubmitNodeStorage submitNodeStorage;

	@Unique
	private final LevelRenderContextImpl renderContext = new LevelRenderContextImpl();
	@Unique
	private final LevelExtractionContextImpl extractionContext = new LevelExtractionContextImpl();

	@Inject(method = "renderLevel", at = @At("HEAD"))
	private void beforeRender(GraphicsResourceAllocator allocator, DeltaTracker deltaTracker, boolean renderBlockOutline, Camera camera, Matrix4f viewMatrix, Matrix4f projectionMatrix, Matrix4f cullProjectionMatrix, GpuBufferSlice fogBuffer, Vector4f fogColor, boolean renderSky, CallbackInfo ci) {
		extractionContext.prepare(minecraft.gameRenderer, (LevelRenderer) (Object) this, levelRenderState, level,
				deltaTracker, renderBlockOutline, camera, viewMatrix, cullProjectionMatrix);
	}

	@ModifyExpressionValue(method = "renderLevel", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/LevelRenderer;prepareCullFrustum(Lorg/joml/Matrix4f;Lorg/joml/Matrix4f;Lnet/minecraft/world/phys/Vec3;)Lnet/minecraft/client/renderer/culling/Frustum;"))
	private Frustum onSetupFrustum(Frustum frustum) {
		extractionContext.setFrustum(frustum);
		return frustum;
	}

	@Inject(method = "extractBlockOutline", at = @At("RETURN"))
	private void afterBlockOutlineExtraction(Camera camera, LevelRenderState renderStates, CallbackInfo ci) {
		LevelRenderEvents.AFTER_BLOCK_OUTLINE_EXTRACTION.invoker().afterBlockOutlineExtraction(extractionContext, minecraft.hitResult);
	}

	@WrapOperation(method = "renderLevel", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/WorldBorderRenderer;extract(Lnet/minecraft/world/level/border/WorldBorder;FLnet/minecraft/world/phys/Vec3;DLnet/minecraft/client/renderer/state/WorldBorderRenderState;)V"))
	private void onWorldBorderExtraction(WorldBorderRenderer instance, WorldBorder worldBorder, float tickProgress, Vec3 vec3d, double viewDistanceBlocks, WorldBorderRenderState worldBorderRenderState, Operation<Void> original) {
		original.call(instance, worldBorder, tickProgress, vec3d, viewDistanceBlocks, worldBorderRenderState);
		LevelRenderEvents.END_EXTRACTION.invoker().endExtraction(extractionContext);
	}

	@ModifyExpressionValue(method = "lambda$addMainPass$0", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/LevelRenderer;prepareChunkRenders(Lorg/joml/Matrix4fc;DDD)Lnet/minecraft/client/renderer/chunk/ChunkSectionsToRender;"))
	private ChunkSectionsToRender onRenderBlockLayers(ChunkSectionsToRender chunkSectionsToRender) {
		renderContext.prepare(minecraft.gameRenderer, (LevelRenderer) (Object) this, levelRenderState, chunkSectionsToRender, submitNodeStorage, renderBuffers.bufferSource());
		return chunkSectionsToRender;
	}

	@Inject(method = "lambda$addMainPass$0",
			slice = @Slice(from = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/LevelRenderer;prepareChunkRenders(Lorg/joml/Matrix4fc;DDD)Lnet/minecraft/client/renderer/chunk/ChunkSectionsToRender;")),
			at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/chunk/ChunkSectionsToRender;renderGroup(Lnet/minecraft/client/renderer/chunk/ChunkSectionLayerGroup;Lcom/mojang/blaze3d/textures/GpuSampler;)V", ordinal = 0)
	)
	private void beforeTerrainRender(CallbackInfo ci) {
		LevelRenderEvents.START_MAIN.invoker().startMain(renderContext);
	}

	@ModifyExpressionValue(method = "lambda$addMainPass$0", at = @At(value = "NEW", target = "Lcom/mojang/blaze3d/vertex/PoseStack;"))
	private PoseStack onCreatePoseStack(PoseStack poseStack) {
		renderContext.setPoseStack(poseStack);
		return poseStack;
	}

	@Inject(method = "lambda$addMainPass$0", at = @At(value = "INVOKE_STRING", target = "Lnet/minecraft/util/profiling/ProfilerFiller;popPush(Ljava/lang/String;)V", args = "ldc=submitEntities"))
	private void beforeEntitySubmission(CallbackInfo ci) {
		LevelRenderEvents.BEFORE_ENTITIES.invoker().beforeEntities(renderContext);
	}

	@WrapOperation(method = "lambda$addMainPass$0",
			slice = @Slice(from = @At(value = "INVOKE_STRING", target = "Lnet/minecraft/util/profiling/ProfilerFiller;popPush(Ljava/lang/String;)V", args = "ldc=submitEntities")),
			at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/OutlineBufferSource;endOutlineBatch()V")
	)
	private void afterEntityRender(OutlineBufferSource instance, Operation<Void> original) {
		original.call(instance);
		LevelRenderEvents.AFTER_ENTITIES.invoker().afterEntities(renderContext);
	}

	@Inject(method = "renderLevel", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/debug/DebugRenderer;emitGizmos(Lnet/minecraft/client/renderer/culling/Frustum;DDDF)V"))
	private void beforeDebugRender(CallbackInfo ci) {
		LevelRenderEvents.BEFORE_DEBUG_RENDER.invoker().beforeDebugRender(renderContext);
	}

	@Inject(method = "lambda$addMainPass$0", at = @At(value = "INVOKE_STRING", target = "Lnet/minecraft/util/profiling/ProfilerFiller;push(Ljava/lang/String;)V", args = "ldc=translucentTerrain"))
	private void beforeTranslucentRender(CallbackInfo ci) {
		LevelRenderEvents.BEFORE_TRANSLUCENT.invoker().beforeTranslucent(renderContext);
	}

	@Inject(method = "renderBlockOutline", at = @At(value = "FIELD", target = "Lnet/minecraft/client/renderer/state/CameraRenderState;pos:Lnet/minecraft/world/phys/Vec3;", opcode = Opcodes.GETFIELD), cancellable = true)
	private void beforeDrawBlockOutline(MultiBufferSource.BufferSource consumers, PoseStack poseStack, boolean bl, LevelRenderState worldRenderState, CallbackInfo ci) {
		if (!LevelRenderEvents.BEFORE_BLOCK_OUTLINE.invoker().beforeBlockOutline(renderContext, renderContext.levelState().blockOutlineRenderState)) {
			consumers.endLastBatch();
			ci.cancel();
		}
	}

	@Inject(method = "lambda$addMainPass$0", at = @At(value = "INVOKE:LAST", target = "Lnet/minecraft/client/renderer/MultiBufferSource$BufferSource;endBatch()V"))
	private void endMainRender(CallbackInfo ci) {
		LevelRenderEvents.END_MAIN.invoker().endMain(renderContext);
	}

	@Inject(method = "allChanged()V", at = @At("HEAD"))
	private void onReload(CallbackInfo ci) {
		InvalidateRenderStateCallback.EVENT.invoker().onInvalidate();
	}
}
