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

package net.fabricmc.fabric.mixin.client.holder.component;

import java.util.ArrayList;
import java.util.List;

import com.llamalad7.mixinextras.sugar.Local;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.client.multiplayer.RegistryDataCollector;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.RegistrySynchronization;
import net.minecraft.core.component.DataComponentInitializers;
import net.minecraft.server.packs.resources.ResourceProvider;

import net.fabricmc.fabric.impl.client.holder.component.FabricRegistryDataCollector;
import net.fabricmc.fabric.impl.holder.component.sync.ClientboundUpdateComponentsPayload;
import net.fabricmc.fabric.impl.holder.component.sync.HolderComponentSynchronization;

@Mixin(RegistryDataCollector.class)
public class RegistryDataCollectorMixin implements FabricRegistryDataCollector {
	@Unique
	private ClientboundUpdateComponentsPayload components = new ClientboundUpdateComponentsPayload(new ArrayList<>());

	@Override
	public void fabric$appendComponents(ClientboundUpdateComponentsPayload payload) {
		components = payload;
	}

	@Inject(method = "collectGameRegistries", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/RegistryDataCollector;updateComponents(Lnet/minecraft/core/RegistryAccess$Frozen;Z)V", shift = At.Shift.AFTER))
	private void updateSyncedComponents(
			ResourceProvider knownDataSource,
			RegistryAccess.Frozen originalRegistries,
			boolean tagsAndComponentsForSynchronizedRegistriesOnly,
			CallbackInfoReturnable<RegistryAccess.Frozen> cir,
			@Local(name = "frozenRegistries") RegistryAccess.Frozen frozenRegistries
	) {
		List<DataComponentInitializers.PendingComponents<?>> pending = HolderComponentSynchronization.deserialize(
				components,
				frozenRegistries
		);

		boolean includeSharedRegistries = !tagsAndComponentsForSynchronizedRegistriesOnly;

		for (DataComponentInitializers.PendingComponents<?> prepared : pending) {
			if (includeSharedRegistries || RegistrySynchronization.isNetworkable(prepared.key())) {
				prepared.apply();
			}
		}
	}
}
