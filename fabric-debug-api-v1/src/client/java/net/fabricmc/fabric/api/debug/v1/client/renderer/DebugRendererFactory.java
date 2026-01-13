package net.fabricmc.fabric.api.debug.v1.client.renderer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.debug.DebugRenderer;

/// A constructor for a
/// [debug subscription][net.minecraft.util.debug.DebugSubscription] renderer.
@FunctionalInterface
public interface DebugRendererFactory {
	/// @return a new renderer for a
	/// [debug subscription][net.minecraft.util.debug.DebugSubscription].
	DebugRenderer.SimpleDebugRenderer create(Minecraft minecraft);
}
