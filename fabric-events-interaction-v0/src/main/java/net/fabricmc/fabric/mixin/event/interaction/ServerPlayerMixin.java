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

package net.fabricmc.fabric.mixin.event.interaction;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.GameType;

import net.fabricmc.fabric.api.entity.FakePlayer;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;

@Mixin(ServerPlayer.class)
public class ServerPlayerMixin {
	@Inject(method = "attack", at = @At("HEAD"), cancellable = true)
	public void onPlayerInteractEntity(Entity target, CallbackInfo info) {
		ServerPlayer player = (ServerPlayer) (Object) this;
		InteractionResult result = AttackEntityCallback.EVENT.invoker().interact(player, player.level(), InteractionHand.MAIN_HAND, target, null);

		if (result != InteractionResult.PASS) {
			info.cancel();
		}
	}

	@Inject(method = "calculateGameModeForNewPlayer", at = @At("HEAD"), cancellable = true)
	public void fakePlayerGameMode(GameType backupGameMode, CallbackInfoReturnable<GameType> cir) {
		// Set the default game mode of the fake player to survival, regardless of the servers forced game mode.
		if ((Object) this instanceof FakePlayer) {
			cir.setReturnValue(GameType.SURVIVAL);
		}
	}
}
