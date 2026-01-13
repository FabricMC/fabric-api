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

import com.llamalad7.mixinextras.sugar.Local;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.network.protocol.game.ServerboundInteractPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;

import net.fabricmc.fabric.api.event.player.UseEntityCallback;

@Mixin(ServerGamePacketListenerImpl.class)
public abstract class ServerGamePacketListenerInteractEntityHandlerMixin {
	@Shadow
	public ServerPlayer player;

	@Inject(method = "handleInteract", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerPlayer;getItemInHand(Lnet/minecraft/world/InteractionHand;)Lnet/minecraft/world/item/ItemStack;"))
	public void handleInteract(ServerboundInteractPacket packet, CallbackInfo info, @Local Entity target) {
		Level level = player.level();

		EntityHitResult hitResult = new EntityHitResult(target, packet.location().add(target.getX(), target.getY(), target.getZ()));
		InteractionResult result = UseEntityCallback.EVENT.invoker().interact(player, level, packet.hand(), target, hitResult);

		if (result != InteractionResult.PASS) {
			info.cancel();
		}
	}
}
