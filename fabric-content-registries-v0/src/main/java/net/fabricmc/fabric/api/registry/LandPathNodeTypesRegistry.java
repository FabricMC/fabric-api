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

/**
 * A registry to associate block states with specific path node types.
 * Specifying a node type for a block will change the way an entity recognizes the block when trying to pathfind.
 * You can make a safe block dangerous and vice-versa.
 * This works only for entities that move on air and land.
 * Duplicated registrations for the same block will replace the previous registration entry.
 */
public final class LandPathNodeTypesRegistry {
	private static final Logger LOGGER = LoggerFactory.getLogger(LandPathNodeTypesRegistry.class);
	private static final Map<Block, PathNodeTypeProvider> NODE_TYPES = new IdentityHashMap<>();

	private LandPathNodeTypesRegistry() {
	}

	/**
	 * Registers a {@link PathType} for the specified block, overriding the default block behavior.
	 *
	 * @param block              Block to register.
	 * @param nodeType           {@link PathType} to associate with the block if it is a direct target
	 *                           in an entity path.
	 *                           (Pass {@code null} to not specify a node type and use the default behavior)
	 * @param nodeTypeIfNeighbor {@link PathType} to associate with the block, if it is in a direct neighbor
	 *                           position to an entity path that is directly next to a block
	 *                           that the entity will pass through or above.
	 *                           (Pass {@code null} to not specify a node type and use the default behavior)
	 */
	public static void register(Block block, @Nullable PathType nodeType, @Nullable PathType nodeTypeIfNeighbor) {
		Objects.requireNonNull(block, "Block cannot be null!");

		// Registers a provider that always returns the specified node type.
		register(block, (state, neighbor) -> neighbor ? nodeTypeIfNeighbor : nodeType);
	}

	/**
	 * Registers a {@link StaticPathNodeTypeProvider} for the specified block overriding the default block behavior.
	 *
	 * <p>A static provider provides the node type basing on the block state.
	 *
	 * @param block    Block to register.
	 * @param provider {@link StaticPathNodeTypeProvider} to associate with the block.
	 */
	public static void register(Block block, StaticPathNodeTypeProvider provider) {
		Objects.requireNonNull(block, "Block cannot be null!");
		Objects.requireNonNull(provider, "StaticPathNodeTypeProvider cannot be null!");

		// Registers the provider.
		PathNodeTypeProvider old = NODE_TYPES.put(block, provider);

		if (old != null) {
			LOGGER.debug("Replaced PathNodeType provider for the block {}", block);
		}
	}

	/**
	 * Registers a {@link DynamicPathNodeTypeProvider} for the specified block, overriding the default block behavior.
	 *
	 * <p>A dynamic provider provides the node type basing on the block state, world and position.
	 * This is more difficult to handle, must be used only if you want to change the node type basing on the position
	 * of the block in the world, and may degrade the game performances because cannot be optimized but must be
	 * recalculated at every tick for every entity.
	 *
	 * @param block    Block to register.
	 * @param provider {@link DynamicPathNodeTypeProvider} to associate with the block.
	 */
	public static void registerDynamic(Block block, DynamicPathNodeTypeProvider provider) {
		Objects.requireNonNull(block, "Block cannot be null!");
		Objects.requireNonNull(provider, "DynamicPathNodeTypeProvider cannot be null!");

		// Registers the provider.
		PathNodeTypeProvider old = NODE_TYPES.put(block, provider);

		if (old != null) {
			LOGGER.debug("Replaced PathNodeType provider for the block {}", block);
		}
	}

	/**
	 * Gets the {@link PathType} from the provider registered for the specified block state at the specified position.
	 *
	 * <p>If no valid {@link PathType} provider is registered for the block, it returns {@code null}.
	 * You cannot use this method to retrieve vanilla block node types.
	 *
	 * @param state    Current block state.
	 * @param world    Current world.
	 * @param pos      Current position.
	 * @param neighbor Specifies if the block is not a directly targeted block, but a neighbor block in the path.
	 * @return the custom {@link PathType} from the provider registered for the specified block,
	 * passing the block state, the world, and the position to the provider, or {@code null} if no valid
	 * provider is registered for the block.
	 */
	@Nullable
	public static PathType getPathNodeType(BlockState state, BlockGetter world, BlockPos pos, boolean neighbor) {
		Objects.requireNonNull(state, "BlockState cannot be null!");
		Objects.requireNonNull(world, "BlockView cannot be null!");
		Objects.requireNonNull(pos, "BlockPos cannot be null!");

		// Gets the node type provider for the block.
		PathNodeTypeProvider provider = getPathNodeTypeProvider(state.getBlock());

		//If no provider exists, returns null.
		if (provider == null) return null;

		//If a provider exists, returns the node type obtained from the provider.
		//The node type can be null too.
		if (provider instanceof DynamicPathNodeTypeProvider) {
			return ((DynamicPathNodeTypeProvider) provider).getPathNodeType(state, world, pos, neighbor);
		} else {
			return ((StaticPathNodeTypeProvider) provider).getPathNodeType(state, neighbor);
		}
	}

