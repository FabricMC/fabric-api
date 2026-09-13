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

public final class ItemComponentTooltipProviderRegistryImpl {
	private static final List<TooltipPair<?>> first = new ArrayList<>();
	private static final List<TooltipPair<?>> last = new ArrayList<>();
	private static final Map<DataComponentType<?>, List<TooltipPair<?>>> before = new IdentityHashMap<>();
	private static final Map<DataComponentType<?>, List<TooltipPair<?>>> after = new IdentityHashMap<>();
	private static boolean hasModdedEntries = false;

	public static <T> void addFirst(DataComponentType<T> componentType, TooltipProvider.Getter<T> getter) {
		first.add(new TooltipPair<>(componentType, getter));
		onModified();
	}

	public static <T> void addLast(DataComponentType<T> componentType, TooltipProvider.Getter<T> getter) {
		last.add(new TooltipPair<>(componentType, getter));
		onModified();
	}

	public static <T> void addBefore(DataComponentType<?> anchor, DataComponentType<T> componentType, TooltipProvider.Getter<T> getter) {
		before.computeIfAbsent(anchor, k -> new ArrayList<>()).add(new TooltipPair<>(componentType, getter));
		onModified();
	}

	public static <T> void addAfter(DataComponentType<?> anchor, DataComponentType<T> componentType, TooltipProvider.Getter<T> getter) {
		after.computeIfAbsent(anchor, k -> new ArrayList<>()).add(new TooltipPair<>(componentType, getter));
		onModified();
	}

	private static void onModified() {
		hasModdedEntries = true;
		VanillaTooltipProviderOrder.load();
	}

	public static boolean hasModdedEntries() {
		return hasModdedEntries;
	}

	public static void onFirst(
			ItemStack stack,
			Item.TooltipContext context,
			TooltipDisplay displayComponent,
			Consumer<Component> componentConsumer,
			TooltipFlag flag
	) {
		Set<DataComponentType<?>> cycleDetector = new HashSet<>();

		for (TooltipPair<?> tooltipPair : first) {
			tooltipPair.appendCustomComponentTooltip(stack, context, displayComponent, componentConsumer, flag, cycleDetector);
		}
	}

	public static void onLast(
			ItemStack stack,
			Item.TooltipContext context,
			TooltipDisplay displayComponent,
			Consumer<Component> componentConsumer,
			TooltipFlag flag
	) {
		Set<DataComponentType<?>> cycleDetector = new HashSet<>();

		for (TooltipPair<?> tooltipPair : last) {
			tooltipPair.appendCustomComponentTooltip(stack, context, displayComponent, componentConsumer, flag, cycleDetector);
		}
	}

	public static void onBefore(
			ItemStack stack,
			DataComponentType<?> componentType,
			Item.TooltipContext context,
			TooltipDisplay displayComponent,
			Consumer<Component> componentConsumer,
			TooltipFlag flag,
			Set<DataComponentType<?>> cycleDetector
	) {
		List<TooltipPair<?>> befores = before.get(componentType);

		if (befores != null) {
			for (TooltipPair<?> beforeTooltipPair : befores) {
				beforeTooltipPair.appendCustomComponentTooltip(stack, context, displayComponent, componentConsumer, flag, cycleDetector);
			}
		}
	}

	public static void onAfter(
			ItemStack stack,
			DataComponentType<?> componentType,
			Item.TooltipContext context,
			TooltipDisplay displayComponent,
			Consumer<Component> componentConsumer,
			TooltipFlag flag,
			Set<DataComponentType<?>> cycleDetector
	) {
		List<TooltipPair<?>> afters = after.get(componentType);

		if (afters != null) {
			for (TooltipPair<?> afterTooltipPair : afters) {
				afterTooltipPair.appendCustomComponentTooltip(stack, context, displayComponent, componentConsumer, flag, cycleDetector);
			}
		}
	}

	private record TooltipPair<T>(DataComponentType<T> component, TooltipProvider.Getter<T> getter) {
		private void appendCustomComponentTooltip(
				ItemStack stack,
				Item.TooltipContext context,
				TooltipDisplay displayComponent,
				Consumer<Component> componentConsumer,
				TooltipFlag flag,
				Set<DataComponentType<?>> cycleDetector
		) {
			if (!cycleDetector.add(this.component())) {
				return;
			}

			onBefore(stack, this.component(), context, displayComponent, componentConsumer, flag, cycleDetector);
			stack.addToTooltip(this.component(), this.getter(), context, displayComponent, componentConsumer, flag);
			onAfter(stack, this.component(), context, displayComponent, componentConsumer, flag, cycleDetector);

			cycleDetector.remove(this.component());
		}
	}
}
