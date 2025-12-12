package net.fabricmc.fabric.test.rendering.client.mixin;

import java.util.Set;

import com.llamalad7.mixinextras.sugar.Local;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.client.multiplayer.ClientDebugSubscriber;
import net.minecraft.util.debug.DebugSubscription;

import net.fabricmc.fabric.test.rendering.DebugSubscriptions;
import net.fabricmc.loader.api.FabricLoader;

@Mixin(ClientDebugSubscriber.class)
public abstract class ClientDebugSubscriberMixin {
	@Shadow
	private static void addFlag(
			Set<DebugSubscription<?>> set,
			DebugSubscription<?> debugSubscription,
			boolean bl
	) {
	}

	@Inject(
			method = "requestedSubscriptions",
			at = @At("RETURN")
	)
	private void requestSubscriptions(CallbackInfoReturnable<Set<DebugSubscription<?>>> cir, @Local Set<DebugSubscription<?>> set) {
		if (FabricLoader.getInstance().isDevelopmentEnvironment()) {
			addFlag(set, DebugSubscriptions.SUS_AVATAR, true);
		}
	}
}
