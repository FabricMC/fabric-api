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

import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Objects;

import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.PathType;

/// A registry to associate block states with specific path types.
/// Specifying a path type for a block will change the way an entity recognizes the block when trying to pathfind.
/// You can make a safe block dangerous and vice-versa.
/// This works only for entities that move on air and land.
/// Duplicated registrations for the same block will replace the previous registration entry.
public final class LandPathTypeRegistry {
	private static final Logger LOGGER = LoggerFactory.getLogger(LandPathTypeRegistry.class);
	private static final Map<Block, PathTypeProvider> PATH_TYPES = new IdentityHashMap<>();

	private LandPathTypeRegistry() {
	}

	/// Registers a [PathType] for the specified block, overriding the default block behavior.
	///
	/// @param block              Block to register.
	/// @param pathType           [PathType] to associate with the block if it is a direct target
	///                           in an entity path.
	///                           (Pass `null` to not specify a path type and use the default behavior)
	/// @param pathTypeIfNeighbor [PathType] to associate with the block, if it is in a direct neighbor
	///                           position to an entity path that is directly next to a block
	///                           that the entity will pass through or above.
	///                           (Pass `null` to not specify a path type and use the default behavior)
	public static void register(Block block, @Nullable PathType pathType, @Nullable PathType pathTypeIfNeighbor) {
		Objects.requireNonNull(block, "Block cannot be null!");

		// Registers a provider that always returns the specified path type.
		register(block, (state, neighbor) -> neighbor ? pathTypeIfNeighbor : pathType);
	}

	/// Registers a [StaticPathTypeProvider] for the specified block overriding the default block behavior.
	///
	/// A static provider provides the path type basing on the block state.
	///
	/// @param block    Block to register.
	/// @param provider [StaticPathTypeProvider] to associate with the block.
	public static void register(Block block, StaticPathTypeProvider provider) {
		Objects.requireNonNull(block, "Block cannot be null!");
		Objects.requireNonNull(provider, "StaticPathTypeProvider cannot be null!");

		// Registers the provider.
		PathTypeProvider old = PATH_TYPES.put(block, provider);

		if (old != null) {
			LOGGER.debug("Replaced PathType provider for the block {}", block);
		}
	}

	/// Registers a [DynamicPathTypeProvider] for the specified block, overriding the default block behavior.
	///
	/// A dynamic provider provides the path type basing on the block state, level and position.
	/// This is more difficult to handle, must be used only if you want to change the path type basing on the position
	/// of the block in the world, and may degrade the game performances because cannot be optimized but must be
	/// recalculated at every tick for every entity.
	///
	/// @param block    Block to register.
	/// @param provider [DynamicPathTypeProvider] to associate with the block.
	public static void registerDynamic(Block block, DynamicPathTypeProvider provider) {
		Objects.requireNonNull(block, "Block cannot be null!");
		Objects.requireNonNull(provider, "DynamicPathTypeProvider cannot be null!");

		// Registers the provider.
		PathTypeProvider old = PATH_TYPES.put(block, provider);

		if (old != null) {
			LOGGER.debug("Replaced PathType provider for the block {}", block);
		}
	}

	/// Gets the [PathType] from the provider registered for the specified block state at the specified position.
	///
	/// If no valid [PathType] provider is registered for the block, it returns `null`.
	/// You cannot use this method to retrieve vanilla block path types.
	///
	/// @param state    Current block state.
	/// @param level    Current level.
	/// @param pos      Current position.
	/// @param neighbor Specifies if the block is not a directly targeted block, but a neighbor block in the path.
	/// @return the custom [PathType] from the provider registered for the specified block,
	/// passing the block state, the level, and the position to the provider, or `null` if no valid
	/// provider is registered for the block.
	@Nullable
	public static PathType getPathType(BlockState state, BlockGetter level, BlockPos pos, boolean neighbor) {
		Objects.requireNonNull(state, "BlockState cannot be null!");
		Objects.requireNonNull(level, "BlockGetter cannot be null!");
		Objects.requireNonNull(pos, "BlockPos cannot be null!");

		// Gets the path type provider for the block.
		PathTypeProvider provider = getPathTypeProvider(state.getBlock());

		//If no provider exists, returns null.
		if (provider == null) return null;

		//If a provider exists, returns the path type obtained from the provider.
		//The path type can be null too.
		if (provider instanceof DynamicPathTypeProvider) {
			return ((DynamicPathTypeProvider) provider).getPathType(state, level, pos, neighbor);
		} else {
			return ((StaticPathTypeProvider) provider).getPathType(state, neighbor);
		}
	}

