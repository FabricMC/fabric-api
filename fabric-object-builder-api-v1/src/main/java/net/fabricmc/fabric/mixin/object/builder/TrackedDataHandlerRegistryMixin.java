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

package net.fabricmc.fabric.mixin.object.builder;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.entity.data.TrackedDataHandler;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;

import net.fabricmc.fabric.impl.object.builder.FabricTrackedDataRegistryImpl;
import net.fabricmc.loader.api.FabricLoader;

@Mixin(TrackedDataHandlerRegistry.class)
abstract class TrackedDataHandlerRegistryMixin {
	@Inject(method = "<clinit>", at = @At("TAIL"))
	private static void storeVanillaHandlers(CallbackInfo ci) {
		FabricTrackedDataRegistryImpl.storeVanillaHandlers();
	}

	@Inject(method = "register(Lnet/minecraft/entity/data/TrackedDataHandler;)V", at = @At("HEAD"))
	private static void onHeadRegister(TrackedDataHandler<?> handler, CallbackInfo ci) {
		if (FabricTrackedDataRegistryImpl.hasStoredVanillaHandlers() && FabricLoader.getInstance().isDevelopmentEnvironment()) {
			throw new IllegalStateException("Tried to register tracked data handler " + handler + " using TrackedDataHandlerRegistry.register. This is not allowed as it can lead to desynchronization issues; use FabricTrackedDataRegistry.register instead.");
		}
	}
}
