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

package net.fabricmc.fabric.impl.debug;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;

import net.minecraft.util.debug.DebugSubscription;
import net.minecraft.util.debug.DebugValueSource;
import net.minecraft.world.entity.Entity;

import net.fabricmc.fabric.api.debug.v1.DebugValueFactory;

public final class EntityDebugSubscriptionRegistryImpl {
	public static final Set<Entry> ENTITY_DEBUG_SUBSCRIPTIONS =
			new HashSet<>();

	public static <T, E extends Entity> void register(
			DebugSubscription<T> debugSubscription,
			Predicate<Entity> shouldSubscribe,
			DebugValueFactory<E, T> valueFactory
	) {
		//noinspection unchecked // Casts to super-type
		ENTITY_DEBUG_SUBSCRIPTIONS.add(new Entry(
				(DebugSubscription<Object>) debugSubscription,
				shouldSubscribe,
				(DebugValueFactory<Entity, T>) valueFactory
		));
	}

	public static void addDebugValues(
			Object entity,
			DebugValueSource.Registration registration
	) {
		for (Entry entry : ENTITY_DEBUG_SUBSCRIPTIONS) {
			if (entry.shouldSubscribe().test((Entity) entity)) {
				registration.register(
						entry.debugSubscription(),
						() -> entry.valueFactory().create((Entity) entity)
				);
			}
		}
	}

	public record Entry(
			DebugSubscription<Object> debugSubscription,
			Predicate<Entity> shouldSubscribe,
			DebugValueFactory<Entity, ?> valueFactory
	) {
		// Ensure DebugSubscriptions with different values don't both
		// get into the Set, causing undesirable behavior

		@Override
		public boolean equals(Object o) {
			if (o == null || getClass() != o.getClass()) return false;
			Entry entry = (Entry) o;
			return Objects.equals(
					debugSubscription,
					entry.debugSubscription
			);
		}

		@Override
		public int hashCode() {
			return Objects.hash(debugSubscription);
		}
	}
}
