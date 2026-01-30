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

import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.VertexFormat;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.render.GuiRenderer;
import net.minecraft.client.gui.render.pip.PictureInPictureRenderer;
import net.minecraft.client.gui.render.state.GuiRenderState;
import net.minecraft.client.gui.render.state.pip.PictureInPictureRenderState;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.SubmitNodeStorage;
import net.minecraft.client.renderer.feature.FeatureRenderDispatcher;

import net.fabricmc.fabric.impl.client.rendering.GuiRendererExtensions;
import net.fabricmc.fabric.impl.client.rendering.SpecialGuiElementRegistryImpl;
import net.fabricmc.fabric.impl.client.rendering.SpecialGuiElementRendererPool;

@Mixin(GuiRenderer.class)
abstract class GuiRendererMixin implements GuiRendererExtensions {
	@Shadow
	@Final
	@Mutable
	private Map<Class<? extends PictureInPictureRenderState>, PictureInPictureRenderer<?>> pictureInPictureRenderers;
	@Shadow
	@Final
	private MultiBufferSource.BufferSource bufferSource;

	@Unique
	private boolean hasFabricInitialized = false;
	@Unique
	private final Map<Class<? extends PictureInPictureRenderState>, SpecialGuiElementRendererPool<?>> rendererPools = new HashMap<>();
	@Unique
	private SubmitNodeCollector orderedRenderCommandQueue = null;

	@Inject(method = "<init>", at = @At(value = "RETURN"))
	private void mutableSpecialElementRenderers(GuiRenderState state, MultiBufferSource.BufferSource vertexConsumers, SubmitNodeCollector orderedRenderCommandQueue, FeatureRenderDispatcher renderDispatcher, List list, CallbackInfo ci) {
		this.pictureInPictureRenderers = new IdentityHashMap<>(this.pictureInPictureRenderers);
	}

	@Override
	public void fabric_onReady(SubmitNodeStorage entityRenderDispatcher) {
		this.orderedRenderCommandQueue = entityRenderDispatcher;
		SpecialGuiElementRegistryImpl.onReady(Minecraft.getInstance(), bufferSource, entityRenderDispatcher, this.pictureInPictureRenderers);
		this.hasFabricInitialized = true;
	}

	@Inject(method = "preparePictureInPicture", at = @At("HEAD"))
	private void prePrepareSpecialElements(CallbackInfo ci) {
		rendererPools.values().forEach(SpecialGuiElementRendererPool::newFrame);
	}

	@Inject(method = "preparePictureInPicture", at = @At("RETURN"))
	private void postPrepareSpecialElements(CallbackInfo ci) {
		rendererPools.values().forEach(SpecialGuiElementRendererPool::cleanUpUnusedRenderers);
	}

	@ModifyVariable(method = "preparePictureInPictureState", at = @At("STORE"))
	private <T extends PictureInPictureRenderState> PictureInPictureRenderer<T> substituteSpecialElementRenderer(PictureInPictureRenderer<T> original, T elementState) {
		if (original == null || !hasFabricInitialized) {
			return original;
		}

		SpecialGuiElementRendererPool<T> rendererPool = (SpecialGuiElementRendererPool<T>) rendererPools.computeIfAbsent(original.getRenderStateClass(), k -> new SpecialGuiElementRendererPool<>());
		return rendererPool.substitute(original, elementState, Minecraft.getInstance(), bufferSource, Objects.requireNonNull(orderedRenderCommandQueue, "renderDispatcher"));
	}

	@Inject(method = "close", at = @At("RETURN"))
	private void closeRendererPools(CallbackInfo ci) {
		rendererPools.values().forEach(SpecialGuiElementRendererPool::close);
	}

	@WrapOperation(
			method = "executeDraw(Lnet/minecraft/client/gui/render/GuiRenderer$Draw;Lcom/mojang/blaze3d/systems/RenderPass;Lcom/mojang/blaze3d/buffers/GpuBuffer;Lcom/mojang/blaze3d/vertex/VertexFormat$IndexType;)V",
			at = @At(
					value = "INVOKE",
					target = "Lcom/mojang/blaze3d/systems/RenderPass;setIndexBuffer(Lcom/mojang/blaze3d/buffers/GpuBuffer;Lcom/mojang/blaze3d/vertex/VertexFormat$IndexType;)V"
			)
	)
	private void fixNonQuadIndexing(RenderPass instance, GpuBuffer buffer, VertexFormat.IndexType indexType, Operation<Void> original, @Coerce DrawAccessor draw) {
		RenderPipeline pipeline = draw.fabric$pipeline();

		if (pipeline.usePipelineDrawModeForGui() && pipeline.getVertexFormatMode() != VertexFormat.Mode.QUADS) {
			RenderSystem.AutoStorageIndexBuffer shapeIndexBuffer = RenderSystem.getSequentialBuffer(pipeline.getVertexFormatMode());
			buffer = shapeIndexBuffer.getBuffer(draw.fabric$indexCount());
			indexType = shapeIndexBuffer.type();
		}

		original.call(instance, buffer, indexType);
	}

	@ModifyExpressionValue(method = "addElementToMesh(Lnet/minecraft/client/gui/render/state/GuiElementRenderState;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/render/GuiRenderer;scissorChanged(Lnet/minecraft/client/gui/navigation/ScreenRectangle;Lnet/minecraft/client/gui/navigation/ScreenRectangle;)Z"))
	private boolean uploadPrimitivesIndividually(boolean original, @Local RenderPipeline pipeline) {
		return original || pipeline.getVertexFormatMode().connectedPrimitives;
	}
}
