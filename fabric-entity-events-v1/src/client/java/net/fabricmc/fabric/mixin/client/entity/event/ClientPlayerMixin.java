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

package net.fabricmc.fabric.mixin.client.entity.event;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.network.ClientPlayerEntity;

import net.fabricmc.fabric.api.entity.event.client.ClientPlayerEvents;

@Mixin(ClientPlayerEntity.class)
abstract class ClientPlayerMixin {
	@Inject(method = "tickMovement", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerEntity;isUsingItem()Z"))
	private void invokeModifyItemUseMovementSpeedEvent(CallbackInfo ci) {
		ClientPlayerEntity player = (ClientPlayerEntity) (Object) this;

		if (player.isUsingItem() && !player.hasVehicle() && (player.input.movementForward != 0.0F || player.input.movementSideways != 0.0F)) {
			Float speedPercentage = ClientPlayerEvents.MODIFY_ITEM_USE_MOVEMENT_SPEED.invoker().modifyItemUseMovementSpeed(player);

			if (speedPercentage != null) {
				player.input.movementSideways *= speedPercentage * 5.0F;
				player.input.movementForward *= speedPercentage * 5.0F;
			}
		}
	}
}
