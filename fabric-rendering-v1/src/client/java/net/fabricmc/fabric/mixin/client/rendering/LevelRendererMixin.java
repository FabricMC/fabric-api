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
import com.mojang.blaze3d.textures.GpuSampler;
import com.mojang.blaze3d.vertex.PoseStack;
import org.joml.Matrix4fc;
import org.joml.Vector4f;
import org.jspecify.annotations.Nullable;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderBuffers;
import net.minecraft.client.renderer.SubmitNodeStorage;
import net.minecraft.client.renderer.chunk.ChunkSectionLayerGroup;
import net.minecraft.client.renderer.chunk.ChunkSectionsToRender;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.state.level.LevelRenderState;

import net.fabricmc.fabric.api.client.rendering.v1.InvalidateRenderStateCallback;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents;
import net.fabricmc.fabric.impl.client.rendering.LevelRendererExtensions;
import net.fabricmc.fabric.impl.client.rendering.level.LevelExtractionContextImpl;
import net.fabricmc.fabric.impl.client.rendering.level.LevelRenderContextImpl;

@Mixin(LevelRenderer.class)
public abstract class LevelRendererMixin implements LevelRendererExtensions {
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

	@Override
	public void fabric_prepareLevelExtractionContext(DeltaTracker deltaTracker) {
		extractionContext.prepare(
				minecraft.gameRenderer,
				minecraft.levelRenderer,
				levelRenderState,
				level,
				deltaTracker,
				minecraft.gameRenderer.getMainCamera());
	}

	@Inject(method = "renderLevel", at = @At("HEAD"))
	private void beforeRender(GraphicsResourceAllocator resourceAllocator, DeltaTracker deltaTracker, boolean renderOutline, CameraRenderState cameraState, Matrix4fc modelViewMatrix, GpuBufferSlice terrainFog, Vector4f fogColor, boolean shouldRenderSky, ChunkSectionsToRender chunkSectionsToRender, CallbackInfo ci) {
		renderContext.prepare(minecraft.gameRenderer, (LevelRenderer) (Object) this, levelRenderState, chunkSectionsToRender, submitNodeStorage, renderBuffers.bufferSource());
	}

	@Inject(method = "extractBlockOutline", at = @At("RETURN"))
	private void afterBlockOutlineExtraction(Camera camera, LevelRenderState renderStates, CallbackInfo ci) {
		LevelRenderEvents.AFTER_BLOCK_OUTLINE_EXTRACTION.invoker().afterBlockOutlineExtraction(extractionContext, minecraft.hitResult);
	}

	@Inject(method = "extractLevel", at = @At("RETURN"))
	private void afterExtractLevel(DeltaTracker deltaTracker, Camera camera, float deltaPartialTick, CallbackInfo ci) {
		LevelRenderEvents.END_EXTRACTION.invoker().endExtraction(extractionContext);
	}

	@WrapOperation(method = "lambda$addMainPass$0", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/chunk/ChunkSectionsToRender;renderGroup(Lnet/minecraft/client/renderer/chunk/ChunkSectionLayerGroup;Lcom/mojang/blaze3d/textures/GpuSampler;)V", ordinal = 0))
	private void wrapRenderOpaqueTerrain(ChunkSectionsToRender chunkSectionsToRender, ChunkSectionLayerGroup group, GpuSampler sampler, Operation<Void> original) {
		LevelRenderEvents.START_MAIN.invoker().startMain(renderContext);
		original.call(chunkSectionsToRender, group, sampler);
		LevelRenderEvents.AFTER_OPAQUE_TERRAIN.invoker().afterOpaqueTerrain(renderContext);
	}

	@ModifyExpressionValue(method = "lambda$addMainPass$0", at = @At(value = "NEW", target = "Lcom/mojang/blaze3d/vertex/PoseStack;"))
	private PoseStack onCreatePoseStack(PoseStack poseStack) {
		renderContext.setPoseStack(poseStack);
		return poseStack;
	}

	@Inject(method = "lambda$addMainPass$0", at = @At(value = "INVOKE_STRING", target = "Lnet/minecraft/util/profiling/ProfilerFiller;popPush(Ljava/lang/String;)V", args = "ldc=renderSolidFeatures"))
	private void afterCollectSubmits(CallbackInfo ci) {
		LevelRenderEvents.COLLECT_SUBMITS.invoker().collectSubmits(renderContext);
	}

	@Inject(method = "lambda$addMainPass$0", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/LevelRenderer;checkPoseStack(Lcom/mojang/blaze3d/vertex/PoseStack;)V", ordinal = 0))
	private void afterRenderSolidFeatures(CallbackInfo ci) {
		LevelRenderEvents.AFTER_SOLID_FEATURES.invoker().afterSolidFeatures(renderContext);
	}

	@Inject(method = "lambda$addMainPass$0", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/feature/FeatureRenderDispatcher;renderTranslucentFeatures()V", shift = At.Shift.AFTER))
	private void afterRenderTranslucentFeatures(CallbackInfo ci) {
		LevelRenderEvents.AFTER_TRANSLUCENT_FEATURES.invoker().afterTranslucentFeatures(renderContext);
	}

	@Inject(method = "renderBlockOutline", at = @At(value = "FIELD", target = "Lnet/minecraft/client/renderer/state/level/CameraRenderState;pos:Lnet/minecraft/world/phys/Vec3;", opcode = Opcodes.GETFIELD), cancellable = true)
	private void beforeRenderBlockOutline(MultiBufferSource.BufferSource bufferSource, PoseStack poseStack, boolean translucent, LevelRenderState levelRenderState, CallbackInfo ci) {
		if (!LevelRenderEvents.BEFORE_BLOCK_OUTLINE.invoker().beforeBlockOutline(renderContext, renderContext.levelState().blockOutlineRenderState)) {
			bufferSource.endLastBatch();
			ci.cancel();
		}
	}

	@Inject(method = "lambda$addMainPass$0", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/LevelRenderer;finalizeGizmoCollection()V"))
	private void beforeCollectGizmos(CallbackInfo ci) {
		LevelRenderEvents.BEFORE_GIZMOS.invoker().beforeGizmos(renderContext);
	}

	@WrapOperation(method = "lambda$addMainPass$0", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/chunk/ChunkSectionsToRender;renderGroup(Lnet/minecraft/client/renderer/chunk/ChunkSectionLayerGroup;Lcom/mojang/blaze3d/textures/GpuSampler;)V", ordinal = 1))
	private void wrapRenderTranslucentTerrain(ChunkSectionsToRender chunkSectionsToRender, ChunkSectionLayerGroup group, GpuSampler sampler, Operation<Void> original) {
		LevelRenderEvents.BEFORE_TRANSLUCENT_TERRAIN.invoker().beforeTranslucentTerrain(renderContext);
		original.call(chunkSectionsToRender, group, sampler);
		LevelRenderEvents.AFTER_TRANSLUCENT_TERRAIN.invoker().afterTranslucentTerrain(renderContext);
	}

	@Inject(method = "lambda$addMainPass$0", at = @At("RETURN"))
	private void endMainRender(CallbackInfo ci) {
		LevelRenderEvents.END_MAIN.invoker().endMain(renderContext);
	}

	@Inject(method = "allChanged()V", at = @At("HEAD"))
	private void onReload(CallbackInfo ci) {
		InvalidateRenderStateCallback.EVENT.invoker().onInvalidate();
	}
}
