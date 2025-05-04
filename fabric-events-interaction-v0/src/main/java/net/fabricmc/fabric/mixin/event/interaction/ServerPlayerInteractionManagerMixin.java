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

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.network.packet.s2c.play.BlockUpdateS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.network.ServerPlayerInteractionManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.event.player.UseItemCallback;

@Mixin(ServerPlayerInteractionManager.class)
public class ServerPlayerInteractionManagerMixin {
	@Shadow
	protected ServerWorld world;
	@Final
	@Shadow
	protected ServerPlayerEntity player;

	@Inject(at = @At("HEAD"), method = "processBlockBreakingAction", cancellable = true)
	public void startBlockBreak(BlockPos pos, PlayerActionC2SPacket.Action playerAction, Direction direction, int worldHeight, int i, CallbackInfo info) {
		if (playerAction != PlayerActionC2SPacket.Action.START_DESTROY_BLOCK) return;
		ActionResult result = AttackBlockCallback.EVENT.invoker().interact(player, world, Hand.MAIN_HAND, pos, direction);

		if (result != ActionResult.PASS) {
			// The client might have broken the block on its side, so make sure to let it know.
			this.player.networkHandler.sendPacket(new BlockUpdateS2CPacket(world, pos));

			if (world.getBlockState(pos).hasBlockEntity()) {
				BlockEntity blockEntity = world.getBlockEntity(pos);

				if (blockEntity != null) {
					Packet<ClientPlayPacketListener> updatePacket = blockEntity.toUpdatePacket();

					if (updatePacket != null) {
						this.player.networkHandler.sendPacket(updatePacket);
					}
				}
			}

			info.cancel();
		}
	}

	@Inject(at = @At("HEAD"), method = "interactBlock", cancellable = true)
	public void interactBlock(ServerPlayerEntity player, World world, ItemStack stack, Hand hand, BlockHitResult blockHitResult, CallbackInfoReturnable<ActionResult> info) {
		ActionResult result = UseBlockCallback.EVENT.invoker().interact(player, world, hand, blockHitResult);

		if (result != ActionResult.PASS) {
			info.setReturnValue(result);
			info.cancel();
			return;
		}
	}

	@Inject(at = @At("HEAD"), method = "interactItem", cancellable = true)
	public void interactItem(ServerPlayerEntity player, World world, ItemStack stack, Hand hand, CallbackInfoReturnable<ActionResult> info) {
		ActionResult result = UseItemCallback.EVENT.invoker().interact(player, world, hand);

		if (result != ActionResult.PASS) {
			info.setReturnValue(result);
			info.cancel();
			return;
		}
	}

	@Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/block/Block;onBreak(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;Lnet/minecraft/entity/player/PlayerEntity;)Lnet/minecraft/block/BlockState;"), method = "tryBreakBlock", cancellable = true)
	private void breakBlock(BlockPos pos, CallbackInfoReturnable<Boolean> cir, @Local BlockEntity entity, @Local BlockState state) {
		boolean result = PlayerBlockBreakEvents.BEFORE.invoker().beforeBlockBreak(this.world, this.player, pos, state, entity);

		if (!result) {
			PlayerBlockBreakEvents.CANCELED.invoker().onBlockBreakCanceled(this.world, this.player, pos, state, entity);

			cir.setReturnValue(false);
		}
	}

	@Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/block/Block;onBroken(Lnet/minecraft/world/WorldAccess;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;)V"), method = "tryBreakBlock")
	private void onBlockBroken(BlockPos pos, CallbackInfoReturnable<Boolean> cir, @Local BlockEntity entity, @Local BlockState state) {
		PlayerBlockBreakEvents.AFTER.invoker().afterBlockBreak(this.world, this.player, pos, state, entity);
	}
}
