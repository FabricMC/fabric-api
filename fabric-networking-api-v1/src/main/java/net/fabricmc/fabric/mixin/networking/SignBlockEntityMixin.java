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

package net.fabricmc.fabric.mixin.networking;

import java.util.Optional;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import net.minecraft.block.entity.SignBlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtElement;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

import net.fabricmc.fabric.impl.networking.CustomClickActionsRegistry;

@Mixin(SignBlockEntity.class)
public class SignBlockEntityMixin {
	@WrapOperation(
			method = "runCommandClickEvent",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/server/MinecraftServer;handleCustomClickAction(Lnet/minecraft/util/Identifier;Ljava/util/Optional;)V"
			)
	)
	private void hookCustomClickActionListener(MinecraftServer instance, Identifier id, Optional<NbtElement> payload, Operation<Void> original, @Local(argsOnly = true) PlayerEntity player) {
		original.call(instance, id, payload);

		if (player instanceof ServerPlayerEntity serverPlayer) {
			CustomClickActionsRegistry.PLAY_REGISTRY.invokeListenerEvent(
					id,
					new CustomClickActionsRegistry.PlayContextImpl(serverPlayer.networkHandler, payload.orElse(null))
			);
		}
	}
}
