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

package net.fabricmc.fabric.impl.loot;

import java.util.List;
import java.util.Objects;
import java.util.Set;

import com.google.common.base.Preconditions;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.storage.loot.LootTable;

import net.fabricmc.fabric.api.loot.v3.LootModifierTarget;
import net.fabricmc.fabric.api.loot.v3.LootTableSource;

public final class LootModifierTargets {
	// TODO: or registry?
	private static final BiMap<Identifier, MapCodec<? extends LootModifierTarget>> TARGET_CODECS = HashBiMap.create();

	public static final Codec<LootModifierTarget> TARGET_CODEC = Identifier.CODEC.dispatch(
			target -> TARGET_CODECS.inverse().get(target.codec()),
			TARGET_CODECS::get
	);

	static {
		registerType(Identifier.fromNamespaceAndPath("fabric", "require_all"), RequireAll.MAP_CODEC);
		registerType(Identifier.fromNamespaceAndPath("fabric", "require_any"), RequireAny.MAP_CODEC);
		registerType(Identifier.fromNamespaceAndPath("fabric", "loot_table_id"), LootTableId.MAP_CODEC);
		registerType(Identifier.fromNamespaceAndPath("fabric", "source"), Source.MAP_CODEC);
	}

	public static void registerType(Identifier id, MapCodec<? extends LootModifierTarget> codec) {
		Objects.requireNonNull(id, "Loot modifier target id cannot be null");
		Objects.requireNonNull(codec, "Loot modifier target codec cannot be null");

		if (TARGET_CODECS.putIfAbsent(id, codec) != null) {
			throw new IllegalStateException("Loot modifier target " + id + " has already been registered");
		}
	}

	public record RequireAll(List<LootModifierTarget> children) implements LootModifierTarget {
		public static final MapCodec<RequireAll> MAP_CODEC = TARGET_CODEC.listOf(1, Integer.MAX_VALUE).fieldOf("children").xmap(RequireAll::new, RequireAll::children);

		public RequireAll {
			Preconditions.checkArgument(!children.isEmpty(), "require_all loot modifier target must have children");
		}

		@Override
		public boolean shouldModify(ResourceKey<LootTable> key, LootTableSource source) {
			for (LootModifierTarget child : children) {
				if (!child.shouldModify(key, source)) {
					return false;
				}
			}

			return true;
		}

		@Override
		public MapCodec<? extends LootModifierTarget> codec() {
			return MAP_CODEC;
		}
	}

	public record RequireAny(List<LootModifierTarget> children) implements LootModifierTarget {
		public static final MapCodec<RequireAny> MAP_CODEC = TARGET_CODEC.listOf(1, Integer.MAX_VALUE).fieldOf("children").xmap(RequireAny::new, RequireAny::children);

		public RequireAny {
			Preconditions.checkArgument(!children.isEmpty(), "require_any loot modifier target must have children");
		}

		@Override
		public boolean shouldModify(ResourceKey<LootTable> key, LootTableSource source) {
			for (LootModifierTarget child : children) {
				if (child.shouldModify(key, source)) {
					return true;
				}
			}

			return false;
		}

		@Override
		public MapCodec<? extends LootModifierTarget> codec() {
			return MAP_CODEC;
		}
	}

	public record LootTableId(List<ResourceKey<LootTable>> lootTables) implements LootModifierTarget {
		public static final MapCodec<LootTableId> MAP_CODEC = ResourceKey.codec(Registries.LOOT_TABLE).listOf().fieldOf("loot_tables").xmap(LootTableId::new, LootTableId::lootTables);

		@Override
		public boolean shouldModify(ResourceKey<LootTable> key, LootTableSource source) {
			return lootTables.contains(key);
		}

		@Override
		public MapCodec<? extends LootModifierTarget> codec() {
			return MAP_CODEC;
		}
	}

	public record Source(Set<LootTableSource> sources) implements LootModifierTarget {
		private static final String BUILTIN_SOURCES_KEY = "any_builtin";
		public static final Set<LootTableSource> BUILTIN_SOURCES = Set.of(LootTableSource.VANILLA, LootTableSource.MOD);

		private static final Codec<Set<LootTableSource>> SOURCE_LIST_CODEC =
				Codec.either(
						LootTableSource.CODEC.listOf().xmap(Set::copyOf, List::copyOf),
						Codec.stringResolver(sources -> BUILTIN_SOURCES.equals(sources) ? BUILTIN_SOURCES_KEY : null, key -> BUILTIN_SOURCES_KEY.equals(key) ? BUILTIN_SOURCES : null)
				)
				.xmap(Either::unwrap, sources -> BUILTIN_SOURCES.equals(sources) ? Either.right(sources) : Either.left(sources));
		public static final MapCodec<Source> MAP_CODEC = SOURCE_LIST_CODEC.fieldOf("sources").xmap(Source::new, Source::sources);

		@Override
		public boolean shouldModify(ResourceKey<LootTable> key, LootTableSource source) {
			return sources.contains(source);
		}

		@Override
		public MapCodec<? extends LootModifierTarget> codec() {
			return MAP_CODEC;
		}
	}
}
