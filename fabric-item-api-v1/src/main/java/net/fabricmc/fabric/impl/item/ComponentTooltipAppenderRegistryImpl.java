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

package net.fabricmc.fabric.impl.item;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

import net.minecraft.core.component.DataComponentType;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.component.TooltipProvider;

public final class ComponentTooltipAppenderRegistryImpl {
	private static final List<DataComponentType<? extends TooltipProvider>> first = new ArrayList<>();
	private static final List<DataComponentType<? extends TooltipProvider>> last = new ArrayList<>();
	private static final Map<DataComponentType<?>, List<DataComponentType<? extends TooltipProvider>>> before = new IdentityHashMap<>();
	private static final Map<DataComponentType<?>, List<DataComponentType<? extends TooltipProvider>>> after = new IdentityHashMap<>();
	private static boolean hasModdedEntries = false;

	public static void addFirst(DataComponentType<? extends TooltipProvider> componentType) {
		first.add(componentType);
		onModified();
	}

	public static void addLast(DataComponentType<? extends TooltipProvider> componentType) {
		last.add(componentType);
		onModified();
	}

	public static void addBefore(DataComponentType<?> anchor, DataComponentType<? extends TooltipProvider> componentType) {
		before.computeIfAbsent(anchor, k -> new ArrayList<>()).add(componentType);
		onModified();
	}

	public static void addAfter(DataComponentType<?> anchor, DataComponentType<? extends TooltipProvider> componentType) {
		after.computeIfAbsent(anchor, k -> new ArrayList<>()).add(componentType);
		onModified();
	}

	private static void onModified() {
		hasModdedEntries = true;
		VanillaTooltipAppenderOrder.load();
	}

	public static boolean hasModdedEntries() {
		return hasModdedEntries;
	}

	public static void onFirst(
			ItemStack stack,
			Item.TooltipContext context,
			TooltipDisplay displayComponent,
			Consumer<Component> textConsumer,
			TooltipFlag type
	) {
		Set<DataComponentType<?>> cycleDetector = new HashSet<>();

		for (DataComponentType<? extends TooltipProvider> componentType : first) {
			appendCustomComponentTooltip(stack, componentType, context, displayComponent, textConsumer, type, cycleDetector);
		}
	}

	public static void onLast(
			ItemStack stack,
			Item.TooltipContext context,
			TooltipDisplay displayComponent,
			Consumer<Component> textConsumer,
			TooltipFlag type
	) {
		Set<DataComponentType<?>> cycleDetector = new HashSet<>();

		for (DataComponentType<? extends TooltipProvider> componentType : last) {
			appendCustomComponentTooltip(stack, componentType, context, displayComponent, textConsumer, type, cycleDetector);
		}
	}

	public static void onBefore(
			ItemStack stack,
			DataComponentType<?> componentType,
			Item.TooltipContext context,
			TooltipDisplay displayComponent,
			Consumer<Component> textConsumer,
			TooltipFlag type,
			Set<DataComponentType<?>> cycleDetector
	) {
		List<DataComponentType<? extends TooltipProvider>> befores = before.get(componentType);

		if (befores != null) {
			for (DataComponentType<? extends TooltipProvider> beforeComponentType : befores) {
				appendCustomComponentTooltip(stack, beforeComponentType, context, displayComponent, textConsumer, type, cycleDetector);
			}
		}
	}

	public static void onAfter(
			ItemStack stack,
			DataComponentType<?> componentType,
			Item.TooltipContext context,
			TooltipDisplay displayComponent,
			Consumer<Component> textConsumer,
			TooltipFlag type,
			Set<DataComponentType<?>> cycleDetector
	) {
		List<DataComponentType<? extends TooltipProvider>> afters = after.get(componentType);

		if (afters != null) {
			for (DataComponentType<? extends TooltipProvider> afterComponentType : afters) {
				appendCustomComponentTooltip(stack, afterComponentType, context, displayComponent, textConsumer, type, cycleDetector);
			}
		}
	}

	private static void appendCustomComponentTooltip(
			ItemStack stack,
			DataComponentType<? extends TooltipProvider> componentType,
			Item.TooltipContext context,
			TooltipDisplay displayComponent,
			Consumer<Component> textConsumer,
			TooltipFlag type,
			Set<DataComponentType<?>> cycleDetector
	) {
		if (!cycleDetector.add(componentType)) {
			return;
		}

		onBefore(stack, componentType, context, displayComponent, textConsumer, type, cycleDetector);
		stack.addToTooltip(componentType, context, displayComponent, textConsumer, type);
		onAfter(stack, componentType, context, displayComponent, textConsumer, type, cycleDetector);

		cycleDetector.remove(componentType);
	}
}
