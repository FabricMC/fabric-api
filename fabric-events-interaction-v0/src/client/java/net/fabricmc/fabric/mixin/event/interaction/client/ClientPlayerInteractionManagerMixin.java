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

package net.fabricmc.fabric.mixin.event.interaction.client;

import com.llamalad7.mixinextras.sugar.Local;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.client.network.SequencedPacketCreator;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

import net.fabricmc.fabric.api.event.client.player.ClientPlayerBlockBreakEvents;
import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.event.player.UseItemCallback;

@Mixin(ClientPlayerInteractionManager.class)
public abstract class ClientPlayerInteractionManagerMixin {
	@Shadow
	@Final
	private MinecraftClient client;
	@Shadow
	@Final
	private ClientPlayNetworkHandler networkHandler;

	@Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerEntity;getAbilities()Lnet/minecraft/entity/player/PlayerAbilities;", ordinal = 0), method = "attackBlock", cancellable = true)
	public void attackBlock(BlockPos pos, Direction direction, CallbackInfoReturnable<Boolean> info) {
		fabric_fireAttackBlockCallback(pos, direction, info);
	}

	@Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerEntity;getAbilities()Lnet/minecraft/entity/player/PlayerAbilities;", ordinal = 0), method = "updateBlockBreakingProgress", cancellable = true)
	public void method_2902(BlockPos pos, Direction direction, CallbackInfoReturnable<Boolean> info) {
		if (this.client.player.getAbilities().creativeMode) {
			fabric_fireAttackBlockCallback(pos, direction, info);
		}
	}

	@Unique
	private void fabric_fireAttackBlockCallback(BlockPos pos, Direction direction, CallbackInfoReturnable<Boolean> info) {
		ActionResult result = AttackBlockCallback.EVENT.invoker().interact(client.player, client.world, Hand.MAIN_HAND, pos, direction);

		if (result != ActionResult.PASS) {
			// Returning true will spawn particles and trigger the animation of the hand -> only for SUCCESS.
			info.setReturnValue(result == ActionResult.SUCCESS);

			// We also need to let the server process the action if it's accepted.
			if (result.isAccepted()) {
				sendSequencedPacket(client.world, id -> new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.START_DESTROY_BLOCK, pos, direction, id));
			}
		}
	}

	@Inject(method = "breakBlock", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/Block;onBroken(Lnet/minecraft/world/WorldAccess;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;)V"))
	private void fabric$onBlockBroken(BlockPos pos, CallbackInfoReturnable<Boolean> cir, @Local BlockState blockState) {
		ClientPlayerBlockBreakEvents.AFTER.invoker().afterBlockBreak(client.world, client.player, pos, blockState);
	}

	@Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerInteractionManager;sendSequencedPacket(Lnet/minecraft/client/world/ClientWorld;Lnet/minecraft/client/network/SequencedPacketCreator;)V"), method = "interactBlock", cancellable = true)
	public void interactBlock(ClientPlayerEntity player, Hand hand, BlockHitResult blockHitResult, CallbackInfoReturnable<ActionResult> info) {
		// hook interactBlock between the world border check and the actual block interaction to invoke the use block event first
		// this needs to be in interactBlock to avoid sending a packet in line with the event javadoc

		if (player.isSpectator()) return; // vanilla spectator check happens later, repeat it before the event to avoid false invocations

		ActionResult result = UseBlockCallback.EVENT.invoker().interact(player, player.getWorld(), hand, blockHitResult);

		if (result != ActionResult.PASS) {
			if (result == ActionResult.SUCCESS) {
				// send interaction packet to the server with a new sequentially assigned id
				sendSequencedPacket(player.clientWorld, id -> new PlayerInteractBlockC2SPacket(hand, blockHitResult, id));
			}

			info.setReturnValue(result);
		}
	}

	@Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerInteractionManager;syncSelectedSlot()V", ordinal = 0), method = "interactItem", cancellable = true)
	public void interactItem(PlayerEntity player, Hand hand, CallbackInfoReturnable<ActionResult> info) {
		// hook interactBlock between the spectator check and sending the first packet to invoke the use item event first
		// this needs to be in interactBlock to avoid sending a packet in line with the event javadoc
		ActionResult result = UseItemCallback.EVENT.invoker().interact(player, player.getWorld(), hand);

		if (result != ActionResult.PASS) {
			if (result == ActionResult.SUCCESS) {
				// send interaction packet to the server with a new sequentially assigned id
				sendSequencedPacket((ClientWorld) player.getWorld(), id -> new PlayerInteractItemC2SPacket(hand, id, player.getYaw(), player.getPitch()));
			}

			info.setReturnValue(result);
		}
	}

	@Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayNetworkHandler;sendPacket(Lnet/minecraft/network/packet/Packet;)V", ordinal = 0), method = "attackEntity", cancellable = true)
	public void attackEntity(PlayerEntity player, Entity entity, CallbackInfo info) {
		ActionResult result = AttackEntityCallback.EVENT.invoker().interact(player, player.getWorld(), Hand.MAIN_HAND /* TODO */, entity, null);

		if (result != ActionResult.PASS) {
			if (result == ActionResult.SUCCESS) {
				this.networkHandler.sendPacket(PlayerInteractEntityC2SPacket.attack(entity, player.isSneaking()));
			}

			info.cancel();
		}
	}

	@Shadow
	protected abstract void sendSequencedPacket(ClientWorld clientWorld, SequencedPacketCreator supplier);
}
