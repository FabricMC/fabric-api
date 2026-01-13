package net.fabricmc.fabric.api.debug.v1;

import java.util.Objects;
import java.util.function.Predicate;

import net.minecraft.util.debug.DebugSubscription;
import net.minecraft.world.entity.Entity;

import net.fabricmc.fabric.impl.debug.EntityDebugSubscriptionRegistryImpl;

/// A registry for [debug subscriptions][DebugSubscription] specific to entities
/// or properties of entities.
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