	/**
	 * Gets the raw {@link PathNodeTypeProvider} registered for the specified block.
	 *
	 * <p>If no {@link PathNodeTypeProvider} is registered for the block, it returns {@code null}.
	 *
	 * <p>Note 1: {@link PathNodeTypeProvider} is a marker interface with no methods,
	 * so you need to cast the result to a subtype, in order to get something from it.
	 * Currently, if non-null, the result can be of {@link StaticPathNodeTypeProvider}
	 * or {@link DynamicPathNodeTypeProvider}.
	 * Note that more kinds of providers might be added if the API is expanded in the future,
	 * so make sure not to fail if another type of object is returned.
	 *
	 * <p>Note 2: This method is intended to be used in any cases in which you need to get
	 * the raw provider for the block, if you need the {@link PathType} for the block state instead,
	 * you can simply use {@link #getPathNodeType}.
	 *
	 * @param block Current block.
	 * @return the {@link PathNodeTypeProvider} registered for the specified block,
	 * or {@code null} if no provider is registered for the block.
	 */
	@Nullable
	public static PathNodeTypeProvider getPathNodeTypeProvider(Block block) {
		Objects.requireNonNull(block, "Block cannot be null!");

		return NODE_TYPES.get(block);
	}

	/**
	 * Generic provider, this is a marker interface.
	 */
	public sealed interface PathNodeTypeProvider permits StaticPathNodeTypeProvider, DynamicPathNodeTypeProvider {
	}

	/**
	 * A functional interface that provides the {@link PathType}, given the block state.
	 */
	@FunctionalInterface
	public non-sealed interface StaticPathNodeTypeProvider extends PathNodeTypeProvider {
		/**
		 * Gets the {@link PathType} for the specified block state.
		 *
		 * <p>You can specify what to return if the block state is a direct target of an entity path,
		 * or a neighbor block of the entity path.
		 *
		 * <p>For example, for a cactus-like block you should use {@link PathType#DAMAGE_OTHER} if the block
		 * is a direct target in the entity path ({@code neighbor == false}) to specify that an entity should not pass
		 * through or above the block because it will cause damage, and you should use {@link PathType#DANGER_OTHER}
		 * if the block is a neighbor block in the entity path ({@code neighbor == true}) to specify that the entity
		 * should not get close to the block because it is dangerous.
		 *
		 * @param state    Current block state.
		 * @param neighbor Specifies that the block is in a direct neighbor position to an entity path
		 *                 that is directly next to a block that the entity will pass through or above.
		 * @return the custom {@link PathType} registered for the specified block state.
		 */
		@Nullable
		PathType getPathNodeType(BlockState state, boolean neighbor);
	}

	/**
	 * A functional interface that provides the {@link PathType}, given the block state world and position.
	 */
	@FunctionalInterface
	public non-sealed interface DynamicPathNodeTypeProvider extends PathNodeTypeProvider {
		/**
		 * Gets the {@link PathType} for the specified block state at the specified position.
		 *
		 * <p>You can specify what to return if the block state is a direct target of an entity path,
		 * or a neighbor block of the entity path.
		 *
		 * <p>For example, for a cactus-like block you should specify {@link PathType#DAMAGE_OTHER} if the block
		 * is a direct target ({@code neighbor == false}) to specify that an entity should not pass through or above
		 * the block because it will cause damage, and {@link PathType#DANGER_OTHER} if the cactus will be found
		 * as a neighbor block in the entity path ({@code neighbor == true}) to specify that the entity should not get
		 * close to the block because is dangerous.
		 *
		 * @param state    Current block state.
		 * @param world    Current world.
		 * @param pos      Current position.
		 * @param neighbor Specifies that the block is in a direct neighbor position to an entity path
		 *                 (directly next to a block that the entity will pass through or above).
		 * @return the custom {@link PathType} registered for the specified block state at the specified position.
		 */
		@Nullable
		PathType getPathNodeType(BlockState state, BlockGetter world, BlockPos pos, boolean neighbor);
	}
}
