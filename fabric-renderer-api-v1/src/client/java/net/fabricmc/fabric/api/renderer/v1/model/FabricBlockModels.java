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

package net.fabricmc.fabric.api.renderer.v1.model;

import net.minecraft.client.renderer.block.BlockModelShaper;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.EmptyBlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Note: This interface is automatically implemented on {@link BlockModelShaper} via Mixin and interface injection.
 */
public interface FabricBlockModels {
	/**
	 * Alternative for {@link BlockModelShaper#getParticleIcon(BlockState)} that additionally accepts a
	 * {@link BlockAndTintGetter} and {@link BlockPos} to invoke
	 * {@link FabricBlockStateModel#particleSprite(BlockAndTintGetter, BlockPos, BlockState)}. <b>Prefer using this method
	 * over the vanilla alternative when applicable to correctly retrieve context-aware particle sprites.</b> If world
	 * context is not available, use the vanilla method instead of passing empty world context to this method.
	 *
	 * @param state The block state whose model to retrieve the particle sprite from.
	 * @param blockView The world in which the block exists. <b>Should not be empty (i.e. not
	 * 	                {@link EmptyBlockAndTintGetter}).</b>
	 * @param pos The position of the block in the world.
	 * @return the particle sprite
	 */
	default TextureAtlasSprite getModelParticleSprite(BlockState state, BlockAndTintGetter blockView, BlockPos pos) {
		return ((BlockModelShaper) this).getBlockModel(state).particleSprite(blockView, pos, state);
	}
}
