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

package net.fabricmc.fabric.mixin.resource;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.configuration.ClientboundSelectKnownPacks;
import net.minecraft.server.network.config.SynchronizeRegistriesTask;
import net.minecraft.server.packs.repository.KnownPack;

import net.fabricmc.fabric.impl.resource.pack.ModResourcePackCreator;

@Mixin(SynchronizeRegistriesTask.class)
public abstract class SynchronizeRegistriesTaskMixin {
	@Unique
	private static final Logger LOGGER = LoggerFactory.getLogger("SynchronizeRegistriesTaskMixin");
	@Shadow
	@Final
	private List<KnownPack> requestedPacks;

	@Shadow
	protected abstract void sendRegistries(Consumer<Packet<?>> sender, Set<KnownPack> commonKnownPacks);

	@Inject(method = "handleResponse", at = @At("HEAD"), cancellable = true)
	public void onSelectKnownPacks(List<KnownPack> clientKnownPacks, Consumer<Packet<?>> sender, CallbackInfo ci) {
		if (new HashSet<>(this.requestedPacks).containsAll(clientKnownPacks)) {
			this.sendRegistries(sender, Set.copyOf(clientKnownPacks));
			ci.cancel();
		}
	}

	@Inject(method = "sendRegistries", at = @At("HEAD"))
	public void syncRegistryAndTags(Consumer<Packet<?>> sender, Set<KnownPack> commonKnownPacks, CallbackInfo ci) {
		LOGGER.debug("Synchronizing registries with common known packs: {}", commonKnownPacks);
	}

	@Inject(method = "start", at = @At("HEAD"), cancellable = true)
	private void sendPacket(Consumer<Packet<?>> sender, CallbackInfo ci) {
		if (this.requestedPacks.size() > ModResourcePackCreator.MAX_KNOWN_PACKS) {
			LOGGER.warn("Too many knownPacks: Found {}; max {}", this.requestedPacks.size(), ModResourcePackCreator.MAX_KNOWN_PACKS);
			sender.accept(new ClientboundSelectKnownPacks(this.requestedPacks.subList(0, ModResourcePackCreator.MAX_KNOWN_PACKS)));
			ci.cancel();
		}
	}
}
