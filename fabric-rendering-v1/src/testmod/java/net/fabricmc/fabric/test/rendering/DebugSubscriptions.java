package net.fabricmc.fabric.test.rendering;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.util.debug.DebugSubscription;

public final class DebugSubscriptions {
	public static final DebugSubscription<SusDebugInfo> SUS_PLAYER = register(
			"sus",
			SusDebugInfo.STREAM_CODEC
	);

	public static void init() {
	}

	private static <T> DebugSubscription<T> register(String name, StreamCodec<? super RegistryFriendlyByteBuf, T> streamCodec) {
		return Registry.register(BuiltInRegistries.DEBUG_SUBSCRIPTION, Identifier.fromNamespaceAndPath("fabric-rendering-v1-testmod", name), new DebugSubscription<>(streamCodec));
	}
}
