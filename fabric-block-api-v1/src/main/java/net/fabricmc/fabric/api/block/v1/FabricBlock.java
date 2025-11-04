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

package net.fabricmc.fabric.api.block.v1;

import org.jspecify.annotations.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

/**
 * General-purpose Fabric-provided extensions for {@link Block} subclasses.
 *
 * <p>Note: This interface is automatically implemented on all blocks via Mixin and interface injection.
 */
// Note to maintainers: Functions should only be added to this interface if they are general-purpose enough,
// to be evaluated on a case-by-case basis. Otherwise, they are better suited for more specialized APIs.
public interface FabricBlock {
	/**
	 * Return the current appearance of the block, i.e. which block state this block reports to look like on a given side.
	 *
	 * <p>Common implementors are covers and facades, or any other mimic blocks that proxy another block's model.
	 * These will want to override this method. In that case, make sure to carefully read the implementation guidelines below.
	 *
	 * <p>Common consumers are models with connected textures that wish to seamlessly connect to mimic blocks.
	 * These will want to check the apparent block state using {@link FabricBlockState#getAppearance}.
	 *
	 * <p>Generally, the appearance will be queried from a nearby block,
	 * identified by the optional {@code sourcePos} and {@code sourceState} parameters.
	 *
	 * <p>When a block changes appearance, it should trigger a chunk remesh for itself and the adjacent blocks,
	 * for example by calling {@link Level#sendBlockUpdated}.
	 *
	 * <p>Note: Overriding this method for a block does <strong>not</strong> change how it renders.
	 * It's up to modded models to check for the appearance of nearby blocks and adjust accordingly.
	 *
	 * <h3>Implementation guidelines</h3>
	 *
	 * <p>This can be called on the server, where block entity data can be safely accessed,
	 * and on the client, possibly in a meshing thread, where block entity data is not safe to access!
	 * Here is an example of how data from a block entity can be handled safely.
	 * The block entity should override {@code RenderDataBlockEntity#getBlockEntityRenderData} to return
	 * the necessary data. Refer to the documentation of {@code RenderDataBlockEntity} for more information.
	 * <pre>{@code @Override
	 * public BlockState getAppearance(BlockState state, BlockRenderView renderView, BlockPos pos, Direction side, @Nullable BlockState sourceState, @Nullable BlockPos sourcePos) {
	 *     if (renderView instanceof ServerWorld serverWorld) {
	 *         // Server side; ok to use block entity directly!
	 *         BlockEntity blockEntity = serverWorld.getBlockEntity(pos);
	 *
	 *         if (blockEntity instanceof ...) {
	 *             // Get data from block entity
	 *             return ...;
	 *         }
	 *     } else {
	 *         // Client side; need to use the block entity render data!
	 *         Object data = renderView.getBlockEntityRenderData(pos);
	 *
	 *         // Check if data is not null and of the correct type, and use that to determine the appearance
	 *         if (data instanceof ...) {
	 *             // get appearance for side ...
	 *             return ...;
	 *         }
	 *     }
	 *
	 *     // Example of varying the appearance based on the source pos
	 *     if (sourcePos != null) {
	 *         // get appearance for side ...
	 *         return ...;
	 *     }
	 *
	 *     // If there is no other appearance, just return the original block state
	 *     return state;
	 * });
	 * }</pre>
	 *
	 * @param state       state of this block, whose appearance is being queried
	 * @param renderView  the world this block is in
	 * @param pos         position of this block, whose appearance is being queried
	 * @param side        the side for which the appearance is being queried
	 * @param sourceState (optional) state of the block that is querying the appearance, or null if unknown
	 * @param sourcePos   (optional) position of the block that is querying the appearance, or null if unknown
	 * @return the appearance of the block on the given side; the original {@code state} can be returned if there is no better option
	 */
	default BlockState getAppearance(BlockState state, BlockAndTintGetter renderView, BlockPos pos, Direction side, @Nullable BlockState sourceState, @Nullable BlockPos sourcePos) {
		return state;
	}
}
