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

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.multiplayer.prediction.PredictiveAction;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.game.ServerboundInteractPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.network.protocol.game.ServerboundUseItemOnPacket;
import net.minecraft.network.protocol.game.ServerboundUseItemPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

import net.fabricmc.fabric.api.event.client.player.ClientPlayerBlockBreakEvents;
import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.event.player.UseItemCallback;

@Mixin(MultiPlayerGameMode.class)
public abstract class MultiPlayerGameModeMixin {
	@Shadow
	@Final
	private Minecraft minecraft;
	@Shadow
	@Final
	private ClientPacketListener connection;

	@Shadow
	protected abstract void startPrediction(ClientLevel clientLevel, PredictiveAction predictiveAction);

	@Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;getAbilities()Lnet/minecraft/world/entity/player/Abilities;", ordinal = 0), method = "startDestroyBlock", cancellable = true)
	public void attackBlock(BlockPos pos, Direction direction, CallbackInfoReturnable<Boolean> info) {
		fabric_fireAttackBlockCallback(pos, direction, info);
	}

	@Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;getAbilities()Lnet/minecraft/world/entity/player/Abilities;", ordinal = 0), method = "continueDestroyBlock", cancellable = true)
	public void method_2902(BlockPos pos, Direction direction, CallbackInfoReturnable<Boolean> info) {
		if (this.minecraft.player.getAbilities().instabuild) {
			fabric_fireAttackBlockCallback(pos, direction, info);
		}
	}

	@Unique
	private void fabric_fireAttackBlockCallback(BlockPos pos, Direction direction, CallbackInfoReturnable<Boolean> info) {
		InteractionResult result = AttackBlockCallback.EVENT.invoker().interact(minecraft.player, minecraft.level, InteractionHand.MAIN_HAND, pos, direction);

		if (result != InteractionResult.PASS) {
			// Returning true will spawn particles and trigger the animation of the hand -> only for SUCCESS.
			info.setReturnValue(result == InteractionResult.SUCCESS);

			// We also need to let the server process the action if it's accepted.
			if (result.consumesAction()) {
				startPrediction(minecraft.level, id -> new ServerboundPlayerActionPacket(ServerboundPlayerActionPacket.Action.START_DESTROY_BLOCK, pos, direction, id));
			}
		}
	}

	@Inject(method = "destroyBlock", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/Block;destroy(Lnet/minecraft/world/level/LevelAccessor;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;)V"))
	private void fabric$onBlockBroken(BlockPos pos, CallbackInfoReturnable<Boolean> cir, @Local BlockState blockState) {
		ClientPlayerBlockBreakEvents.AFTER.invoker().afterBlockBreak(minecraft.level, minecraft.player, pos, blockState);
	}

	@Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/MultiPlayerGameMode;startPrediction(Lnet/minecraft/client/multiplayer/ClientLevel;Lnet/minecraft/client/multiplayer/prediction/PredictiveAction;)V"), method = "useItemOn", cancellable = true)
	public void interactBlock(LocalPlayer player, InteractionHand hand, BlockHitResult blockHitResult, CallbackInfoReturnable<InteractionResult> info) {
		// hook interactBlock between the world border check and the actual block interaction to invoke the use block event first
		// this needs to be in interactBlock to avoid sending a packet in line with the event javadoc

		if (player.isSpectator()) return; // vanilla spectator check happens later, repeat it before the event to avoid false invocations

		InteractionResult result = UseBlockCallback.EVENT.invoker().interact(player, player.level(), hand, blockHitResult);

		if (result != InteractionResult.PASS) {
			if (result.consumesAction()) {
				// send interaction packet to the server with a new sequentially assigned id
				startPrediction((ClientLevel) player.level(), id -> new ServerboundUseItemOnPacket(hand, blockHitResult, id));
			}

			info.setReturnValue(result);
		}
	}

	@Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/MultiPlayerGameMode;ensureHasSentCarriedItem()V", ordinal = 0), method = "useItem", cancellable = true)
	public void interactItem(Player player, InteractionHand hand, CallbackInfoReturnable<InteractionResult> info) {
		// hook interactBlock between the spectator check and sending the first packet to invoke the use item event first
		// this needs to be in interactBlock to avoid sending a packet in line with the event javadoc
		InteractionResult result = UseItemCallback.EVENT.invoker().interact(player, player.level(), hand);

		if (result != InteractionResult.PASS) {
			if (result == InteractionResult.SUCCESS) {
				// send interaction packet to the server with a new sequentially assigned id
				startPrediction((ClientLevel) player.level(), id -> new ServerboundUseItemPacket(hand, id, player.getYRot(), player.getXRot()));
			}

			info.setReturnValue(result);
		}
	}

	@Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/ClientPacketListener;send(Lnet/minecraft/network/protocol/Packet;)V", ordinal = 0), method = "attack", cancellable = true)
	public void attackEntity(Player player, Entity entity, CallbackInfo info) {
		InteractionResult result = AttackEntityCallback.EVENT.invoker().interact(player, player.level(), InteractionHand.MAIN_HAND /* TODO */, entity, null);

		if (result != InteractionResult.PASS) {
			if (result == InteractionResult.SUCCESS) {
				this.connection.send(ServerboundInteractPacket.createAttackPacket(entity, player.isShiftKeyDown()));
			}

			info.cancel();
		}
	}
}
