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

package net.fabricmc.fabric.api.lookup.v1.entity;

import java.util.function.BiFunction;

import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.Nullable;

import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;

import net.fabricmc.fabric.impl.lookup.entity.EntityApiLookupImpl;

/// An object that allows retrieving APIs from entities.
/// Instances of this interface can be obtained through [#get]
///
/// When trying to [#find] an API for an entity, the provider registered for the entity type will be queried if it exists.
/// If it doesn't exist, or if it returns `null`, the fallback providers will be queried in order.
/// ### Usage Example
/// Let's pretend that we have the following interface that we want to attach to entities.
///
/// ```java
/// public interface Leveled{int getLevel();}
/// ```
///
/// We need to create the EntityApiLookup. We don't need any context so we use [Void].
///
/// ```java
/// public class MyApi{public static final EntityApiLookup<Leveled, Void> LEVELED_ENTITY = EntityApiLookup.get(Identifier.fromNamespaceAndPath("modid", "leveled_entity"), Leveled.class, Void.class);}
/// ```
///
/// Now we can query instances of `Leveled`.
///
/// ```java
/// Leveled leveled = MyApi.LEVELED_ENTITY.find(entity, null);if (leveled != null){// Do something with the API.System.out.println("Entity " + entity.getScoreboardName() + " is level " + leveled.getLevel());}
/// ```
///
/// For query to return useful result, we must expose the API.
///
/// ```java
/// // If the entity directly implements the interface, registerSelf can be used.public class LeveledPig extends Pig implements Leveled{...}MyApi.LEVELED_ENTITY.registerSelf(LEVELED_PIG_ENTITY_TYPE);// Otherwise, registerForType can be used.MyApi.LEVELED_ENTITY.registerForType((zombie, ignored) ->{// Return a Leveled instance for your entity here, or null if there's none.// The context is Void in this case, so it can be ignored.}, EntityType.ZOMBIE);// Generic fallback, to interface with anything, for example if we want to all other entity level defaults to 1.MyApi.LEVELED_ENTITY.registerFallback((entity, ignored) ->{// Return something if available, or null otherwise.});
/// ```
///
/// @param <A> the type of the API we want to query.
/// @param <C> the type of the additional context object. Completely arbitrary.
///            If no context is necessary, [Void] should be used and `null` instances should be passed.
@ApiStatus.NonExtendable
public interface EntityApiLookup<A, C> {
	/// Retrieve the [EntityApiLookup] associated with an identifier, or create it if it didn't exist yet.
	///
	/// @param lookupId     the unique identifier of the lookup.
	/// @param apiClass     the class of the API.
	/// @param contextClass the class of the additional context.
	/// @return the unique lookup with the passed lookupId.
	/// @throws IllegalArgumentException If another `apiClass` or another `contextClass` was already registered with the same identifier.
	static <A, C> EntityApiLookup<A, C> get(Identifier lookupId, Class<A> apiClass, Class<C> contextClass) {
		return EntityApiLookupImpl.get(lookupId, apiClass, contextClass);
	}

	/// Attempt to retrieve an API from an entity.
	///
	/// @param entity  the entity.
	/// @param context additional context for the query, defined by type parameter C.
	/// @return The retrieved API, or `null` if no API was found.
	@Nullable
	A find(Entity entity, C context);

	/// Expose the API for the passed entities that directly implements it.
	///
	/// Implementation note: this is checked once after the first server started event fired by creating entity instances using the types.
	///
	/// @param entityTypes the entity types for which the API are exposed to.
	/// @throws IllegalArgumentException if the entity is not an instance of the API class.
	void registerSelf(EntityType<?>... entityTypes);

	/// Expose the API for instances of the entity type.
	/// This overload allows using the correct entity class directly.
	///
	/// @param <T>        the entity class for which the API is exposed to
	/// @param provider   the provider: returns an API if it's available in the entity with specified context, or `null` otherwise.
	/// @param entityType the entity type.
	@SuppressWarnings("unchecked")
	default <T extends Entity> void registerForType(BiFunction<T, C, @Nullable A> provider, EntityType<T> entityType) {
		registerForTypes((entity, context) -> provider.apply((T) entity, context), entityType);
	}

	/// Expose the API for instances of the entity types.
	/// This overload allows for registering multiple entity types at once,
	/// but due to how generics work in java, the provider has to cast to the correct type if necessary.
	///
	/// @param provider    the provider.
	/// @param entityTypes the entity types for which the API are exposed to.
	void registerForTypes(EntityApiProvider<A, C> provider, EntityType<?>... entityTypes);

	/// Expose the API for all queries: the provider will be invoked if no object was found using the entity providers.
	/// May have big performance impact on all queries, use cautiously.
	void registerFallback(EntityApiProvider<A, C> fallbackProvider);

	/// Return the identifier of this lookup.
	Identifier getId();

	/// Returns the API class of this lookup.
	Class<A> apiClass();

	/// Returns the context class of this lookup.
	Class<C> contextClass();

	/// Returns the provider for the passed entity type (registered with one of the `register` functions), or null if none was registered (yet).
	/// Queries should go through [#find], only use this to inspect registered providers!
	@Nullable
	EntityApiProvider<A, C> getProvider(EntityType<?> entityType);

	interface EntityApiProvider<A, C> {
		/// Return an instance of API `A` if available in the given entity with the given context, or `null` otherwise.
		///
		/// @param entity  the entity.
		/// @param context additional context for the query.
		@Nullable
		A find(Entity entity, C context);
	}
}
