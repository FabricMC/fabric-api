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

package net.fabricmc.fabric.mixin.advancement;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.function.Function;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import net.minecraft.server.packs.resources.SimplePreparableReloadListener;

import net.fabricmc.fabric.impl.advancement.AdvancementSourceTracker;

/**
 * Even when {@code SimpleJsonResourceReloadListener#prepare} succeeds, the whole reload can still fail
 * while waiting on the shared preparation barrier, for example because a different reload listener failed.
 * In that case {@code SimplePreparableReloadListener#apply} is never called, so
 * {@link AdvancementSourceTracker#remove} would never run for that preparations map.
 *
 * <p>{@code reload()} passes the barrier's {@code wait} method reference straight into
 * {@link CompletableFuture#thenCompose}, so the method reference is invoked via {@code invokedynamic} and
 * never appears as a plain instruction to target. Instead, this wraps the function passed to
 * {@code thenCompose} to attach the cleanup once the wait either succeeds or fails.
 */
@Mixin(SimplePreparableReloadListener.class)
public class SimplePreparableReloadListenerMixin {
	@WrapOperation(method = "reload", at = @At(value = "INVOKE", target = "Ljava/util/concurrent/CompletableFuture;thenCompose(Ljava/util/function/Function;)Ljava/util/concurrent/CompletableFuture;"))
	private CompletableFuture<Object> discardAdvancementSourcesOnReloadFailure(CompletableFuture<Object> prepareFuture, Function<Object, CompletionStage<Object>> waitOnBarrier, Operation<CompletableFuture<Object>> original) {
		Function<Object, CompletionStage<Object>> wrapped = prepared -> {
			CompletionStage<Object> waitFuture = waitOnBarrier.apply(prepared);

			if (prepared instanceof Map<?, ?> preparations) {
				// No-op for reload listeners other than the advancement one, since remove() only acts on tracked maps.
				waitFuture = waitFuture.whenComplete((result, throwable) -> {
					if (throwable != null) {
						AdvancementSourceTracker.remove(preparations);
					}
				});
			}

			return waitFuture;
		};

		return original.call(prepareFuture, wrapped);
	}
}
