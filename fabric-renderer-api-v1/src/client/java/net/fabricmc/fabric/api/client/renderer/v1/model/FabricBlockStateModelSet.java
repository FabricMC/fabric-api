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

package net.fabricmc.fabric.api.client.renderer.v1.model;

import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.BlockStateModelSet;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Note: This interface is automatically implemented on {@link BlockStateModelSet} via Mixin and interface injection.
 */
public interface FabricBlockStateModelSet {
	/**
	 * Alternative for {@link BlockStateModelSet#getParticleMaterial(BlockState)} that additionally accepts a
	 * {@link BlockAndTintGetter} and {@link BlockPos} to invoke
	 * {@link FabricBlockStateModel#particleMaterial(BlockAndTintGetter, BlockPos, BlockState)}. <b>Prefer using this method
	 * over the vanilla alternative when applicable to correctly retrieve context-aware particle materials.</b> If level
	 * context is not available, use the vanilla method instead of passing empty level context to this method.
	 *
	 * @param state The block state whose model to retrieve the particle material from.
	 * @param level The level in which the block exists. <b>Should not be empty (i.e. not
	 *                    {@link BlockAndTintGetter#EMPTY}).</b>
	 * @param pos The position of the block in the level.
	 * @return the particle material
	 */
	default Material.Baked getParticleMaterial(BlockState state, BlockAndTintGetter level, BlockPos pos) {
		return ((BlockStateModelSet) this).get(state).particleMaterial(
				level, pos, state);
	}
}
