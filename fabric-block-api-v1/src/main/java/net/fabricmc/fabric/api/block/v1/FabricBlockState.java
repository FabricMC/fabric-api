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
import net.minecraft.world.level.block.state.BlockState;

/**
 * General-purpose Fabric-provided extensions for {@link BlockState}, matching the functionality provided in {@link FabricBlock}.
 *
 * <p>Note: This interface is automatically implemented on all block states via Mixin and interface injection.
 */
public interface FabricBlockState {
	/**
	 * Return the current appearance of the block, i.e. which block state this block reports to look like on a given side.
	 *
	 * @param renderView  the world this block is in
	 * @param pos         position of this block, whose appearance is being queried
	 * @param side        the side for which the appearance is being queried
	 * @param sourceState (optional) state of the block that is querying the appearance, or null if unknown
	 * @param sourcePos   (optional) position of the block that is querying the appearance, or null if unknown
	 * @return the appearance of the block on the given side; the original {@code state} can be returned if there is no better option
	 * @see FabricBlock#getAppearance
	 */
	default BlockState getAppearance(BlockAndTintGetter renderView, BlockPos pos, Direction side, @Nullable BlockState sourceState, @Nullable BlockPos sourcePos) {
		BlockState self = (BlockState) this;
		return self.getBlock().getAppearance(self, renderView, pos, side, sourceState, sourcePos);
	}
}
