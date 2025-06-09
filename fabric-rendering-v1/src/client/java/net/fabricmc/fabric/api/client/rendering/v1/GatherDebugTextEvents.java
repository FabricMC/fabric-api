package net.fabricmc.fabric.api.client.rendering.v1;

import java.util.List;

import net.minecraft.client.gui.hud.DebugHud;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;

public final class GatherDebugTextEvents {
	private GatherDebugTextEvents() {
	}

	/**
	 * An event that runs when gathering all game information text lines via {@link DebugHud#getLeftText()}.
	 */
	public static final Event<Left> LEFT = EventFactory.createArrayBacked(Left.class, callbacks -> lines -> {
		for (Left callback : callbacks) {
			callback.onGatherLeftDebugText(lines);
		}
	});

	/**
	 * An event that runs when gathering all game information text lines via {@link DebugHud#getRightText()}.
	 */
	public static final Event<Right> RIGHT = EventFactory.createArrayBacked(Right.class, callbacks -> lines -> {
		for (Right callback : callbacks) {
			callback.onGatherRightDebugText(lines);
		}
	});

	@FunctionalInterface
	public interface Left {
		void onGatherLeftDebugText(List<String> lines);
	}

	@FunctionalInterface
	public interface Right {
		void onGatherRightDebugText(List<String> lines);
	}
}
