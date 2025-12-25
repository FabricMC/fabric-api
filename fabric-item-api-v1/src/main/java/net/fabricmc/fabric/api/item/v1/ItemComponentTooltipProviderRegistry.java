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

package net.fabricmc.fabric.api.item.v1;

import com.google.common.base.Preconditions;
import org.jetbrains.annotations.ApiStatus;

import net.minecraft.core.component.DataComponentType;
import net.minecraft.world.item.component.TooltipProvider;

import net.fabricmc.fabric.impl.item.ItemComponentTooltipProviderRegistryImpl;

/**
 * A registry of {@link TooltipProvider} item components. Adding your item component to this registry will render the
 * item component to the tooltip of them item when it is present, in a location relative to other item components.
 */
@ApiStatus.NonExtendable
public interface ItemComponentTooltipProviderRegistry {
	/**
	 * Adds the specified item component type to the list of tooltip providers to be called first. The component will
	 * render at the top of the tooltip.
	 *
	 * @param componentType the component type to add
	 */
	static void addFirst(DataComponentType<? extends TooltipProvider> componentType) {
		Preconditions.checkNotNull(componentType, "componentType");
		ItemComponentTooltipProviderRegistryImpl.addFirst(componentType);
	}

	/**
	 * Adds the specified item component type to the list of tooltip providers to be called last. The component will
	 * render at the bottom of the tooltip.
	 *
	 * @param componentType the component type to add
	 */
	static void addLast(DataComponentType<? extends TooltipProvider> componentType) {
		Preconditions.checkNotNull(componentType, "componentType");
		ItemComponentTooltipProviderRegistryImpl.addLast(componentType);
	}

	/**
	 * Adds the specified item component type to the list of tooltip providers so that it will render
	 * before the tooltip provider associated with the specified anchor component type.
	 *
	 * @param anchor the component type before which the specified component type will be rendered
	 * @param componentType the component type to add
	 */
	static void addBefore(DataComponentType<?> anchor, DataComponentType<? extends TooltipProvider> componentType) {
		Preconditions.checkNotNull(anchor, "anchor");
		Preconditions.checkNotNull(componentType, "componentType");
		ItemComponentTooltipProviderRegistryImpl.addBefore(anchor, componentType);
	}

	/**
	 * Adds the specified item component type to the list of tooltip providers so that it will render
	 * after the tooltip provider associated with the specified anchor component type.
	 *
	 * @param anchor the component type after which the specified component type will be rendered
	 * @param componentType the component type to add
	 */
	static void addAfter(DataComponentType<?> anchor, DataComponentType<? extends TooltipProvider> componentType) {
		Preconditions.checkNotNull(anchor, "anchor");
		Preconditions.checkNotNull(componentType, "componentType");
		ItemComponentTooltipProviderRegistryImpl.addAfter(anchor, componentType);
	}
}
