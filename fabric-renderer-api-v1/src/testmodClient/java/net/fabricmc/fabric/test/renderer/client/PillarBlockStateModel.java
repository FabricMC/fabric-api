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

import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelDebugName;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;

import net.fabricmc.fabric.api.block.v1.FabricBlockState;
import net.fabricmc.fabric.api.client.model.loading.v1.CustomUnbakedBlockStateModel;
import net.fabricmc.fabric.api.client.renderer.v1.mesh.MutableQuadView;
import net.fabricmc.fabric.api.client.renderer.v1.mesh.QuadEmitter;
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
	private final Material.Baked[] materials;
	private final @BakedQuad.MaterialFlags int[] materialFlags;
	private final @BakedQuad.MaterialFlags int staticMaterialFlags;

	public PillarBlockStateModel(Material.Baked[] materials) {
		this.materials = materials;
		materialFlags = new int[materials.length];
		@BakedQuad.MaterialFlags int staticMaterialFlags = 0;

		for (int i = 0; i < materials.length; i++) {
			Material.Baked material = materials[i];
			int flags = 0;

			if (material.forceTranslucent() || material.sprite()
					.contents()
					.computeTransparency(0.0f, 0.0f, 1.0f, 1.0f)
					.hasTranslucent()) {
				flags |= BakedQuad.FLAG_TRANSLUCENT;
			}

			if (material.sprite().contents().isAnimated()) {
				flags |= BakedQuad.FLAG_ANIMATED;
			}

			materialFlags[i] = flags;
			staticMaterialFlags |= flags;
		}

		this.staticMaterialFlags = staticMaterialFlags;
	}

	@Override
	public void emitQuads(QuadEmitter emitter, BlockAndTintGetter level, BlockPos pos, BlockState state, RandomSource random, Predicate<@Nullable Direction> cullTest) {
		for (Direction side : Direction.values()) {
			ConnectedTexture texture = getConnectedTexture(level, pos, state, side);
			emitter.square(side, 0, 0, 1, 1, 0)
					.materialBake(materials[texture.ordinal()], MutableQuadView.BAKE_LOCK_UV)
					.emit();
		}
	}

	@Override
	public Object createGeometryKey(BlockAndTintGetter level, BlockPos pos, BlockState state, RandomSource random) {
		record Key(ConnectedTexture north, ConnectedTexture south, ConnectedTexture west, ConnectedTexture east) {
		}

		return new Key(
				getConnectedTexture(level, pos, state, Direction.NORTH),
				getConnectedTexture(level, pos, state, Direction.SOUTH),
				getConnectedTexture(level, pos, state, Direction.WEST),
				getConnectedTexture(level, pos, state, Direction.EAST)
		);
	}

	@Override
	@BakedQuad.MaterialFlags
	public int materialFlags(BlockAndTintGetter level, BlockPos pos, BlockState state, RandomSource random) {
		@BakedQuad.MaterialFlags int flags = materialFlags[ConnectedTexture.ALONE.ordinal()];
		flags |= materialFlags[getConnectedTexture(level, pos, state, Direction.NORTH).ordinal()];
		flags |= materialFlags[getConnectedTexture(level, pos, state, Direction.SOUTH).ordinal()];
		flags |= materialFlags[getConnectedTexture(level, pos, state, Direction.WEST).ordinal()];
		flags |= materialFlags[getConnectedTexture(level, pos, state, Direction.EAST).ordinal()];
		return flags;
	}

	private static ConnectedTexture getConnectedTexture(BlockAndTintGetter level, BlockPos pos, BlockState state, Direction side) {
		if (side.getAxis().isHorizontal()) {
			boolean connectAbove = canConnect(level, state, pos, pos.above(), side);
			boolean connectBelow = canConnect(level, state, pos, pos.below(), side);

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

	private static boolean canConnect(BlockAndTintGetter level, BlockState originState, BlockPos originPos, BlockPos otherPos, Direction side) {
		BlockState otherState = level.getBlockState(otherPos);
		// In this testmod we can't rely on injected interfaces - in normal mods the (FabricBlockState) cast will be unnecessary
		BlockState originAppearance = ((FabricBlockState) originState).getAppearance(
				level, originPos, side, otherState, otherPos);

		if (!originAppearance.is(Registration.PILLAR_BLOCK)) {
			return false;
		}

		BlockState otherAppearance = ((FabricBlockState) otherState).getAppearance(
				level, otherPos, side, originState, originPos);

		return otherAppearance.is(Registration.PILLAR_BLOCK);
	}

	@Override
	public void collectParts(RandomSource random, List<BlockStateModelPart> parts) {
	}

	@Override
	public Material.Baked particleMaterial() {
		return materials[0];
	}

	@Override
	public @BakedQuad.MaterialFlags int materialFlags() {
		return staticMaterialFlags;
	}

	public record Unbaked() implements CustomUnbakedBlockStateModel, ModelDebugName {
		private static final List<Material> MATERIALS = Stream.of("alone", "bottom", "middle", "top")
				.map(suffix -> new Material(RendererTest.id("block/pillar_" + suffix)))
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
			Material.Baked[] materials = new Material.Baked[MATERIALS.size()];

			for (int i = 0; i < materials.length; ++i) {
				Material.Baked material = baker.materials()
						.get(MATERIALS.get(i), this);
				materials[i] = material;
			}

			return new PillarBlockStateModel(materials);
		}

		@Override
		public String debugName() {
			return getClass().getName();
		}
	}
}
