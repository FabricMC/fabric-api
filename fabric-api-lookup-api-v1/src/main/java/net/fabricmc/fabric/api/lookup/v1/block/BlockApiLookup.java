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

package net.fabricmc.fabric.api.lookup.v1.block;

import java.util.function.BiFunction;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import net.fabricmc.fabric.impl.lookup.block.BlockApiLookupImpl;

/**
 * An object that allows retrieving APIs from blocks in a world.
 * Instances of this interface can be obtained through {@link #get}.
 *
 * <p>When trying to {@link BlockApiLookup#find} an API, the block or block entity at that position will be queried if it exists.
 * If it doesn't exist, or if it returns {@code null}, the fallback providers will be queried in order.
 *
 * <p>Note: If you are going to query APIs a lot, consider using {@link BlockApiCache}, it may drastically improve performance.
 *
 * <h3>Usage Example</h3>
 * Let us pretend we have the following interface that we would like to attach to some blocks depending on the direction.
 *
 * <pre>{@code
 * public interface FluidContainer {
 *     boolean containsFluids(); // return true if not empty
 * }}</pre>
 * Let us first create a static {@code BlockApiLookup} instance that will manage the registration and the query.
 *
 * <pre>{@code
 * public final class MyApi {
 *     public static final BlockApiLookup<FluidContainer, Direction> FLUID_CONTAINER = BlockApiLookup.get(Identifier.of("mymod", "fluid_container"), FluidContainer.class, Direction.class);
 * }}</pre>
 * Using that, we can query instances of {@code FluidContainer}:
 *
 * <pre>{@code
 * FluidContainer container = MyApi.FLUID_CONTAINER.find(world, pos, direction);
 * if (container != null) {
 *     // Do something with the container
 *     if (container.containsFluids()) {
 *         System.out.println("It contains fluids!");
 *     }
 * }}</pre>
 * For the query to return a useful result, functions that provide an API for a block or a block entity must be registered.
 *
 * <pre>{@code
 * // If the block entity directly implements the interface, registerSelf can be used.
 * public class ContainerBlockEntity implements FluidContainer {
 *     // ...
 * }
 * BlockEntityType<ContainerBlockEntity> CONTAINER_BLOCK_ENTITY_TYPE;
 * MyApi.FLUID_CONTAINER.registerSelf(CONTAINER_BLOCK_ENTITY_TYPE);
 *
 * // For more complicated block entity logic, registerForBlockEntities can be used.
 * // For example, let's provide a stored field, and only when the direction is UP:
 * public class MyBlockEntity {
 *     public final FluidContainer upContainer;
 *     // ...
 * }
 * MyApi.FLUID_CONTAINER.registerForBlockEntities((blockEntity, direction) -> {
 *     if (direction == Direction.UP) { // only expose from the top
 *         // return a field
 *         return ((MyBlockEntity) blockEntity).upContainer;
 *     } else {
 *         return null;
 *     }
 * }, BLOCK_ENTITY_TYPE_1, BLOCK_ENTITY_TYPE_2);
 *
 * // Without a block entity, registerForBlocks can be used.
 * MyApi.FLUID_CONTAINER.registerForBlocks((world, pos, state, blockEntity, direction) -> {
 *     // return a FluidContainer for your block, or null if there is none
 * }, BLOCK_INSTANCE, ANOTHER_BLOCK_INSTANCE); // register as many blocks as you want
 *
 * // Block entity fallback, for example to interface with another mod's FluidInventory.
 * MyApi.FLUID_CONTAINER.registerFallback((world, pos, state, blockEntity, direction) -> {
 *     if (blockEntity instanceof FluidInventory) {
 *         // return wrapper
 *     }
 *     return null;
 * });
 *
 * // General fallback, to interface with anything, for example another BlockApiLookup.
 * MyApi.FLUID_CONTAINER.registerFallback((world, pos, state, blockEntity, direction) -> {
 *     // return something if available, or null
 * });}</pre>
 *
 * <h3>Improving performance</h3>
 * When performing queries every tick, it is recommended to use {@link BlockApiCache BlockApiCache&lt;A, C&gt;}
 * instead of directly querying the {@code BlockApiLookup}.
 *
 * <pre>{@code
 * // 1) create and store an instance
 * BlockApiCache<FluidContainer, Direction> cache = BlockApiCache.create(MyApi.FLUID_CONTAINER, serverWorld, pos);
 *
 * // 2) use it later, the block entity instance will be cached among other things
 * FluidContainer container = cache.find(direction);
 * if (container != null) {
 *     // ...
 * }
 *
 * // 2bis) if the caller is able to cache the block state as well, for example by listening to neighbor updates,
 * //       that will further improve performance.
 * FluidContainer container = cache.find(direction, cachedBlockState);
 * if (container != null) {
 *     // ...
 * }
 *
 * // no need to destroy the cache, the garbage collector will take care of it}</pre>
 *
 * <h3>Generic context types</h3>
 * Note that {@code FluidContainer} and {@code Direction} were completely arbitrary in this example.
 * We can define any {@code BlockApiLookup&lt;A, C&gt;}, where {@code A} is the type of the queried API, and {@code C} is the type of the additional context
 * (the direction parameter in the previous example).
 * If no context is necessary, {@code Void} should be used, and {@code null} instances should be passed.
 *
 * @param <A> The type of the API.
 * @param <C> The type of the additional context object.
 */
