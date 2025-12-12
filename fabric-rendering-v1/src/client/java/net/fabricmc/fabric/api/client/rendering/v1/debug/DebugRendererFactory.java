package net.fabricmc.fabric.api.client.rendering.v1.debug;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.debug.DebugRenderer;

@FunctionalInterface
public interface DebugRendererFactory {
	DebugRenderer.SimpleDebugRenderer create(Minecraft minecraft);
}
