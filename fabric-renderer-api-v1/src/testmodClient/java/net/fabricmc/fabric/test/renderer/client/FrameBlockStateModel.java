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

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import org.jspecify.annotations.Nullable;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import net.fabricmc.fabric.api.blockgetter.v2.FabricBlockGetter;
import net.fabricmc.fabric.api.client.model.loading.v1.CustomUnbakedBlockStateModel;
import net.fabricmc.fabric.api.client.renderer.v1.mesh.QuadEmitter;

// TODO: the inner model will not be tinted. we should add a better solution than applying the tint
//  manually in the model.
public class FrameBlockStateModel implements BlockStateModel {
	private final BlockStateModel frameModel;

	public FrameBlockStateModel(BlockStateModel frameModel) {
		this.frameModel = frameModel;
	}

	@Override
	public void emitQuads(QuadEmitter emitter, BlockAndTintGetter level, BlockPos pos, BlockState state, RandomSource random, Predicate<@Nullable Direction> cullTest) {
		// Emit our frame model
		frameModel.emitQuads(emitter,
				level, pos, state, random, cullTest);

		// We should not access the block entity from here. We should instead use the immutable render data provided by the block entity.
		if (!(((FabricBlockGetter) level).getBlockEntityRenderData(pos) instanceof Block mimickedBlock)) {
			return; // No inner block to render, or data of wrong type
		}

		BlockState innerState = mimickedBlock.defaultBlockState();
		BlockStateModel innerModel = Minecraft.getInstance().getModelManager().getBlockStateModelSet().get(innerState);

		// Now, we emit a transparent scaled-down version of the inner model

		// Let's push a transform to scale the model down, make it transparent, and optionally make it emissive
		boolean emissive = pos.getX() % 2 != 0;
		emitter.pushTransform(quad -> {
			// Scale model down
			for (int vertex = 0; vertex < 4; ++vertex) {
				float x = quad.x(vertex) * 0.8f + 0.1f;
				float y = quad.y(vertex) * 0.8f + 0.1f;
				float z = quad.z(vertex) * 0.8f + 0.1f;
				quad.pos(vertex, x, y, z);
			}

			// Make the quad partially transparent
			quad.chunkLayer(ChunkSectionLayer.TRANSLUCENT);

			// Make the quad emissive, if requested
			if (emissive) {
				quad.emissive(true);
			}

			// Change vertex colors to be partially transparent
			for (int vertex = 0; vertex < 4; ++vertex) {
				int color = quad.color(vertex);
				int alpha = (color >> 24) & 0xFF;
				alpha = alpha * 3 / 4;
				color = (color & 0xFFFFFF) | (alpha << 24);
				quad.color(vertex, color);
			}

			// Return true because we want the quad to be rendered
			return true;
		});
		// Emit the inner block model
		innerModel.emitQuads(emitter,
				level, pos, state, random, cullTest);
		// Let's not forget to pop the transform!
		emitter.popTransform();
	}

	@Override
	@Nullable
	public Object createGeometryKey(BlockAndTintGetter level, BlockPos pos, BlockState state, RandomSource random) {
		// We should not access the block entity from here. We should instead use the immutable render data provided by the block entity.
		if (!(((FabricBlockGetter) level).getBlockEntityRenderData(pos) instanceof Block mimickedBlock)) {
			return this; // No inner block to render, or data of wrong type
		}

		BlockState innerState = mimickedBlock.defaultBlockState();
		BlockStateModel innerModel = Minecraft.getInstance().getModelManager().getBlockStateModelSet().get(innerState);
		Object subkey = innerModel.createGeometryKey(level, pos, state, random);

		if (subkey == null) {
			return null;
		}

		record Key(Object subkey, boolean notEmissive) {
		}

		return new Key(subkey, pos.getX() % 2 == 0);
	}

	@Override
	public Material.Baked particleMaterial(BlockAndTintGetter level, BlockPos pos, BlockState state) {
		// We should not access the block entity from here. We should instead use the immutable render data provided by the block entity.
		if (!(((FabricBlockGetter) level).getBlockEntityRenderData(pos) instanceof Block mimickedBlock)) {
			return frameModel.particleMaterial(level, pos, state); // No inner block to render, or data of wrong type
		}

		BlockState innerState = mimickedBlock.defaultBlockState();
		BlockStateModel innerModel = Minecraft.getInstance().getModelManager().getBlockStateModelSet().get(innerState);
		return innerModel.particleMaterial(level, pos, state);
	}

	@Override
	@BakedQuad.MaterialFlags
	public int materialFlags(BlockAndTintGetter level, BlockPos pos, BlockState state, RandomSource random) {
		@BakedQuad.MaterialFlags int flags = frameModel.materialFlags(level, pos, state, random);

		// We should not access the block entity from here. We should instead use the immutable render data provided by the block entity.
		if (!(((FabricBlockGetter) level).getBlockEntityRenderData(pos) instanceof Block mimickedBlock)) {
			return flags; // No inner block to render, or data of wrong type
		}

		BlockState innerState = mimickedBlock.defaultBlockState();
		BlockStateModel innerModel = Minecraft.getInstance().getModelManager().getBlockStateModelSet().get(innerState);
		flags |= BakedQuad.FLAG_TRANSLUCENT;
		flags |= innerModel.materialFlags(level, pos, state, random);
		return flags;
	}

	@Override
	public void collectParts(RandomSource random, List<BlockStateModelPart> parts) {
		// Renderer API makes this obsolete, so don't add any parts
	}

	@Override
	public Material.Baked particleMaterial() {
		return frameModel.particleMaterial();
	}

	@Override
	public @BakedQuad.MaterialFlags int materialFlags() {
		// This model can render any submodel, which may be translucent and animated, so this model
		// must report that it is also translucent and animated in the general case.
		return BakedQuad.FLAG_ANIMATED | BakedQuad.FLAG_TRANSLUCENT;
	}

	public record Unbaked(BlockStateModel.Unbaked frameModel) implements CustomUnbakedBlockStateModel {
		public static final MapCodec<Unbaked> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
				BlockStateModel.Unbaked.CODEC.fieldOf("frame_model").forGetter(Unbaked::frameModel)
		).apply(instance, Unbaked::new));

		@Override
		public MapCodec<? extends CustomUnbakedBlockStateModel> codec() {
			return CODEC;
		}

		@Override
		public void resolveDependencies(Resolver resolver) {
			frameModel.resolveDependencies(resolver);
		}

		@Override
		public BlockStateModel bake(ModelBaker baker) {
			BlockStateModel bakedFrameModel = frameModel.bake(baker);
			return new FrameBlockStateModel(bakedFrameModel);
		}
	}
}
