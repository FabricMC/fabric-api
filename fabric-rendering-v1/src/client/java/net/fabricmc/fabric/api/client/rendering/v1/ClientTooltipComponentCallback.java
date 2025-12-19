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

import org.jspecify.annotations.Nullable;

import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.Item;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;

/**
 * Allows registering a mapping from {@link TooltipComponent} to {@link ClientTooltipComponent}.
 * This allows custom tooltips for items: first, override {@link Item#getTooltipImage} and return a custom {@link TooltipComponent}.
 * Second, register a listener to this event and convert the component to your client component implementation if it's an instance of your component class.
 *
 * <p>Note that failure to map some components to a client component will throw an exception,
 * so make sure that any components you return in {@link Item#getTooltipImage} will be handled by one of the callbacks.
 */
public interface ClientTooltipComponentCallback {
	Event<ClientTooltipComponentCallback> EVENT = EventFactory.createArrayBacked(
			ClientTooltipComponentCallback.class, listeners -> data -> {
				for (ClientTooltipComponentCallback listener : listeners) {
					ClientTooltipComponent component = listener.getClientComponent(data);

					if (component != null) {
						return component;
					}
				}

				return null;
			});

	/**
	 * Return the client tooltip component for the passed tooltip component, or null if none is available.
	 */
	@Nullable
	ClientTooltipComponent getClientComponent(TooltipComponent component);
}
