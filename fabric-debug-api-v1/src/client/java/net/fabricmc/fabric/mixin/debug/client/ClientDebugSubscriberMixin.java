package net.fabricmc.fabric.mixin.debug.client;

import java.util.Set;

import com.llamalad7.mixinextras.sugar.Local;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.client.multiplayer.ClientDebugSubscriber;
import net.minecraft.util.debug.DebugSubscription;

import net.fabricmc.fabric.impl.debug.client.ClientDebugSubscriptionRegistryImpl;

@Mixin(ClientDebugSubscriber.class)
public abstract class ClientDebugSubscriberMixin {
	@Inject(
			method = "requestedSubscriptions",
			at = @At("RETURN")
	)
	private void addSubscribers(
			CallbackInfoReturnable<Set<DebugSubscription<?>>> cir,
			@Local(name = "subscriptions") Set<DebugSubscription<?>> subscriptions
	) {
		subscriptions.addAll(ClientDebugSubscriptionRegistryImpl.DEBUG_SUBSCRIPTIONS);
	}
}
