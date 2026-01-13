package net.fabricmc.fabric.impl.debug.client;

import java.util.HashSet;
import java.util.Set;

import net.minecraft.util.debug.DebugSubscription;

public final class ClientDebugSubscriptionRegistryImpl {
	public static final Set<DebugSubscription<?>> DEBUG_SUBSCRIPTIONS = new HashSet<>();

	public static <T> void register(DebugSubscription<T> debugSubscription) {
		DEBUG_SUBSCRIPTIONS.add(debugSubscription);
	}
}
