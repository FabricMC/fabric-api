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

package net.fabricmc.fabric.api.loot.v3;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;

import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.storage.loot.LootTable;

import net.fabricmc.fabric.impl.loot.LootModifierTargets;

/**
 * A loot modifier target checks which loot tables should be modified
 * by a {@linkplain LootModifier loot modifier}.
 *
 * <p>There are builtin targets for {@linkplain #lootTable(ResourceKey...) specific loot table ids},
 * {@linkplain #source(LootTableSource...) specific sources} and combining different targets together
 * ({@link #all(LootModifierTarget...) all()} and {@link #any(LootModifierTarget...) any()}).
 *
 * <p>New target types can be registered by registering the codec using {@link #registerType(Identifier, MapCodec)}.
 */
public interface LootModifierTarget {
	/**
	 * The loot modifier target codec.
	 */
	Codec<LootModifierTarget> CODEC = LootModifierTargets.TARGET_CODEC;

	/**
	 * Registers a new loot modifier target type.
	 *
	 * @param id    the id
	 * @param codec the map codec for the targets
	 */
	static void registerType(Identifier id, MapCodec<? extends LootModifierTarget> codec) {
		LootModifierTargets.registerType(id, codec);
	}

	/**
	 * Checks whether a loot table should be modified.
	 *
	 * @param key    the loot table's key
	 * @param source the loot table's source
	 * @return {@code true} if the table should be modified, {@code false} otherwise
	 */
	boolean shouldModify(ResourceKey<LootTable> key, LootTableSource source);

	/**
	 * {@return the map codec of this target}
	 */
	MapCodec<? extends LootModifierTarget> codec();

	/**
	 * Returns a loot modifier target that matches specific loot table keys.
	 *
	 * @param tables the keys to match
	 * @return the target
	 */
	static LootModifierTarget lootTable(ResourceKey<LootTable>... tables) {
		return lootTable(Arrays.asList(tables));
	}

	/**
	 * Returns a loot modifier target that matches specific loot table keys.
	 *
	 * @param tables the keys to match
	 * @return the target
	 */
	static LootModifierTarget lootTable(Collection<ResourceKey<LootTable>> tables) {
		return new LootModifierTargets.LootTableId(List.copyOf(tables));
	}

	/**
	 * Returns a loot modifier target that requires any child target to match.
	 *
	 * @param children the child targets, cannot be empty
	 * @return the target
	 */
	static LootModifierTarget any(LootModifierTarget... children) {
		return any(Arrays.asList(children));
	}

	/**
	 * Returns a loot modifier target that requires any child target to match.
	 *
	 * @param children the child targets, cannot be empty
	 * @return the target
	 */
	static LootModifierTarget any(Collection<? extends LootModifierTarget> children) {
		return new LootModifierTargets.RequireAny(List.copyOf(children));
	}

	/**
	 * Returns a loot modifier target that requires all child targets to match.
	 *
	 * @param children the child targets, cannot be empty
	 * @return the target
	 */
	static LootModifierTarget all(LootModifierTarget... children) {
		return all(Arrays.asList(children));
	}

	/**
	 * Returns a loot modifier target that requires all child targets to match.
	 *
	 * @param children the child targets, cannot be empty
	 * @return the target
	 */
	static LootModifierTarget all(Collection<? extends LootModifierTarget> children) {
		return new LootModifierTargets.RequireAll(List.copyOf(children));
	}

	/**
	 * Returns a loot modifier target that matches specific {@linkplain LootTableSource loot table sources}.
	 *
	 * @param sources the sources to match
	 * @return the target
	 */
	static LootModifierTarget source(LootTableSource... sources) {
		return source(Arrays.asList(sources));
	}

	/**
	 * Returns a loot modifier target that matches {@linkplain LootTableSource#isBuiltin() builtin} loot table sources.
	 *
	 * @return the target
	 */
	static LootModifierTarget builtinSource() {
		return source(LootModifierTargets.Source.BUILTIN_SOURCES);
	}

	/**
	 * Returns a loot modifier target that matches specific {@linkplain LootTableSource loot table sources}.
	 *
	 * @param sources the sources to match
	 * @return the target
	 */
	static LootModifierTarget source(Collection<LootTableSource> sources) {
		return new LootModifierTargets.Source(Set.copyOf(sources));
	}
}
