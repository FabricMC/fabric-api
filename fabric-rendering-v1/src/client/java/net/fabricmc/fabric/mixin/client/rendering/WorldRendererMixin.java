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
import com.llamalad7.mixinextras.sugar.Local;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.block.BlockState;
import net.minecraft.client.render.BlockRenderLayerGroup;
import net.minecraft.client.render.Frustum;
import net.minecraft.client.render.SectionRenderState;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.WorldBorderRendering;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.render.command.OrderedRenderCommandQueueImpl;
import net.minecraft.client.render.debug.DebugRenderer;
import net.minecraft.client.render.state.OutlineRenderState;
import net.minecraft.client.render.state.WorldBorderRenderState;
import net.minecraft.client.render.state.WorldRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.border.WorldBorder;

import net.fabricmc.fabric.api.client.rendering.v1.InvalidateRenderStateCallback;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;

@Mixin(WorldRenderer.class)
public abstract class WorldRendererMixin {
	@Shadow
	@Final
	private WorldRenderState worldRenderState;
	@Shadow
	@Nullable
	private ClientWorld world;
	@Shadow
	@Final
	private OrderedRenderCommandQueueImpl entityRenderCommandQueue;

	@WrapOperation(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/state/WorldRenderState;clear()V"))
	private void onClearWorldRenderState(WorldRenderState instance, Operation<Void> original) {
		original.call(instance);
		WorldRenderEvents.START_EXTRACTION.invoker().startExtraction(worldRenderState, world);
	}

	@WrapOperation(method = "fillEntityOutlineRenderStates",
			slice = @Slice(from = @At(value = "NEW", target = "(Lnet/minecraft/util/math/BlockPos;ZZLnet/minecraft/util/shape/VoxelShape;Lnet/minecraft/util/shape/VoxelShape;Lnet/minecraft/util/shape/VoxelShape;Lnet/minecraft/util/shape/VoxelShape;)Lnet/minecraft/client/render/state/OutlineRenderState;")),
			at = @At(value = "FIELD", target = "Lnet/minecraft/client/render/state/WorldRenderState;outlineRenderState:Lnet/minecraft/client/render/state/OutlineRenderState;")
	)
	private void onBlockOutlineExtraction(WorldRenderState worldRenderState, OutlineRenderState outlineRenderState, Operation<Void> operation, @Local BlockState blockState) {
		operation.call(worldRenderState, outlineRenderState);
		outlineRenderState.setData(WorldRenderEvents.BLOCK_OUTLINE_BLOCK_STATE, blockState);
	}

	@WrapOperation(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/WorldBorderRendering;updateRenderState(Lnet/minecraft/world/border/WorldBorder;Lnet/minecraft/util/math/Vec3d;DLnet/minecraft/client/render/state/WorldBorderRenderState;)V"))
	private void onWorldBorderExtraction(WorldBorderRendering instance, WorldBorder worldBorder, Vec3d vec3d, double d, WorldBorderRenderState worldBorderRenderState, Operation<Void> original) {
		original.call(instance, worldBorder, vec3d, d, worldBorderRenderState);
		WorldRenderEvents.END_EXTRACTION.invoker().endExtraction(worldRenderState, world);
	}

	@Inject(method = "method_62214", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/WorldRenderer;pushEntityRenders(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/state/WorldRenderState;Lnet/minecraft/client/render/command/OrderedRenderCommandQueue;)V"))
	private void onPushEntityRenders(CallbackInfo ci, @Local MatrixStack matrices, @Local(ordinal = 0) VertexConsumerProvider.Immediate consumers) {
		WorldRenderEvents.BEFORE_SUBMIT_ENTITY_COMMANDS.invoker().beforeSubmitEntityCommands(worldRenderState, matrices, entityRenderCommandQueue, consumers);
	}

	@WrapOperation(method = "method_62214", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/WorldRenderer;renderBlockEntities(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/state/WorldRenderState;Lnet/minecraft/client/render/command/OrderedRenderCommandQueueImpl;)V"))
	private void onPushBlockEntityRenders(WorldRenderer instance, MatrixStack matrices, WorldRenderState worldRenderState, OrderedRenderCommandQueueImpl commandQueue, Operation<Void> original, @Local(ordinal = 0) VertexConsumerProvider.Immediate consumers) {
		original.call(instance, matrices, worldRenderState, commandQueue);
		WorldRenderEvents.AFTER_SUBMIT_ENTITY_COMMANDS.invoker().afterSubmitEntityCommands(worldRenderState, matrices, commandQueue, consumers);
	}

	@WrapOperation(method = "method_62214",
			slice = @Slice(from = @At(value = "INVOKE", target = "renderBlockLayers")),
			at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/SectionRenderState;renderSection(Lnet/minecraft/client/render/BlockRenderLayerGroup;)V", ordinal = 0)
	)
	private void onTerrainRender(SectionRenderState instance, BlockRenderLayerGroup group, Operation<Void> original) {
		WorldRenderEvents.START_RENDER.invoker().startRender(worldRenderState, instance);
		original.call(instance, group);
		WorldRenderEvents.AFTER_TERRAIN_RENDER.invoker().afterTerrainRender(worldRenderState, instance);
	}

	@WrapOperation(method = "method_62214",
			slice = @Slice(from = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/command/RenderDispatcher;render()V")),
			at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/VertexConsumerProvider$Immediate;drawCurrentLayer()V")
	)
	private void onEntityRender(VertexConsumerProvider.Immediate instance, Operation<Void> original, @Local SectionRenderState sectionRenderState, @Local MatrixStack matrices, @Local(ordinal = 0) VertexConsumerProvider.Immediate consumers) {
		WorldRenderEvents.BEFORE_ENTITY_RENDER.invoker().beforeEntityRender(worldRenderState, sectionRenderState, matrices, consumers);
		original.call(instance);
		WorldRenderEvents.AFTER_ENTITY_RENDER.invoker().afterEntityRender(worldRenderState, sectionRenderState, matrices, consumers);
	}

	@WrapOperation(method = "method_62214", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/debug/DebugRenderer;render(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/Frustum;Lnet/minecraft/client/render/VertexConsumerProvider$Immediate;DDDZ)V"))
	private void onDebugRender(DebugRenderer instance, MatrixStack matrices, Frustum frustum, VertexConsumerProvider.Immediate consumers, double cameraX, double cameraY, double cameraZ, boolean lateDebug, Operation<Void> original, @Local SectionRenderState sectionRenderState) {
		original.call(instance, matrices, frustum, consumers, cameraX, cameraY, cameraZ, lateDebug);
		WorldRenderEvents.AFTER_DEBUG_RENDER.invoker().afterDebugRender(worldRenderState, sectionRenderState, matrices, consumers);
	}

	@WrapOperation(method = "method_62214",
			slice = @Slice(from = @At(value = "INVOKE_STRING", target = "Lnet/minecraft/util/profiler/Profiler;push(Ljava/lang/String;)V", args = "ldc=translucent")),
			at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/SectionRenderState;renderSection(Lnet/minecraft/client/render/BlockRenderLayerGroup;)V")
	)
	private void onTranslucentRender(SectionRenderState instance, BlockRenderLayerGroup group, Operation<Void> original, @Local MatrixStack matrices, @Local(ordinal = 0) VertexConsumerProvider.Immediate consumers) {
		original.call(instance, group);
		WorldRenderEvents.AFTER_TRANSLUCENT_RENDER.invoker().afterTranslucentRender(worldRenderState, instance, matrices, consumers);
	}

	@Inject(method = "renderTargetBlockOutline", at = @At(value = "FIELD", target = "Lnet/minecraft/client/render/state/CameraRenderState;pos:Lnet/minecraft/util/math/Vec3d;"), cancellable = true)
	private void onDrawBlockOutline(VertexConsumerProvider.Immediate consumers, MatrixStack matrices, boolean bl, WorldRenderState worldRenderState, CallbackInfo ci) {
		if (!WorldRenderEvents.BEFORE_BLOCK_OUTLINE_RENDER.invoker().beforeBlockOutlineRender(worldRenderState, matrices, consumers)) {
			consumers.drawCurrentLayer();
			ci.cancel();
		}
	}

	@Inject(method = "method_62214", at = @At("RETURN"))
	private void afterRender(CallbackInfo ci, @Local SectionRenderState sectionRenderState, @Local MatrixStack matrices, @Local(ordinal = 0) VertexConsumerProvider.Immediate consumers) {
		WorldRenderEvents.END_RENDER.invoker().endRender(worldRenderState, sectionRenderState, matrices, consumers);
	}

	@Inject(method = "reload()V", at = @At("HEAD"))
	private void onReload(CallbackInfo ci) {
		InvalidateRenderStateCallback.EVENT.invoker().onInvalidate();
	}
}
