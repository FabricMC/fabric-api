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

package net.fabricmc.fabric.api.client.model.loading.v1.wrapper;

import java.util.List;
import java.util.function.Predicate;

import org.jspecify.annotations.Nullable;

import net.minecraft.client.renderer.block.model.BlockModelPart;
import net.minecraft.client.renderer.block.model.BlockStateModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;

import net.fabricmc.fabric.api.renderer.v1.mesh.QuadEmitter;

/**
 * A simple implementation of {@link BlockStateModel} that delegates all method calls to the {@link #wrapped} field.
 * Implementations must set the {@link #wrapped} field somehow.
 */
public abstract class WrapperBlockStateModel implements BlockStateModel {
	protected BlockStateModel wrapped;

	protected WrapperBlockStateModel() {
	}

	protected WrapperBlockStateModel(BlockStateModel wrapped) {
		this.wrapped = wrapped;
	}

	@Override
	public void collectParts(RandomSource random, List<BlockModelPart> parts) {
		wrapped.collectParts(random, parts);
	}

	@Override
	public List<BlockModelPart> collectParts(RandomSource random) {
		return wrapped.collectParts(random);
	}

	@Override
	public TextureAtlasSprite particleIcon() {
		return wrapped.particleIcon();
	}

	@Override
	public void emitQuads(QuadEmitter emitter, BlockAndTintGetter blockView, BlockPos pos, BlockState state, RandomSource random, Predicate<@Nullable Direction> cullTest) {
		wrapped.emitQuads(emitter, blockView, pos, state, random, cullTest);
	}

	@Override
	@Nullable
	public Object createGeometryKey(BlockAndTintGetter blockView, BlockPos pos, BlockState state, RandomSource random) {
		return wrapped.createGeometryKey(blockView, pos, state, random);
	}

	@Override
	public TextureAtlasSprite particleSprite(BlockAndTintGetter blockView, BlockPos pos, BlockState state) {
		return wrapped.particleSprite(blockView, pos, state);
	}
}
