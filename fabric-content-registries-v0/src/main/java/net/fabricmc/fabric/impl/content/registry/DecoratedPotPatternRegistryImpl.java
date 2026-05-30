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

package net.fabricmc.fabric.impl.content.registry;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiConsumer;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.entity.DecoratedPotPattern;

public class DecoratedPotPatternRegistryImpl {
	private static final Map<ResourceKey<Item>, ResourceKey<DecoratedPotPattern>> DECORATED_POT_PATTERNS = new HashMap<>();
	private static final AtomicBoolean LOCKED = new AtomicBoolean(false);

	private DecoratedPotPatternRegistryImpl() {
	}

	public static void registerPattern(ResourceKey<Item> sherd, ResourceKey<DecoratedPotPattern> pattern) {
		Objects.requireNonNull(sherd, "Sherd item cannot be null!");
		Objects.requireNonNull(pattern, "Pattern key cannot be null!");

		if (LOCKED.get()) {
			throw new IllegalStateException("Cannot register decorated pot pattern after registry has been locked!");
		}

		DECORATED_POT_PATTERNS.put(sherd, pattern);
	}

	public static void apply(BiConsumer<ResourceKey<Item>, ResourceKey<DecoratedPotPattern>> consumer) {
		LOCKED.set(true);
		DECORATED_POT_PATTERNS.forEach(consumer);
	}
}
