package net.fabricmc.fabric.mixin.networking;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import com.llamalad7.mixinextras.sugar.Local;

import net.fabricmc.fabric.impl.networking.CustomClickEventHandlerRegistry;

import net.minecraft.block.entity.SignBlockEntity;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtElement;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Optional;

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

		// base method has a serverworld parameter, so the player should be a server player
		CustomClickEventHandlerRegistry.invokeListenerEvent(id, (ServerPlayerEntity) player, payload);
	}
}
