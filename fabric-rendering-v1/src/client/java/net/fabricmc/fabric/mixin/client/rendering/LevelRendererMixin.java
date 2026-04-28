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
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.RenderBuffers;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.SubmitNodeStorage;
import net.minecraft.client.renderer.chunk.ChunkSectionLayerGroup;
import net.minecraft.client.renderer.chunk.ChunkSectionsToRender;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.state.level.LevelRenderState;

import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents;
import net.fabricmc.fabric.impl.client.rendering.level.LevelRenderContextImpl;

@Mixin(LevelRenderer.class)
public abstract class LevelRendererMixin {
	@Shadow
	@Final
	private RenderBuffers renderBuffers;
	@Shadow
	@Final
	private LevelRenderState levelRenderState;
	@Shadow
	@Final
	private SubmitNodeStorage submitNodeStorage;

	@Unique
	private final LevelRenderContextImpl renderContext = new LevelRenderContextImpl();

	@Inject(method = "render", at = @At("HEAD"))
	private void beforeRender(GraphicsResourceAllocator resourceAllocator, DeltaTracker deltaTracker, boolean renderOutline, CameraRenderState cameraState, Matrix4fc modelViewMatrix, GpuBufferSlice terrainFog, Vector4f fogColor, boolean shouldRenderSky, CallbackInfo ci) {
		renderContext.prepare(Minecraft.getInstance().gameRenderer, (LevelRenderer) (Object) this, levelRenderState, submitNodeStorage);
	}

	@Inject(method = "prepareChunkRenders", at = @At("RETURN"))
	private void prepareChunkRenders(CallbackInfoReturnable<ChunkSectionsToRender> cir) {
		renderContext.setSectionsToRender(cir.getReturnValue());
	}

	@WrapOperation(method = "lambda$addMainPass$0", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/chunk/ChunkSectionsToRender;renderGroup(Lnet/minecraft/client/renderer/chunk/ChunkSectionLayerGroup;Lcom/mojang/blaze3d/textures/GpuSampler;)V", ordinal = 0))
	private void wrapRenderOpaqueTerrain(ChunkSectionsToRender chunkSectionsToRender, ChunkSectionLayerGroup group, GpuSampler sampler, Operation<Void> original) {
		LevelRenderEvents.START_MAIN.invoker().startMain(renderContext);
		original.call(chunkSectionsToRender, group, sampler);
		LevelRenderEvents.AFTER_OPAQUE_TERRAIN.invoker().afterOpaqueTerrain(renderContext);
	}

	@ModifyExpressionValue(method = "submitFeatures", at = @At(value = "NEW", target = "Lcom/mojang/blaze3d/vertex/PoseStack;"))
	private PoseStack onCreatePoseStack(PoseStack poseStack) {
		renderContext.setPoseStack(poseStack);
		return poseStack;
	}

	@Inject(method = "submitFeatures", at = @At("RETURN"))
	private void afterCollectSubmits(CallbackInfo ci) {
		LevelRenderEvents.COLLECT_SUBMITS.invoker().collectSubmits(renderContext);
	}

	@Inject(method = "lambda$addMainPass$0", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/profiling/ProfilerFiller;pop()V", ordinal = 0))
	private void afterRenderSolidFeatures(CallbackInfo ci) {
		LevelRenderEvents.AFTER_SOLID_FEATURES.invoker().afterSolidFeatures(renderContext);
	}

	@Inject(method = "lambda$addMainPass$0", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/feature/FeatureRenderDispatcher$PreparedFrame;executeTranslucent()V", shift = At.Shift.AFTER))
	private void afterRenderTranslucentFeatures(CallbackInfo ci) {
		LevelRenderEvents.AFTER_TRANSLUCENT_FEATURES.invoker().afterTranslucentFeatures(renderContext);
	}

	@Inject(method = "submitBlockOutline", at = @At(value = "FIELD", target = "Lnet/minecraft/client/renderer/state/level/CameraRenderState;pos:Lnet/minecraft/world/phys/Vec3;", opcode = Opcodes.GETFIELD), cancellable = true)
	private void beforeRenderBlockOutline(PoseStack poseStack, SubmitNodeCollector submitNodeCollector, LevelRenderState levelRenderState, CallbackInfo ci) {
		if (!LevelRenderEvents.BEFORE_BLOCK_OUTLINE.invoker().beforeBlockOutline(renderContext, renderContext.levelState().blockOutlineRenderState)) {
			ci.cancel();
		}
	}

	@Inject(method = "submitFeatures", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/LevelRenderer;finalizeGizmoCollection()V"))
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
}
