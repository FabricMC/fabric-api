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

package net.fabricmc.fabric.api.registry;

import it.unimi.dsi.fastutil.objects.Reference2IntOpenHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.GameEventTags;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.gameevent.vibrations.VibrationSystem;

/**
 * Provides a method for registering sculk sensor frequencies.
 */
public final class SculkSensorFrequencyRegistry {
	private static final Logger LOGGER = LoggerFactory.getLogger(SculkSensorFrequencyRegistry.class);

	private SculkSensorFrequencyRegistry() {
	}

	/**
	 * Registers a sculk sensor frequency for the given game event.
	 *
	 * <p>A frequency is defined as the redstone signal strength a sculk sensor will emit to a comparator when it detects a specific vibration.
	 *
	 * <p>As redstone signal strengths are limited to a maximum of 15, a frequency must also be between 1 and 15. As such, many game events will share a single frequency.
	 *
	 * <p>Note that the game event must also be in the {@linkplain GameEventTags#VIBRATIONS} tag to be detected by sculk sensors in the first place.
	 * The same applies for interactions with the Warden in the {@linkplain GameEventTags#WARDEN_CAN_LISTEN} tag.
	 *
	 * @param event The event to register the frequency for.
	 * @param frequency The frequency to register.
	 * @throws IllegalArgumentException if the given frequency is not within the allowed range.
	 */
	public static void register(ResourceKey<GameEvent> event, int frequency) {
		if (frequency <= 0 || frequency >= 16) {
			throw new IllegalArgumentException("Attempted to register Sculk Sensor frequency for event "+ event.identifier() +" with frequency "+frequency+". Sculk Sensor frequencies must be between 1 and 15 inclusive.");
		}

		final Reference2IntOpenHashMap<ResourceKey<GameEvent>> map = (Reference2IntOpenHashMap<ResourceKey<GameEvent>>) VibrationSystem.VIBRATION_FREQUENCY_FOR_EVENT;
		int replaced = map.put(event, frequency);

		if (replaced != 0) {
			LOGGER.debug("Replaced old frequency mapping for {} - was {}, now {}", event.identifier(), replaced, frequency);
		}
	}
}
