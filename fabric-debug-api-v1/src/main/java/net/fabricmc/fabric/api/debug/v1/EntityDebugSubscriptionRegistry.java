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

package net.fabricmc.fabric.api.debug.v1;

import java.util.Objects;
import java.util.function.Predicate;

import net.minecraft.util.debug.DebugSubscription;
import net.minecraft.world.entity.Entity;

import net.fabricmc.fabric.impl.debug.EntityDebugSubscriptionRegistryImpl;

/// A server-side registry for [debug subscriptions][DebugSubscription] specific
/// to entities or properties of entities.
public final class EntityDebugSubscriptionRegistry {
	/// Registers a [DebugSubscription] based on a given [Entity] and
	/// [Predicate].
	///
	/// @param <T> the inner type of the [DebugSubscription].
	/// @param <E> the type of [Entity] to check against.
	/// @param debugSubscription the [DebugSubscription].
	/// @param shouldSubscribe whether an [Entity] should subscribe to this
	/// [DebugSubscription].
	/// @param valueFactory the factory for the value of type [T].
	public static <T, E extends Entity> void register(
			DebugSubscription<T> debugSubscription,
			Predicate<Entity> shouldSubscribe,
			DebugValueFactory<E, T> valueFactory
	) {
		Objects.requireNonNull(debugSubscription);
		EntityDebugSubscriptionRegistryImpl.register(
				debugSubscription,
				shouldSubscribe,
				valueFactory
		);
	}

	/// Registers a [DebugSubscription] based on a given [Entity] and
	/// [Predicate] if `isEnabledFlag` is `true`.
	///
	/// @param <T> the inner type of the [DebugSubscription].
	/// @param <E> the type of [Entity] to check against.
	/// @param debugSubscription the [DebugSubscription].
	/// @param shouldSubscribe whether an [Entity] should subscribe to this
	/// [DebugSubscription].
	/// @param valueFactory the factory for the value of type [T].
	/// @param isEnabledFlag the flag determining whether to register this
	/// [DebugSubscription].
	public static <T, E extends Entity> void register(
			DebugSubscription<T> debugSubscription,
			Predicate<Entity> shouldSubscribe,
			DebugValueFactory<E, T> valueFactory,
			boolean isEnabledFlag
	) {
		if (isEnabledFlag) {
			register(
					debugSubscription,
					shouldSubscribe,
					valueFactory
			);
		}
	}
}
