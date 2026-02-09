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

package net.fabricmc.fabric.impl.environment.attribute;

import java.util.EnumMap;
import java.util.Map;

import org.jspecify.annotations.NonNull;

import net.minecraft.world.attribute.EnvironmentAttributeSystem;
import net.minecraft.world.level.Level;

import net.fabricmc.fabric.api.environment.attribute.v1.AttributeLayerPosition;
import net.fabricmc.fabric.api.environment.attribute.v1.EnvironmentAttributeEvents;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;

public class EnvironmentAttributeEventsImpl {
	private static final Map<AttributeLayerPosition, Event<EnvironmentAttributeEvents.InsertLayers>> POSITION_EVENT_MAP = new EnumMap<>(AttributeLayerPosition.class);

	@NonNull
	public static Event<EnvironmentAttributeEvents.InsertLayers> getOrCreateInsertLayersEvent(AttributeLayerPosition position) {
		return POSITION_EVENT_MAP.computeIfAbsent(position, (p -> createInsertEvent()));
	}

	public static void insertLayers(AttributeLayerPosition position, EnvironmentAttributeSystem.Builder systemBuilder, Level level) {
		Event<EnvironmentAttributeEvents.InsertLayers> event = POSITION_EVENT_MAP.get(position);

		if (event != null) {
			event.invoker().insertAttributeLayers(systemBuilder, level);
		}
	}

	private static Event<EnvironmentAttributeEvents.InsertLayers> createInsertEvent() {
		return EventFactory.createArrayBacked(EnvironmentAttributeEvents.InsertLayers.class, callbacks -> (systemBuilder, level) -> {
			for (EnvironmentAttributeEvents.InsertLayers callback : callbacks) {
				callback.insertAttributeLayers(systemBuilder, level);
			}
		});
	}
}
