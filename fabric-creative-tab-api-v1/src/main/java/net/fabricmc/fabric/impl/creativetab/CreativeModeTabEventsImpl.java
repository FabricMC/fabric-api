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

package net.fabricmc.fabric.impl.creativetab;

import java.util.HashMap;
import java.util.Map;

import org.jspecify.annotations.Nullable;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;

import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;

public class CreativeModeTabEventsImpl {
	private static final Map<ResourceKey<CreativeModeTab>, Event<CreativeModeTabEvents.ModifyOutput>> CREATIVE_MODE_TAB_EVENT_MAP = new HashMap<>();

	public static Event<CreativeModeTabEvents.ModifyOutput> getOrCreateModifyOutputEvent(ResourceKey<CreativeModeTab> resourceKey) {
		return CREATIVE_MODE_TAB_EVENT_MAP.computeIfAbsent(resourceKey, (g -> createModifyEvent()));
	}

	@Nullable
	public static Event<CreativeModeTabEvents.ModifyOutput> getModifyOutputEvent(ResourceKey<CreativeModeTab> resourceKey) {
		return CREATIVE_MODE_TAB_EVENT_MAP.get(resourceKey);
	}

	private static Event<CreativeModeTabEvents.ModifyOutput> createModifyEvent() {
		return EventFactory.createArrayBacked(CreativeModeTabEvents.ModifyOutput.class, callbacks -> (entries) -> {
			for (CreativeModeTabEvents.ModifyOutput callback : callbacks) {
				callback.modifyOutput(entries);
			}
		});
	}
}
