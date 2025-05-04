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
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.Vec3d;

import net.fabricmc.fabric.api.event.client.player.ClientPreAttackCallback;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;

@Mixin(MinecraftClient.class)
public abstract class MinecraftClientMixin {
	private boolean fabric_attackCancelled;

	@Shadow
	private ClientPlayerEntity player;

	@Shadow
	public abstract ClientPlayNetworkHandler getNetworkHandler();

	@Shadow
	@Final
	public GameOptions options;

	@Shadow
	@Nullable
	public ClientPlayerInteractionManager interactionManager;

	@Shadow
	@Nullable
	public ClientWorld world;

	@Inject(
			at = @At(
					value = "INVOKE",
					target = "net/minecraft/client/network/ClientPlayerInteractionManager.interactEntityAtLocation(Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/entity/Entity;Lnet/minecraft/util/hit/EntityHitResult;Lnet/minecraft/util/Hand;)Lnet/minecraft/util/ActionResult;"
			),
			method = "doItemUse",
			cancellable = true
	)
	private void injectUseEntityCallback(CallbackInfo ci, @Local Hand hand, @Local EntityHitResult hitResult, @Local Entity entity) {
		ActionResult result = UseEntityCallback.EVENT.invoker().interact(player, player.getEntityWorld(), hand, entity, hitResult);

		if (result != ActionResult.PASS) {
			if (result.isAccepted()) {
				Vec3d hitVec = hitResult.getPos().subtract(entity.getX(), entity.getY(), entity.getZ());
				getNetworkHandler().sendPacket(PlayerInteractEntityC2SPacket.interactAt(entity, player.isSneaking(), hand, hitVec));
			}

			if (result instanceof ActionResult.Success success) {
				if (success.swingSource() == ActionResult.SwingSource.CLIENT) {
					player.swingHand(hand);
				}
			}

			ci.cancel();
		}
	}

	@Inject(
			method = "handleInputEvents",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/client/network/ClientPlayerEntity;isUsingItem()Z",
					ordinal = 0
			)
	)
	private void injectHandleInputEventsForPreAttackCallback(CallbackInfo ci) {
		int attackKeyPressCount = ((KeyBindingAccessor) options.attackKey).fabric_getTimesPressed();

		if (options.attackKey.isPressed() || attackKeyPressCount != 0) {
			fabric_attackCancelled = ClientPreAttackCallback.EVENT.invoker().onClientPlayerPreAttack(
					(MinecraftClient) (Object) this, player, attackKeyPressCount
			);
		} else {
			fabric_attackCancelled = false;
		}
	}

	@Inject(method = "doAttack", at = @At("HEAD"), cancellable = true)
	private void injectDoAttackForCancelling(CallbackInfoReturnable<Boolean> cir) {
		if (fabric_attackCancelled) {
			cir.setReturnValue(false);
		}
	}

	@Inject(method = "handleBlockBreaking", at = @At("HEAD"), cancellable = true)
	private void injectHandleBlockBreakingForCancelling(boolean breaking, CallbackInfo ci) {
		if (fabric_attackCancelled) {
			if (interactionManager != null) {
				interactionManager.cancelBlockBreaking();
			}

			ci.cancel();
		}
	}
}
