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

package net.fabricmc.fabric.mixin.entity.event;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.network.Connection;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.entity.Entity;

import net.fabricmc.fabric.api.entity.event.v1.ServerEntityWorldChangeEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;

@Mixin(PlayerList.class)
abstract class PlayerListMixin {
	@Inject(method = "respawn", at = @At("TAIL"))
	private void afterRespawn(ServerPlayer oldPlayer, boolean alive, Entity.RemovalReason removalReason, CallbackInfoReturnable<ServerPlayer> cir) {
		ServerPlayer newPlayer = cir.getReturnValue();
		ServerPlayerEvents.AFTER_RESPAWN.invoker().afterRespawn(oldPlayer, newPlayer, alive);

		if (oldPlayer.level() != newPlayer.level()) {
			ServerEntityWorldChangeEvents.AFTER_PLAYER_CHANGE_WORLD.invoker().afterChangeWorld(newPlayer, oldPlayer.level(), newPlayer.level());
		}
	}

	@Inject(method = "placeNewPlayer", at = @At("RETURN"))
	private void firePlayerJoinEvent(Connection connection, ServerPlayer player, CommonListenerCookie clientData, CallbackInfo ci) {
		ServerPlayerEvents.JOIN.invoker().onJoin(player);
	}

	@Inject(method = "remove", at = @At("HEAD"))
	private void firePlayerLeaveEvent(ServerPlayer player, CallbackInfo ci) {
		ServerPlayerEvents.LEAVE.invoker().onLeave(player);
	}
}
