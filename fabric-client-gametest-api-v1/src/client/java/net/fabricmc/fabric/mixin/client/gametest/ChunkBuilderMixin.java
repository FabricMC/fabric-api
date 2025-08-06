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

package net.fabricmc.fabric.mixin.client.gametest;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.render.chunk.BlockBufferAllocatorStorage;
import net.minecraft.client.render.chunk.ChunkBuilder;
import net.minecraft.client.render.chunk.ChunkRenderTaskScheduler;
import net.minecraft.util.math.Vec3d;

@Mixin(ChunkBuilder.class)
public class ChunkBuilderMixin {
	@Unique
	private static final Logger LOGGER = LoggerFactory.getLogger(ChunkBuilderMixin.class);

	@Inject(method = "method_23086", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/chunk/ChunkRenderTaskScheduler;enqueue(Lnet/minecraft/client/render/chunk/ChunkBuilder$BuiltChunk$Task;)V"))
	private void logEnqueue(ChunkBuilder.BuiltChunk.Task task, CallbackInfo ci) {
		LOGGER.info("Enqueue task {} - {}, origin {}", task.getClass(), task.hashCode(), task.getOrigin());
	}

	@WrapOperation(method = "scheduleRunTasks", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/chunk/ChunkRenderTaskScheduler;dequeueNearest(Lnet/minecraft/util/math/Vec3d;)Lnet/minecraft/client/render/chunk/ChunkBuilder$BuiltChunk$Task;"))
	private ChunkBuilder.BuiltChunk.Task logDequeue(ChunkRenderTaskScheduler instance, Vec3d pos, Operation<ChunkBuilder.BuiltChunk.Task> original) {
		ChunkBuilder.BuiltChunk.Task task = original.call(instance, pos);

		if (task != null) {
			LOGGER.info("Dequeue task {} - {}, origin {}", task.getClass(), task.hashCode(), task.getOrigin());
		}

		return task;
	}

	@Inject(method = "method_22755", at = @At(value = "INVOKE", target = "Ljava/util/concurrent/atomic/AtomicBoolean;set(Z)V"))
	private void logFinished(ChunkBuilder.BuiltChunk.Task task, BlockBufferAllocatorStorage blockBufferAllocatorStorage, @Coerce Object result, Throwable throwable, CallbackInfo ci) {
		LOGGER.info("Finish task {} - {}, origin {}", task.getClass(), task.hashCode(), task.getOrigin());
	}
}
