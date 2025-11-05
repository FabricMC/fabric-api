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

package net.fabricmc.fabric.mixin.event.lifecycle.client;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.protocol.common.ClientboundUpdateTagsPacket;
import net.minecraft.network.protocol.game.ClientboundLoginPacket;
import net.minecraft.network.protocol.game.ClientboundRespawnPacket;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.LevelChunk;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientBlockEntityEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.CommonLifecycleEvents;
import net.fabricmc.fabric.impl.event.lifecycle.LoadedChunksCache;

@Mixin(ClientPacketListener.class)
abstract class ClientPacketListenerMixin {
	@Shadow
	private ClientLevel level;

	@Shadow
	@Final
	private RegistryAccess.Frozen registryAccess;

	@Inject(method = "handleRespawn", at = @At(value = "NEW", target = "net/minecraft/client/multiplayer/ClientLevel"))
	private void onPlayerRespawn(ClientboundRespawnPacket packet, CallbackInfo ci) {
		// If a world already exists, we need to unload all (block)entities in the world.
		if (this.level != null) {
			for (Entity entity : this.level.entitiesForRendering()) {
				ClientEntityEvents.ENTITY_UNLOAD.invoker().onUnload(entity, this.level);
			}

			for (LevelChunk chunk : ((LoadedChunksCache) this.level).fabric_getLoadedChunks()) {
				for (BlockEntity blockEntity : chunk.getBlockEntities().values()) {
					ClientBlockEntityEvents.BLOCK_ENTITY_UNLOAD.invoker().onUnload(blockEntity, this.level);
				}
			}
		}
	}

	/**
	 * An explanation why we unload entities during onGameJoin:
	 * Proxies such as Waterfall may send another Game Join packet if entity meta rewrite is disabled, so we will cover ourselves.
	 * Velocity by default will send a Game Join packet when the player changes servers, which will create a new client world.
	 * Also anyone can send another GameJoinPacket at any time, so we need to watch out.
	 */
	@Inject(method = "handleLogin", at = @At(value = "NEW", target = "net/minecraft/client/multiplayer/ClientLevel"))
	private void onGameJoin(ClientboundLoginPacket packet, CallbackInfo ci) {
		// If a world already exists, we need to unload all (block)entities in the world.
		if (this.level != null) {
			for (Entity entity : level.entitiesForRendering()) {
				ClientEntityEvents.ENTITY_UNLOAD.invoker().onUnload(entity, this.level);
			}

			for (LevelChunk chunk : ((LoadedChunksCache) this.level).fabric_getLoadedChunks()) {
				for (BlockEntity blockEntity : chunk.getBlockEntities().values()) {
					ClientBlockEntityEvents.BLOCK_ENTITY_UNLOAD.invoker().onUnload(blockEntity, this.level);
				}
			}
		}
	}

	// Called when the client disconnects from a server or enters reconfiguration.
	@Inject(method = "clearLevel", at = @At("HEAD"))
	private void onClearWorld(CallbackInfo ci) {
		// If a world already exists, we need to unload all (block)entities in the world.
		if (this.level != null) {
			for (Entity entity : this.level.entitiesForRendering()) {
				ClientEntityEvents.ENTITY_UNLOAD.invoker().onUnload(entity, this.level);
			}

			for (LevelChunk chunk : ((LoadedChunksCache) this.level).fabric_getLoadedChunks()) {
				for (BlockEntity blockEntity : chunk.getBlockEntities().values()) {
					ClientBlockEntityEvents.BLOCK_ENTITY_UNLOAD.invoker().onUnload(blockEntity, this.level);
				}
			}
		}
	}

	@Inject(method = "handleUpdateTags", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/entity/FuelValues;vanillaBurnTimes(Lnet/minecraft/core/HolderLookup$Provider;Lnet/minecraft/world/flag/FeatureFlagSet;)Lnet/minecraft/world/level/block/entity/FuelValues;"))
	private void invokeTagsLoaded(ClientboundUpdateTagsPacket packet, CallbackInfo ci) {
		CommonLifecycleEvents.TAGS_LOADED.invoker().onTagsLoaded(registryAccess, true);
	}
}