	/// Gets the raw [PathTypeProvider] registered for the specified block.
	///
	/// If no [PathTypeProvider] is registered for the block, it returns `null`.
	///
	/// Note 1: [PathTypeProvider] is a marker interface with no methods,
	/// so you need to cast the result to a subtype, in order to get something from it.
	/// Currently, if non-null, the result can be of [StaticPathTypeProvider]
	/// or [DynamicPathTypeProvider].
	/// Note that more kinds of providers might be added if the API is expanded in the future,
	/// so make sure not to fail if another type of object is returned.
	///
	/// Note 2: This method is intended to be used in any cases in which you need to get
	/// the raw provider for the block, if you need the [PathType] for the block state instead,
	/// you can simply use [#getPathType].
	///
	/// @param block Current block.
	/// @return the [PathTypeProvider] registered for the specified block,
	/// or `null` if no provider is registered for the block.
	@Nullable
	public static PathTypeProvider getPathTypeProvider(Block block) {
		Objects.requireNonNull(block, "Block cannot be null!");

		return PATH_TYPES.get(block);
	}

	/// Generic provider, this is a marker interface.
	public sealed interface PathTypeProvider permits StaticPathTypeProvider, DynamicPathTypeProvider {
	}

	/// A functional interface that provides the [PathType], given the block state.
	@FunctionalInterface
	public non-sealed interface StaticPathTypeProvider extends PathTypeProvider {
		/// Gets the [PathType] for the specified block state.
		///
		/// You can specify what to return if the block state is a direct target of an entity path,
		/// or a neighbor block of the entity path.
		///
		/// For example, for a cactus-like block you should use [PathType#DAMAGE_OTHER] if the block
		/// is a direct target in the entity path (`neighbor == false`) to specify that an entity should not pass
		/// through or above the block because it will cause damage, and you should use [PathType#DANGER_OTHER]
		/// if the block is a neighbor block in the entity path (`neighbor == true`) to specify that the entity
		/// should not get close to the block because it is dangerous.
		///
		/// @param state    Current block state.
		/// @param neighbor Specifies that the block is in a direct neighbor position to an entity path
		///                 that is directly next to a block that the entity will pass through or above.
		/// @return the custom [PathType] registered for the specified block state.
		@Nullable
		PathType getPathType(BlockState state, boolean neighbor);
	}

	/// A functional interface that provides the [PathType], given the block state level and position.
	@FunctionalInterface
	public non-sealed interface DynamicPathTypeProvider extends PathTypeProvider {
		/// Gets the [PathType] for the specified block state at the specified position.
		///
		/// You can specify what to return if the block state is a direct target of an entity path,
		/// or a neighbor block of the entity path.
		///
		/// For example, for a cactus-like block you should specify [PathType#DAMAGE_OTHER] if the block
		/// is a direct target (`neighbor == false`) to specify that an entity should not pass through or above
		/// the block because it will cause damage, and [PathType#DANGER_OTHER] if the cactus will be found
		/// as a neighbor block in the entity path (`neighbor == true`) to specify that the entity should not get
		/// close to the block because is dangerous.
		///
		/// @param state    Current block state.
		/// @param level    Current level.
		/// @param pos      Current position.
		/// @param neighbor Specifies that the block is in a direct neighbor position to an entity path
		///                 (directly next to a block that the entity will pass through or above).
		/// @return the custom [PathType] registered for the specified block state at the specified position.
		@Nullable
		PathType getPathType(BlockState state, BlockGetter level, BlockPos pos, boolean neighbor);
	}
}
