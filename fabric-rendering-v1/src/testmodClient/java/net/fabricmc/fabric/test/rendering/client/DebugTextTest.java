package net.fabricmc.fabric.test.rendering.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.GatherDebugTextEvents;

public class DebugTextTest implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		GatherDebugTextEvents.LEFT.register(lines -> {
			lines.addLast("Custom Left Side Bottom Text");
		});
		GatherDebugTextEvents.RIGHT.register(lines -> {
			lines.addFirst("Custom Right Side Top Text");
		});
	}
}
