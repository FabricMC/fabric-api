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

import java.util.Set;
import java.util.function.Consumer;

import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.serialization.DynamicOps;

import net.fabricmc.fabric.api.networking.v1.ServerConfigurationNetworking;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.core.LayeredRegistryAccess;
import net.minecraft.nbt.Tag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket;
import net.minecraft.server.RegistryLayer;
import net.minecraft.server.network.config.SynchronizeRegistriesTask;
import net.minecraft.server.packs.repository.KnownPack;

import net.fabricmc.fabric.impl.holder.component.sync.DataComponentNetworkSerialization;

@Mixin(SynchronizeRegistriesTask.class)
public class SynchronizeRegistriesTaskMixin {
	@Shadow
	@Final
	private LayeredRegistryAccess<RegistryLayer> registries;

	@Inject(method = "sendRegistries", at = @At("TAIL"))
	private void sendComponents(
			Consumer<Packet<?>> connection,
			Set<KnownPack> negotiatedPacks,
			CallbackInfo ci,
			@Local DynamicOps<Tag> ops
	) {
		connection.accept(ServerConfigurationNetworking.createClientboundPacket(DataComponentNetworkSerialization.serialize(ops, registries)));
	}
}
