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

package net.fabricmc.fabric.mixin.item;

import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentInitializers;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;

import net.fabricmc.fabric.impl.item.DefaultItemComponentImpl;

@Mixin(targets = "net.minecraft.core.component.DataComponentInitializers$1")
public abstract class DataComponentInitializersPendingComponentsMixin<T> {
	@Unique
	private HolderLookup.Provider registryLookup;

	@Shadow
	public abstract ResourceKey<? extends Registry<?>> key();

	@Inject(method = "<init>", at = @At("TAIL"))
	private void store(ResourceKey<T> par1, List<DataComponentInitializers.BakedEntry<T>> par2, CallbackInfo ci) {
		registryLookup = DefaultItemComponentImpl.LOOKUP_PROVIDER_SCOPED_VALUE.get();
	}

	@Inject(method = "apply", at = @At("RETURN"))
	private void apply(CallbackInfo ci) {
		if (Registries.ITEM.identifier().equals(key().identifier())) {
			DefaultItemComponentImpl.modifyItemComponents(registryLookup);
		}
	}
}
