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

package net.fabricmc.fabric.mixin.registry.sync;

import java.util.Set;
import java.util.function.BiConsumer;

import com.mojang.serialization.DynamicOps;
import org.spongepowered.asm.mixin.Dynamic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.RegistrySynchronization;
import net.minecraft.resources.RegistryDataLoader;

import net.fabricmc.fabric.impl.registry.sync.DynamicRegistriesImpl;

// Implements skipping empty dynamic registries with the SKIP_WHEN_EMPTY sync option.
@Mixin(RegistrySynchronization.class)
abstract class RegistrySynchronizationMixin {
	/**
	 * Used for tag syncing.
	 */
	@Dynamic("method_45961: Stream.filter in stream")
	@Inject(method = "method_56601", at = @At("HEAD"), cancellable = true)
	private static void filterNonSyncedEntries(RegistryAccess.RegistryEntry<?> entry, CallbackInfoReturnable<Boolean> cir) {
		boolean canSkip = DynamicRegistriesImpl.SKIP_EMPTY_SYNC_REGISTRIES.contains(entry.key());

		if (canSkip && entry.value().size() == 0) {
			cir.setReturnValue(false);
		}
	}

	/**
	 * Used for registry serialization.
	 */
	@Dynamic("method_56597: Optional.ifPresent in serialize")
	@Inject(method = "method_56596", at = @At("HEAD"), cancellable = true)
	private static void filterNonSyncedEntriesAgain(Set set, RegistryDataLoader.RegistryData entry, DynamicOps dynamicOps, BiConsumer biConsumer, Registry registry, CallbackInfo ci) {
		boolean canSkip = DynamicRegistriesImpl.SKIP_EMPTY_SYNC_REGISTRIES.contains(registry.key());

		if (canSkip && registry.size() == 0) {
			ci.cancel();
		}
	}
}