@ApiStatus.NonExtendable
public interface BlockApiLookup<A, C> {
	/**
	 * Retrieve the {@link BlockApiLookup} associated with an identifier, or create it if it didn't exist yet.
	 *
	 * @param lookupId The unique identifier of the lookup.
	 * @param apiClass The class of the API.
	 * @param contextClass The class of the additional context.
	 * @return The unique lookup with the passed lookupId.
	 * @throws IllegalArgumentException If another {@code apiClass} or another {@code contextClass} was already registered with the same identifier.
	 */
	static <A, C> BlockApiLookup<A, C> get(Identifier lookupId, Class<A> apiClass, Class<C> contextClass) {
		return BlockApiLookupImpl.get(lookupId, apiClass, contextClass);
	}

	/**
	 * Attempt to retrieve an API from a block in the world.
	 * Consider using {@link BlockApiCache} if you are doing frequent queries at the same position.
	 *
	 * <p>Note: If the block state or the block entity is known, it is more efficient to use {@link BlockApiLookup#find(World, BlockPos, BlockState, BlockEntity, Object)}.
	 *
	 * @param world The world.
	 * @param pos The position of the block.
	 * @param context Additional context for the query, defined by type parameter C.
	 * @return The retrieved API, or {@code null} if no API was found.
	 */
	@Nullable
	default A find(World world, BlockPos pos, C context) {
		return find(world, pos, null, null, context);
	}

	/**
	 * Attempt to retrieve an API from a block in the world.
	 * Consider using {@link BlockApiCache} if you are doing frequent queries at the same position.
	 *
	 * @param world The world.
	 * @param pos The position of the block.
	 * @param context Additional context for the query, defined by type parameter C.
	 * @param state The block state at the target position, or null if unknown.
	 * @param blockEntity The block entity at the target position if it is known, or null if it is unknown or does not exist.
	 * @return The retrieved API, or {@code null} if no API was found.
	 */
	@Nullable
	A find(World world, BlockPos pos, @Nullable BlockState state, @Nullable BlockEntity blockEntity, C context);

	/**
	 * Expose the API for the passed block entities directly implementing it.
	 *
	 * <p>Implementation note: this is checked at registration time by creating block entity instances using the passed types.
	 *
	 * @param blockEntityTypes Block entity types for which to expose the API.
	 * @throws IllegalArgumentException If the API class is not assignable from instances of the passed block entity types.
	 */
	void registerSelf(BlockEntityType<?>... blockEntityTypes);

