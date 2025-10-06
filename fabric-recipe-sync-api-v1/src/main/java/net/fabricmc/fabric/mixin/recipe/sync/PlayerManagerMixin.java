package net.fabricmc.fabric.mixin.recipe.sync;

import net.fabricmc.fabric.api.recipe.sync.v1.RecipeSyncEvents;

import net.fabricmc.fabric.impl.recipe.sync.RecipeSyncHelperImpl;

import net.minecraft.network.ClientConnection;
import net.minecraft.server.PlayerManager;

import net.minecraft.server.network.ConnectedClientData;
import net.minecraft.server.network.ServerPlayNetworkHandler;

import net.minecraft.server.network.ServerPlayerEntity;

import net.minecraft.util.Identifier;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Set;

@Mixin(PlayerManager.class)
public class PlayerManagerMixin {
	@Inject(method = "onPlayerConnect", at = @At("TAIL"))
	private void fabric$onPlayerConnect(ClientConnection connection, ServerPlayerEntity player, ConnectedClientData clientData, CallbackInfo ci) {
		// Let mods queue requests for this player before sync
		Set<Identifier> syncSet = RecipeSyncHelperImpl.INSTANCE.collectQueuedSyncs(player);
		RecipeSyncEvents.BEFORE_SYNC.invoker().beforeSync(player, syncSet);

		// Actually send sync and revoke packets
		RecipeSyncHelperImpl.INSTANCE.flushToClient(player);

		// Notify mods after sync
		RecipeSyncEvents.AFTER_SYNC.invoker().afterSync(player, syncSet);
	}
}
