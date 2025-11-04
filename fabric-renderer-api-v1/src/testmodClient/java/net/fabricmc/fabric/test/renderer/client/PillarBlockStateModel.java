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
import java.util.stream.Stream;

import com.mojang.serialization.MapCodec;
import org.jspecify.annotations.Nullable;

import net.minecraft.client.renderer.block.model.BlockModelPart;
import net.minecraft.client.renderer.block.model.BlockStateModel;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelDebugName;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;

import net.fabricmc.fabric.api.block.v1.FabricBlockState;
import net.fabricmc.fabric.api.client.model.loading.v1.CustomUnbakedBlockStateModel;
import net.fabricmc.fabric.api.renderer.v1.mesh.MutableQuadView;
import net.fabricmc.fabric.api.renderer.v1.mesh.QuadEmitter;
import net.fabricmc.fabric.test.renderer.Registration;
import net.fabricmc.fabric.test.renderer.RendererTest;

/**
 * Very crude implementation of a pillar block model that connects with pillars above and below.
 */
public class PillarBlockStateModel implements BlockStateModel {
	private enum ConnectedTexture {
		ALONE, BOTTOM, MIDDLE, TOP
	}

	// alone, bottom, middle, top
	private final TextureAtlasSprite[] sprites;

	public PillarBlockStateModel(TextureAtlasSprite[] sprites) {
		this.sprites = sprites;
	}

	@Override
	public void emitQuads(QuadEmitter emitter, BlockAndTintGetter blockView, BlockPos pos, BlockState state, RandomSource random, Predicate<@Nullable Direction> cullTest) {
		for (Direction side : Direction.values()) {
			ConnectedTexture texture = getConnectedTexture(blockView, pos, state, side);
			emitter.square(side, 0, 0, 1, 1, 0);
			emitter.spriteBake(sprites[texture.ordinal()], MutableQuadView.BAKE_LOCK_UV);
			emitter.emit();
		}
	}

	@Override
	public Object createGeometryKey(BlockAndTintGetter blockView, BlockPos pos, BlockState state, RandomSource random) {
		record Key(ConnectedTexture north, ConnectedTexture south, ConnectedTexture west, ConnectedTexture east) {
		}

		return new Key(
				getConnectedTexture(blockView, pos, state, Direction.NORTH),
				getConnectedTexture(blockView, pos, state, Direction.SOUTH),
				getConnectedTexture(blockView, pos, state, Direction.WEST),
				getConnectedTexture(blockView, pos, state, Direction.EAST)
		);
	}

	private static ConnectedTexture getConnectedTexture(BlockAndTintGetter blockView, BlockPos pos, BlockState state, Direction side) {
		if (side.getAxis().isHorizontal()) {
			boolean connectAbove = canConnect(blockView, state, pos, pos.above(), side);
			boolean connectBelow = canConnect(blockView, state, pos, pos.below(), side);

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

	private static boolean canConnect(BlockAndTintGetter blockView, BlockState originState, BlockPos originPos, BlockPos otherPos, Direction side) {
		BlockState otherState = blockView.getBlockState(otherPos);
		// In this testmod we can't rely on injected interfaces - in normal mods the (FabricBlockState) cast will be unnecessary
		BlockState originAppearance = ((FabricBlockState) originState).getAppearance(blockView, originPos, side, otherState, otherPos);

		if (!originAppearance.is(Registration.PILLAR_BLOCK)) {
			return false;
		}

		BlockState otherAppearance = ((FabricBlockState) otherState).getAppearance(blockView, otherPos, side, originState, originPos);

		if (!otherAppearance.is(Registration.PILLAR_BLOCK)) {
			return false;
		}

		return true;
	}

	@Override
	public void collectParts(RandomSource random, List<BlockModelPart> parts) {
	}

	@Override
	public TextureAtlasSprite particleIcon() {
		return sprites[0];
	}

	public record Unbaked() implements CustomUnbakedBlockStateModel, ModelDebugName {
		private static final List<Material> SPRITES = Stream.of("alone", "bottom", "middle", "top")
				.map(suffix -> new Material(TextureAtlas.LOCATION_BLOCKS, RendererTest.id("block/pillar_" + suffix)))
				.toList();
		public static final Unbaked INSTANCE = new Unbaked();
		public static final MapCodec<Unbaked> CODEC = MapCodec.unit(INSTANCE);

		@Override
		public MapCodec<? extends CustomUnbakedBlockStateModel> codec() {
			return CODEC;
		}

		@Override
		public void resolveDependencies(Resolver resolver) {
		}

		@Override
		public BlockStateModel bake(ModelBaker baker) {
			TextureAtlasSprite[] sprites = new TextureAtlasSprite[SPRITES.size()];

			for (int i = 0; i < sprites.length; ++i) {
				sprites[i] = baker.sprites().get(SPRITES.get(i), this);
			}

			return new PillarBlockStateModel(sprites);
		}

		@Override
		public String debugName() {
			return getClass().getName();
		}
	}
}
