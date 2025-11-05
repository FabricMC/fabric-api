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
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockUpdatePacket;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerPlayerGameMode;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.event.player.UseItemCallback;

@Mixin(ServerPlayerGameMode.class)
public class ServerPlayerGameModeMixin {
	@Final
	@Shadow
	protected ServerPlayer player;

	@Shadow
	protected ServerLevel level;

	@Inject(at = @At("HEAD"), method = "handleBlockBreakAction", cancellable = true)
	public void startBlockBreak(BlockPos pos, ServerboundPlayerActionPacket.Action playerAction, Direction direction, int worldHeight, int i, CallbackInfo info) {
		if (playerAction != ServerboundPlayerActionPacket.Action.START_DESTROY_BLOCK) return;
		InteractionResult result = AttackBlockCallback.EVENT.invoker().interact(player, level, InteractionHand.MAIN_HAND, pos, direction);

		if (result != InteractionResult.PASS) {
			// The client might have broken the block on its side, so make sure to let it know.
			this.player.connection.send(new ClientboundBlockUpdatePacket(level, pos));

			if (level.getBlockState(pos).hasBlockEntity()) {
				BlockEntity blockEntity = level.getBlockEntity(pos);

				if (blockEntity != null) {
					Packet<ClientGamePacketListener> updatePacket = blockEntity.getUpdatePacket();

					if (updatePacket != null) {
						this.player.connection.send(updatePacket);
					}
				}
			}

			info.cancel();
		}
	}

	@Inject(at = @At("HEAD"), method = "useItemOn", cancellable = true)
	public void interactBlock(ServerPlayer player, Level world, ItemStack stack, InteractionHand hand, BlockHitResult blockHitResult, CallbackInfoReturnable<InteractionResult> info) {
		InteractionResult result = UseBlockCallback.EVENT.invoker().interact(player, world, hand, blockHitResult);

		if (result != InteractionResult.PASS) {
			info.setReturnValue(result);
			info.cancel();
			return;
		}
	}

	@Inject(at = @At("HEAD"), method = "useItem", cancellable = true)
	public void interactItem(ServerPlayer player, Level world, ItemStack stack, InteractionHand hand, CallbackInfoReturnable<InteractionResult> info) {
		InteractionResult result = UseItemCallback.EVENT.invoker().interact(player, world, hand);

		if (result != InteractionResult.PASS) {
			info.setReturnValue(result);
			info.cancel();
			return;
		}
	}

	@Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/Block;playerWillDestroy(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/entity/player/Player;)Lnet/minecraft/world/level/block/state/BlockState;"), method = "destroyBlock", cancellable = true)
	private void breakBlock(BlockPos pos, CallbackInfoReturnable<Boolean> cir, @Local BlockEntity entity, @Local BlockState state) {
		boolean result = PlayerBlockBreakEvents.BEFORE.invoker().beforeBlockBreak(this.level, this.player, pos, state, entity);

		if (!result) {
			PlayerBlockBreakEvents.CANCELED.invoker().onBlockBreakCanceled(this.level, this.player, pos, state, entity);

			cir.setReturnValue(false);
		}
	}

	@Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/Block;destroy(Lnet/minecraft/world/level/LevelAccessor;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;)V"), method = "destroyBlock")
	private void onBlockBroken(BlockPos pos, CallbackInfoReturnable<Boolean> cir, @Local BlockEntity entity, @Local BlockState state) {
		PlayerBlockBreakEvents.AFTER.invoker().afterBlockBreak(this.level, this.player, pos, state, entity);
	}
}
