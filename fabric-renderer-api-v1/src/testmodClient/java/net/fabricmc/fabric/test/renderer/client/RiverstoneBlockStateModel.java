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

package net.fabricmc.fabric.test.renderer.client;

import java.util.List;
import java.util.function.Predicate;

import org.jetbrains.annotations.Nullable;

import net.minecraft.block.BlockState;
import net.minecraft.client.render.model.BlockModelPart;
import net.minecraft.client.render.model.BlockStateModel;
import net.minecraft.client.texture.Sprite;
import net.minecraft.registry.tag.BiomeTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.BlockRenderView;

import net.fabricmc.fabric.api.blockview.v2.FabricBlockView;
import net.fabricmc.fabric.api.renderer.v1.mesh.QuadEmitter;

public class RiverstoneBlockStateModel implements BlockStateModel {
	private final BlockStateModel regularModel;
	private final BlockStateModel riverModel;

	public RiverstoneBlockStateModel(BlockStateModel regularModel, BlockStateModel riverModel) {
		this.regularModel = regularModel;
		this.riverModel = riverModel;
	}

	@Override
	public void emitQuads(QuadEmitter emitter, BlockRenderView blockView, BlockPos pos, BlockState state, Random random, Predicate<@Nullable Direction> cullTest) {
		if (((FabricBlockView) blockView).hasBiomes() && ((FabricBlockView) blockView).getBiomeFabric(pos).isIn(BiomeTags.IS_RIVER)) {
			riverModel.emitQuads(emitter, blockView, pos, state, random, cullTest);
		} else {
			regularModel.emitQuads(emitter, blockView, pos, state, random, cullTest);
		}
	}

	@Override
	@Nullable
	public Object createGeometryKey(BlockRenderView blockView, BlockPos pos, BlockState state, Random random) {
		if (((FabricBlockView) blockView).hasBiomes() && ((FabricBlockView) blockView).getBiomeFabric(pos).isIn(BiomeTags.IS_RIVER)) {
			return riverModel.createGeometryKey(blockView, pos, state, random);
		} else {
			return regularModel.createGeometryKey(blockView, pos, state, random);
		}
	}

	@Override
	public void addParts(Random random, List<BlockModelPart> parts) {
	}

	@Override
	public Sprite particleSprite() {
		return regularModel.particleSprite();
	}

	@Override
	public Sprite particleSprite(BlockRenderView blockView, BlockPos pos, BlockState state) {
		if (((FabricBlockView) blockView).hasBiomes() && ((FabricBlockView) blockView).getBiomeFabric(pos).isIn(BiomeTags.IS_RIVER)) {
			return riverModel.particleSprite(blockView, pos, state);
		} else {
			return regularModel.particleSprite(blockView, pos, state);
		}
	}
}
