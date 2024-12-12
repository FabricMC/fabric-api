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

package net.fabricmc.fabric.api.client.model.loading.v1;

import org.jetbrains.annotations.ApiStatus;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.model.GroupableModel;

/**
 * Block state resolvers are responsible for mapping each {@link BlockState} of a block to a {@link GroupableModel}.
 * They replace the {@code blockstates/} JSON files. One block can be mapped to only one block state resolver; multiple
 * resolvers will not receive the same block.
 *
 * <p>Block state resolvers can be used to create custom block state formats or dynamically resolve block state models.
 *
 * <p>Use {@link ModelModifier.OnLoad} instead of this interface if interacting with the block and block states directly
 * is not necessary. This includes custom model deserializers and loaders.
 *
 * @see ModelModifier.OnLoad
 * @see ModelModifier.OnLoadBlock
 */
@FunctionalInterface
public interface BlockStateResolver {
	/**
	 * Resolves the models for all block states of the block.
	 *
	 * <p>For each block state, call {@link Context#setModel} to set its unbaked model.
	 * This method must be called exactly once for each block state.
	 *
	 * <p>Note that if multiple block states share the same unbaked model instance, it will be baked multiple times
	 * (once per block state that has the model set).
	 */
	void resolveBlockStates(Context context);

	/**
	 * The context for block state resolution.
	 */
	@ApiStatus.NonExtendable
	interface Context {
		/**
		 * The block for which block state models are being resolved.
		 */
		Block block();

		/**
		 * Sets the model for a block state.
		 *
		 * @param state the block state for which this model should be used
		 * @param model the unbaked model for this block state
		 */
		void setModel(BlockState state, GroupableModel model);
	}
}
