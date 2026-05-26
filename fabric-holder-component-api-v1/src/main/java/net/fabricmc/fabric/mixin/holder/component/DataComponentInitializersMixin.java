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

package net.fabricmc.fabric.mixin.holder.component;

import java.util.Map;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.logging.LogUtils;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentInitializers;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.packs.resources.ResourceManager;

import net.fabricmc.fabric.api.holder.component.v1.FabricDataComponentInitializer;
import net.fabricmc.fabric.impl.holder.component.FabricDataComponentInitContextImpl;
import net.fabricmc.fabric.impl.holder.component.FabricDataComponentInitializersImpl;

@Mixin(DataComponentInitializers.class)
public class DataComponentInitializersMixin {
	@ModifyReturnValue(method = "runInitializers", at = @At("RETURN"))
	private Map<ResourceKey<?>, DataComponentMap.Builder> runFabricInitializers(
			Map<ResourceKey<?>, DataComponentMap.Builder> original,
			@Local(argsOnly = true) HolderLookup.Provider holders
	) {
		if (!FabricDataComponentInitializersImpl.RESOURCE_MANAGER.isBound()) {
			// I would like to include a warning here if another mod calls this method, but we do expect vanilla to call it-
			// with this unbound in RegistryDataCollector. I could probably stackwalk but that seems like a lot of effort.

			// LOGGER.warn("DataComponentInitializers.runInitializers() was called, but RESOURCE_MANAGER is not bound!");
			return original;
		}

		ResourceManager resourceManager = FabricDataComponentInitializersImpl.RESOURCE_MANAGER.get();

		FabricDataComponentInitializer.Context context = new FabricDataComponentInitContextImpl(
				holders,
				resourceManager,
				original
		);

		for (FabricDataComponentInitializer initializer : FabricDataComponentInitializersImpl.sort()) {
			initializer.run(context);
		}

		return original;
	}
}
