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
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Vector4f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.render.Camera;
import net.minecraft.client.render.DefaultFramebufferSet;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.state.WorldRenderState;
import net.minecraft.client.util.ObjectAllocator;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;

import net.fabricmc.fabric.api.client.rendering.v1.InvalidateRenderStateCallback;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;

@Mixin(WorldRenderer.class)
public abstract class WorldRendererMixin {
	@Shadow
	@Final
	private DefaultFramebufferSet framebufferSet;
	@Shadow
	@Final
	private WorldRenderState worldRenderState;

	@Shadow
	@Nullable
	private ClientWorld world;

	@Inject(method = "render", at = @At("HEAD"))
	private void beforeRender(ObjectAllocator objectAllocator, RenderTickCounter tickCounter, boolean renderBlockOutline, Camera camera, Matrix4f positionMatrix, Matrix4f projectionMatrix, Matrix4f matrix4f2, GpuBufferSlice gpuBufferSlice, Vector4f vector4f, boolean bl, CallbackInfo ci) {
		WorldRenderEvents.START.invoker().onStart(worldRenderState);
	}

	@Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/WorldBorderRendering;updateRenderState(Lnet/minecraft/world/border/WorldBorder;Lnet/minecraft/util/math/Vec3d;DLnet/minecraft/client/render/state/WorldBorderRenderState;)V"))
	private void afterExtract(ObjectAllocator objectAllocator, RenderTickCounter tickCounter, boolean renderBlockOutline, Camera camera, Matrix4f positionMatrix, Matrix4f projectionMatrix, Matrix4f matrix4f2, GpuBufferSlice gpuBufferSlice, Vector4f vector4f, boolean bl, CallbackInfo ci) {
		WorldRenderEvents.UPDATE_STATE.invoker().afterStateUpdate(worldRenderState, this.world);
	}

	@WrapOperation(
			method = "method_62214",
			at = @At(
				value = "INVOKE",
				target = "Lnet/minecraft/client/render/WorldRenderer;pushEntityRenders(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/state/WorldRenderState;Lnet/minecraft/client/render/command/OrderedRenderCommandQueue;)V"
			)
	)
	private void pushEntityRenders(WorldRenderer instance, MatrixStack matrices, WorldRenderState renderStates, OrderedRenderCommandQueue queue, Operation<Void> original) {
		WorldRenderEvents.BEFORE_ENTITIES.invoker().beforeEntities(worldRenderState, matrices, queue);
		original.call(instance, matrices, renderStates, queue);
		WorldRenderEvents.AFTER_ENTITIES.invoker().afterEntities(worldRenderState, matrices, queue);
	}

	@Inject(
			method = "method_62214",
			at = @At(
				value = "INVOKE",
				target = "Lnet/minecraft/client/render/debug/DebugRenderer;render(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/Frustum;Lnet/minecraft/client/render/VertexConsumerProvider$Immediate;DDDZ)V",
				ordinal = 0
			)
	)
	private void beforeDebugRender(CallbackInfo ci) {
		WorldRenderEvents.BEFORE_DEBUG_RENDER.invoker().beforeDebugRender(worldRenderState);
	}

	@Inject(
			method = "method_62214",
			slice = @Slice(from = @At(value = "INVOKE_STRING", args = "ldc=translucent", target = "Lnet/minecraft/util/profiler/Profiler;push(Ljava/lang/String;)V")),
			at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/SectionRenderState;renderSection(Lnet/minecraft/client/render/BlockRenderLayerGroup;)V", ordinal = 0)
	)
	private void beforeClouds(CallbackInfo ci) {
		WorldRenderEvents.AFTER_TRANSLUCENT.invoker().afterTranslucent(worldRenderState);
	}

	@Inject(method = "method_62214", at = @At("RETURN"))
	private void afterRender(CallbackInfo ci) {
		WorldRenderEvents.LAST.invoker().onLast(worldRenderState);
	}

	@Inject(method = "reload()V", at = @At("HEAD"))
	private void onReload(CallbackInfo ci) {
		InvalidateRenderStateCallback.EVENT.invoker().onInvalidate();
	}
}
