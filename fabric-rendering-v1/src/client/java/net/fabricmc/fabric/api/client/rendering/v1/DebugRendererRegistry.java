package net.fabricmc.fabric.api.client.rendering.v1;

import java.util.function.Predicate;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.debug.DebugRenderer;
import net.minecraft.util.debug.DebugSubscription;

import net.fabricmc.fabric.impl.client.rendering.DebugRendererRegistryImpl;

/**
 * Helper class for registering custom {@linkplain DebugRenderer debug renderers}.
 */
public final class DebugRendererRegistry {
	private DebugRendererRegistry() {
	}

	public static <T> void register(
			DebugRendererFactory debugRenderer,
			DebugSubscription<T> debugSubscription
	) {
		DebugRendererRegistryImpl.register(debugRenderer, debugSubscription);
	}

	public static <T> void registerConditional(
			DebugRendererFactory debugRenderer,
			DebugSubscription<T> debugSubscription,
			Predicate<Minecraft> isEnabled
	) {
		DebugRendererRegistryImpl.registerConditional(debugRenderer, debugSubscription, isEnabled);
	}
}
