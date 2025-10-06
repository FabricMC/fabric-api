package net.fabricmc.fabric.mixin.recipe.sync;

import java.util.Collection;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

import net.fabricmc.fabric.api.recipe.sync.v1.RecipeSyncEvents;
import net.fabricmc.fabric.impl.recipe.sync.RecipeSyncHelperImpl;

@Mixin(MinecraftServer.class)
public class MinecraftServerMixin {
	@Inject(method = "reloadResources", at = @At("TAIL"))
	private void fabric$onRecipesReload(Collection<String> dataPacks, CallbackInfoReturnable<CompletableFuture<Void>> cir) {
		RecipeSyncHelperImpl.INSTANCE.clearRequests();
		MinecraftServer server = (MinecraftServer) (Object) this;
		RecipeSyncHelperImpl.INSTANCE.clearRequests();
		for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
			Set<Identifier> syncSet = RecipeSyncHelperImpl.INSTANCE.collectQueuedSyncs(player);
			RecipeSyncEvents.BEFORE_SYNC.invoker().beforeSync(player, syncSet);
			RecipeSyncHelperImpl.INSTANCE.flushToClient(player);
			RecipeSyncEvents.AFTER_SYNC.invoker().afterSync(player, syncSet);
		}
	}
}
