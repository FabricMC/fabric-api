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

import java.util.Map;
import java.util.function.Consumer;

import com.llamalad7.mixinextras.sugar.Local;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.client.multiplayer.ClientChunkCache;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.ClientboundLevelChunkPacketData;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.levelgen.Heightmap;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientChunkEvents;

@Mixin(ClientChunkCache.class)
public abstract class ClientChunkCacheMixin {
	@Final
	@Shadow
	ClientLevel level;

	@Inject(method = "replaceWithPacketData", at = @At("TAIL"))
	private void onChunkLoad(int x, int z, FriendlyByteBuf packetByteBuf, Map<Heightmap.Types, long[]> highmap, Consumer<ClientboundLevelChunkPacketData.BlockEntityTagOutput> consumer, CallbackInfoReturnable<LevelChunk> info) {
		ClientChunkEvents.CHUNK_LOAD.invoker().onChunkLoad(this.level, info.getReturnValue());
	}

	@Inject(method = "replaceWithPacketData", at = @At(value = "NEW", target = "net/minecraft/world/level/chunk/LevelChunk", shift = At.Shift.BEFORE))
	private void onChunkUnload(int x, int z, FriendlyByteBuf buf, Map<Heightmap.Types, long[]> highmap, Consumer<ClientboundLevelChunkPacketData.BlockEntityTagOutput> consumer, CallbackInfoReturnable<LevelChunk> info, @Local LevelChunk worldChunk) {
		if (worldChunk != null) {
			ClientChunkEvents.CHUNK_UNLOAD.invoker().onChunkUnload(this.level, worldChunk);
		}
	}

	@Inject(method = "drop", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/ClientChunkCache$Storage;drop(ILnet/minecraft/world/level/chunk/LevelChunk;)V"))
	private void onChunkUnload(ChunkPos pos, CallbackInfo ci, @Local LevelChunk chunk) {
		ClientChunkEvents.CHUNK_UNLOAD.invoker().onChunkUnload(this.level, chunk);
	}

	@Inject(
			method = "updateViewRadius",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/client/multiplayer/ClientChunkCache$Storage;inRange(II)Z"
			)
	)
	private void onUpdateLoadDistance(int loadDistance, CallbackInfo ci, @Local ClientChunkCache.Storage clientChunkMap, @Local LevelChunk oldChunk, @Local ChunkPos chunkPos) {
		if (!clientChunkMap.inRange(chunkPos.x, chunkPos.z)) {
			ClientChunkEvents.CHUNK_UNLOAD.invoker().onChunkUnload(this.level, oldChunk);
		}
	}
}
