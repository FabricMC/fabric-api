package net.fabricmc.fabric.mixin.recipe;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import it.unimi.dsi.fastutil.objects.ReferenceSet;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import net.minecraft.network.packet.Packet;
import net.minecraft.recipe.RecipeType;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.recipe.v1.SendSyncableRecipesCallback;
import net.fabricmc.fabric.impl.recipe.sync.FabricSyncRecipesPayload;

@Mixin(PlayerManager.class)
public class PlayerManagerMixin {
	@Final
	@Shadow
	private MinecraftServer server;

	@WrapOperation(method = "onPlayerConnect", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerPlayNetworkHandler;sendPacket(Lnet/minecraft/network/packet/Packet;)V", ordinal = 3))
	private void populateRecipes(ServerPlayNetworkHandler instance, Packet packet, Operation<Void> original, @Local(argsOnly = true) ServerPlayerEntity serverPlayer) {
		final ReferenceSet<RecipeType<?>> recipeTypesToSend = new ReferenceOpenHashSet<>();
		SendSyncableRecipesCallback.EVENT.invoker().sendRecipes(recipeTypesToSend);
		original.call(instance, packet);
		var payload = FabricSyncRecipesPayload.create(recipeTypesToSend, server.getRecipeManager().preparedRecipes);
		ServerPlayNetworking.send(serverPlayer, payload);
	}
}
