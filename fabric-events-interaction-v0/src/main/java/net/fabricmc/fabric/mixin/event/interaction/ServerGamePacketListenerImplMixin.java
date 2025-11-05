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

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.ServerboundPickItemFromBlockPacket;
import net.minecraft.network.protocol.game.ServerboundPickItemFromEntityPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockState;

import net.fabricmc.fabric.api.event.player.PlayerPickItemEvents;

@Mixin(ServerGamePacketListenerImpl.class)
public abstract class ServerGamePacketListenerImplMixin {
	@Shadow
	@Final
	private ServerPlayer player;

	@Shadow
	private void tryPickItem(ItemStack stack) {
		throw new AssertionError();
	}

	@WrapOperation(method = "handlePickItemFromBlock", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;getCloneItemStack(Lnet/minecraft/world/level/LevelReader;Lnet/minecraft/core/BlockPos;Z)Lnet/minecraft/world/item/ItemStack;"))
	public ItemStack onPickItemFromBlock(BlockState state, LevelReader world, BlockPos pos, boolean includeData, Operation<ItemStack> operation, @Local(argsOnly = true) ServerboundPickItemFromBlockPacket packet) {
		ItemStack stack = PlayerPickItemEvents.BLOCK.invoker().onPickItemFromBlock(player, pos, state, packet.includeData());

		if (stack == null) {
			return operation.call(state, world, pos, includeData);
		} else if (!stack.isEmpty()) {
			this.tryPickItem(stack);
		}

		// Prevent vanilla data-inclusion behavior
		return ItemStack.EMPTY;
	}

	@WrapOperation(method = "handlePickItemFromEntity", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;getPickResult()Lnet/minecraft/world/item/ItemStack;"))
	public ItemStack onPickItemFromEntity(Entity entity, Operation<ItemStack> operation, @Local(argsOnly = true) ServerboundPickItemFromEntityPacket packet) {
		ItemStack stack = PlayerPickItemEvents.ENTITY.invoker().onPickItemFromEntity(player, entity, packet.includeData());

		if (stack == null) {
			return operation.call(entity);
		} else if (!stack.isEmpty()) {
			this.tryPickItem(stack);
		}

		// Prevent vanilla data-inclusion behavior
		return ItemStack.EMPTY;
	}
}
