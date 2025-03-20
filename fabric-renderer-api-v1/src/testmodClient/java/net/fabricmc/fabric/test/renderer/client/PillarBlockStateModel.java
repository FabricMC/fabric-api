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
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.BlockRenderView;

import net.fabricmc.fabric.api.block.v1.FabricBlockState;
import net.fabricmc.fabric.api.renderer.v1.mesh.MutableQuadView;
import net.fabricmc.fabric.api.renderer.v1.mesh.QuadEmitter;
import net.fabricmc.fabric.test.renderer.Registration;

/**
 * Very crude implementation of a pillar block model that connects with pillars above and below.
 */
public class PillarBlockStateModel implements BlockStateModel {
	private enum ConnectedTexture {
		ALONE, BOTTOM, MIDDLE, TOP
	}

	// alone, bottom, middle, top
	private final Sprite[] sprites;

	public PillarBlockStateModel(Sprite[] sprites) {
		this.sprites = sprites;
	}

	@Override
	public void emitQuads(QuadEmitter emitter, BlockRenderView blockView, BlockPos pos, BlockState state, Random random, Predicate<@Nullable Direction> cullTest) {
		for (Direction side : Direction.values()) {
			ConnectedTexture texture = getConnectedTexture(blockView, pos, state, side);
			emitter.square(side, 0, 0, 1, 1, 0);
			emitter.spriteBake(sprites[texture.ordinal()], MutableQuadView.BAKE_LOCK_UV);
			emitter.color(-1, -1, -1, -1);
			emitter.emit();
		}
	}

	@Override
	public Object createGeometryKey(BlockRenderView blockView, BlockPos pos, BlockState state, Random random) {
		record Key(ConnectedTexture north, ConnectedTexture south, ConnectedTexture west, ConnectedTexture east) {
		}

		return new Key(
				getConnectedTexture(blockView, pos, state, Direction.NORTH),
				getConnectedTexture(blockView, pos, state, Direction.SOUTH),
				getConnectedTexture(blockView, pos, state, Direction.WEST),
				getConnectedTexture(blockView, pos, state, Direction.EAST)
		);
	}

	private static ConnectedTexture getConnectedTexture(BlockRenderView blockView, BlockPos pos, BlockState state, Direction side) {
		if (side.getAxis().isHorizontal()) {
			boolean connectAbove = canConnect(blockView, state, pos, pos.up(), side);
			boolean connectBelow = canConnect(blockView, state, pos, pos.down(), side);

			if (connectAbove && connectBelow) {
				return ConnectedTexture.MIDDLE;
			} else if (connectAbove) {
				return ConnectedTexture.BOTTOM;
			} else if (connectBelow) {
				return ConnectedTexture.TOP;
			}
		}

		return ConnectedTexture.ALONE;
	}

	private static boolean canConnect(BlockRenderView blockView, BlockState originState, BlockPos originPos, BlockPos otherPos, Direction side) {
		BlockState otherState = blockView.getBlockState(otherPos);
		// In this testmod we can't rely on injected interfaces - in normal mods the (FabricBlockState) cast will be unnecessary
		BlockState originAppearance = ((FabricBlockState) originState).getAppearance(blockView, originPos, side, otherState, otherPos);

		if (!originAppearance.isOf(Registration.PILLAR_BLOCK)) {
			return false;
		}

		BlockState otherAppearance = ((FabricBlockState) otherState).getAppearance(blockView, otherPos, side, originState, originPos);

		if (!otherAppearance.isOf(Registration.PILLAR_BLOCK)) {
			return false;
		}

		return true;
	}

	@Override
	public void addParts(Random random, List<BlockModelPart> parts) {
	}

	@Override
	public Sprite particleSprite() {
		return sprites[0];
	}
}
