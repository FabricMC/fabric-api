package net.fabricmc.fabric.api.client.rendering.v1;

import java.util.function.Predicate;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.debug.DebugRenderer;

import net.fabricmc.fabric.impl.client.rendering.DebugRendererRegistryImpl;

/**
 * Helper class for registering custom {@linkplain DebugRenderer debug renderers}.
 */
public final class DebugRendererRegistry {
	private DebugRendererRegistry() {
	}

	public static void register(DebugRendererFactory debugRenderer) {
		DebugRendererRegistryImpl.register(debugRenderer);
	}

	public static void registerConditional(
			DebugRendererFactory debugRenderer,
			Predicate<Minecraft> isEnabled
	) {
		DebugRendererRegistryImpl.registerConditional(debugRenderer, isEnabled);
	}
}
