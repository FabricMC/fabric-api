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

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.network.protocol.game.ServerboundInteractPacket;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

import net.fabricmc.fabric.api.event.player.UseEntityCallback;

@Mixin(targets = "net.minecraft.server.network.ServerGamePacketListenerImpl$1")
public abstract class ServerPlayNetworkHandlerInteractEntityHandlerMixin implements ServerboundInteractPacket.Handler {
	@Shadow
	@Final
	ServerGamePacketListenerImpl field_28963;

	@Shadow
	@Final
	Entity val$target;

	@Inject(method = "onInteraction(Lnet/minecraft/world/InteractionHand;Lnet/minecraft/world/phys/Vec3;)V", at = @At(value = "HEAD"), cancellable = true)
	public void onPlayerInteractEntity(InteractionHand hand, Vec3 hitPosition, CallbackInfo info) {
		Player player = this.field_28963.player;
		Level world = player.level();

		EntityHitResult hitResult = new EntityHitResult(val$target, hitPosition.add(val$target.getX(), val$target.getY(), val$target.getZ()));
		InteractionResult result = UseEntityCallback.EVENT.invoker().interact(player, world, hand, val$target, hitResult);

		if (result != InteractionResult.PASS) {
			info.cancel();
		}
	}

	@Inject(method = "onInteraction(Lnet/minecraft/world/InteractionHand;)V", at = @At(value = "HEAD"), cancellable = true)
	public void onPlayerInteractEntity(InteractionHand hand, CallbackInfo info) {
		Player player = this.field_28963.player;
		Level world = player.level();

		InteractionResult result = UseEntityCallback.EVENT.invoker().interact(player, world, hand, val$target, null);

		if (result != InteractionResult.PASS) {
			info.cancel();
		}
	}
}
