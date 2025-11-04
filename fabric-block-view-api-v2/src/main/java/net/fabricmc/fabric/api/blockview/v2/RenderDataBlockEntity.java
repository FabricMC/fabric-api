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

package net.fabricmc.fabric.api.blockview.v2;

import org.jspecify.annotations.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.entity.BlockEntity;

/**
 * Extensions that allow {@link BlockEntity} subclasses to provide render data.
 *
 * <p>Block entity render data is arbitrary data that captures some useful state of the
 * {@link BlockEntity} and is safe to use in a multithreaded environment. In these environments,
 * accessing and using a {@link BlockEntity} directly via {@link BlockGetter#getBlockEntity(BlockPos)}
 * may not be thread-safe since the {@link BlockEntity} may be modified on a different thread, and it
 * may not be consistent since accessing the internal state of the {@link BlockEntity} could modify it
 * in a non-atomic way (such as through lazy computation). Using render data avoids these issues.
 *
 * <h3>Implementation Tips</h3>
 *
 * <p>The simplest form of render data is a value or object that is immutable. If only one such value
 * must serve as render data, then it can be returned directly. An example of this would be returning
 * an {@code Integer} that represents some internal state of a block entity. If more than one value
 * must be used as render data, it can be packaged into an object that cannot be modified externally,
 * such as a record. It is also possible to make render data a mutable object, but it must be ensured
 * that changes to the internal state of this object are atomic and safe.
 *
 * <p>Note: This interface is automatically implemented on all {@link BlockEntity} instances via Mixin and interface injection.
 */
public interface RenderDataBlockEntity {
	/**
	 * Gets the render data provided by this block entity. The returned object must be safe to
	 * use in a multithreaded environment.
	 *
	 * <p>Note: <b>This method should not be called directly</b>; use
	 * {@link FabricBlockView#getBlockEntityRenderData(BlockPos)} instead. Only call this
	 * method when the result is used to implement
	 * {@link FabricBlockView#getBlockEntityRenderData(BlockPos)}.
	 *
	 * @return the render data
	 * @see FabricBlockView#getBlockEntityRenderData(BlockPos)
	 */
	@Nullable
	default Object getRenderData() {
		return null;
	}
}