	/**
	 * Expose the API for the passed blocks.
	 * The mapping from the parameters of the query to the API is handled by the passed {@link BlockApiProvider}.
	 *
	 * @param provider The provider.
	 * @param blocks The blocks.
	 */
	void registerForBlocks(BlockApiProvider<A, C> provider, Block... blocks);

	/**
	 * Expose the API for instances of the passed block entity type.
	 * The mapping from the parameters of the query to the API is handled by the passed {@code provider}.
	 * This overload allows using the correct block entity class directly.
	 *
	 * <p>Note: The type is not used directly for detecting the supported blocks and block entities in the world, but it is converted to
	 * its {@linkplain BlockEntityType#blocks} when this method is called.
	 * If the {@code blocks} field is empty, {@link IllegalArgumentException} is thrown.
	 *
	 * @param <T> The block entity class for which an API is exposed.
	 * @param provider The provider: returns an API if available in the passed block entity with the passed context,
	 *                 or {@code null} if no API is available.
	 * @param blockEntityType The block entity type.
	 */
	@SuppressWarnings("unchecked")
	default <T extends BlockEntity> void registerForBlockEntity(BiFunction<? super T, C, @Nullable A> provider, BlockEntityType<T> blockEntityType) {
		registerForBlockEntities((blockEntity, context) -> provider.apply((T) blockEntity, context), blockEntityType);
	}

	/**
	 * Expose the API for instances of the passed block entity types.
	 * The mapping from the parameters of the query to the API is handled by the passed {@link BlockEntityApiProvider}.
	 * This overload allows registering multiple block entity types at once,
	 * but due to how generics work in java, the provider has to cast to the correct block entity class if necessary.
	 *
	 * <p>Note: The type is not used directly for detecting the supported blocks and block entities in the world, but it is converted to
	 * its {@linkplain BlockEntityType#blocks} when this method is called.
	 * If the {@code blocks} field is empty, {@link IllegalArgumentException} is thrown.
	 *
	 * @param provider The provider.
	 * @param blockEntityTypes The block entity types.
	 */
	void registerForBlockEntities(BlockEntityApiProvider<A, C> provider, BlockEntityType<?>... blockEntityTypes);

	/**
	 * Expose the API for all queries: the provider will be invoked if no object was found using the block or block entity providers.
	 * This may have a big performance impact on all queries, use cautiously.
	 *
	 * @param fallbackProvider The fallback provider.
	 */
	void registerFallback(BlockApiProvider<A, C> fallbackProvider);

	/**
	 * Return the identifier of this lookup.
	 */
	Identifier getId();

	/**
	 * Return the API class of this lookup.
	 */
	Class<A> apiClass();

	/**
	 * Return the context class of this lookup.
	 */
	Class<C> contextClass();

	/**
	 * Return the provider for the passed block (registered with one of the {@code register} functions), or null if none was registered (yet).
	 * Queries should go through {@link #find}, only use this to inspect registered providers!
	 */
	@Nullable
	BlockApiProvider<A, C> getProvider(Block block);

	@FunctionalInterface
	interface BlockApiProvider<A, C> {
		/**
		 * Return an API of type {@code A} if available in the world at the given pos with the given context, or {@code null} otherwise.
		 *
		 * @param world The world.
		 * @param pos The position in the world.
		 * @param state The block state.
		 * @param blockEntity The block entity, if it exists in the world.
		 * @param context Additional context passed to the query.
		 * @return An API of type {@code A}, or {@code null} if no API is available.
		 */
		@Nullable
		A find(World world, BlockPos pos, BlockState state, @Nullable BlockEntity blockEntity, C context);
	}

	@FunctionalInterface
	interface BlockEntityApiProvider<A, C> {
		/**
		 * Return an API of type {@code A} if available in the given block entity with the given context, or {@code null} otherwise.
		 *
		 * @param blockEntity The block entity. It is guaranteed that it is never null.
		 * @param context Additional context passed to the query.
		 * @return An API of type {@code A}, or {@code null} if no API is available.
		 */
		@Nullable
		A find(BlockEntity blockEntity, C context);
	}
}
