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

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Predicate;

import com.mojang.datafixers.util.Pair;

import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import net.fabricmc.fabric.mixin.content.registry.HoeItemAccessor;

/**
 * A registry for hoe tilling interactions. A vanilla example is turning dirt to dirt paths.
 */
public final class TillableBlockRegistry {
	private TillableBlockRegistry() {
	}

	/**
	 * Registers a tilling interaction.
	 *
	 * <p>Tilling interactions are a two-step process. First, a usage predicate is run that decides whether to till
	 * a block. If the predicate returns {@code true}, an action is executed. Default instances of these can be created
	 * with these {@link HoeItem} methods:
	 * <ul>
	 * <li>usage predicate for farmland-like behavior: {@link HoeItem#onlyIfAirAbove(UseOnContext)}</li>
	 * <li>simple action: {@link HoeItem#changeIntoState(BlockState)} (BlockState)}</li>
	 * <li>simple action that also drops an item: {@link HoeItem#changeIntoStateAndDropItem(BlockState, ItemLike)}</li>
	 * </ul>
	 *
	 * @param input          the input block that can be tilled
	 * @param usagePredicate a predicate that filters if the block can be tilled
	 * @param tillingAction  an action that is executed if the predicate returns {@code true}
	 */
	public static void register(Block input, Predicate<UseOnContext> usagePredicate, Consumer<UseOnContext> tillingAction) {
		Objects.requireNonNull(input, "input block cannot be null");
		HoeItemAccessor.getTillingActions().put(input, Pair.of(usagePredicate, tillingAction));
	}

	/**
	 * Registers a simple tilling interaction.
	 *
	 * @param input          the input block that can be tilled
	 * @param usagePredicate a predicate that filters if the block can be tilled
	 * @param tilled         the tilled result block state
	 */
	public static void register(Block input, Predicate<UseOnContext> usagePredicate, BlockState tilled) {
		Objects.requireNonNull(tilled, "tilled block state cannot be null");
		register(input, usagePredicate, HoeItem.changeIntoState(tilled));
	}

	/**
	 * Registers a simple tilling interaction that also drops an item.
	 *
	 * @param input          the input block that can be tilled
	 * @param usagePredicate a predicate that filters if the block can be tilled
	 * @param tilled         the tilled result block state
	 * @param droppedItem    an item that is dropped when the input block is tilled
	 */
	public static void register(Block input, Predicate<UseOnContext> usagePredicate, BlockState tilled, ItemLike droppedItem) {
		Objects.requireNonNull(tilled, "tilled block state cannot be null");
		Objects.requireNonNull(droppedItem, "dropped item cannot be null");
		register(input, usagePredicate, HoeItem.changeIntoStateAndDropItem(tilled, droppedItem));
	}
}
