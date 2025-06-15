/*
 * Copyright (c) 2016, 2017, 2018, 2019 FabricMC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.fabricmc.fabric.api.client.rendering.v1;

import java.util.List;

import net.minecraft.client.gui.hud.DebugHud;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;

/**
 * Provides events that give control over the string lists rendered on both sides of the {@link DebugHud}.
 *
 * <p>The strings do support <a href="https://minecraft.wiki/w/Formatting_codes">formatting codes</a>.
 */
public final class GatherDebugTextEvents {
	private GatherDebugTextEvents() {
	}

	/**
	 * An event that runs when gathering all game information text lines.
	 */
	public static final Event<Left> LEFT = EventFactory.createArrayBacked(Left.class, callbacks -> lines -> {
		for (Left callback : callbacks) {
			callback.onGatherLeftDebugText(lines);
		}
	});

	/**
	 * An event that runs when gathering all system information text lines.
	 */
	public static final Event<Right> RIGHT = EventFactory.createArrayBacked(Right.class, callbacks -> lines -> {
		for (Right callback : callbacks) {
			callback.onGatherRightDebugText(lines);
		}
	});

	@FunctionalInterface
	public interface Left {
		/**
		 * @param lines the mutable result of {@link DebugHud#getLeftText()}
		 */
		void onGatherLeftDebugText(List<String> lines);
	}

	@FunctionalInterface
	public interface Right {
		/**
		 * @param lines the mutable result of {@link DebugHud#getRightText()}
		 */
		void onGatherRightDebugText(List<String> lines);
	}
}
