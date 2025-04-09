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
import org.jetbrains.annotations.Nullable;

import net.minecraft.block.BlockState;
import net.minecraft.client.render.model.Baker;
import net.minecraft.client.render.model.BlockModelPart;
import net.minecraft.client.render.model.BlockStateModel;
import net.minecraft.client.texture.Sprite;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.BlockRenderView;
import net.minecraft.world.biome.Biome;

import net.fabricmc.fabric.api.blockview.v2.FabricBlockView;
import net.fabricmc.fabric.api.client.model.loading.v1.CustomUnbakedBlockStateModel;
import net.fabricmc.fabric.api.renderer.v1.mesh.QuadEmitter;

public class BiomeDependentBlockStateModel implements BlockStateModel {
	private final BlockStateModel regularModel;
	private final BlockStateModel biomeModel;
	private final TagKey<Biome> biomeTag;

	public BiomeDependentBlockStateModel(BlockStateModel regularModel, BlockStateModel biomeModel, TagKey<Biome> biomeTag) {
		this.regularModel = regularModel;
		this.biomeModel = biomeModel;
		this.biomeTag = biomeTag;
	}

	@Override
	public void emitQuads(QuadEmitter emitter, BlockRenderView blockView, BlockPos pos, BlockState state, Random random, Predicate<@Nullable Direction> cullTest) {
		if (((FabricBlockView) blockView).hasBiomes() && ((FabricBlockView) blockView).getBiomeFabric(pos).isIn(biomeTag)) {
			biomeModel.emitQuads(emitter, blockView, pos, state, random, cullTest);
		} else {
			regularModel.emitQuads(emitter, blockView, pos, state, random, cullTest);
		}
	}

	@Override
	@Nullable
	public Object createGeometryKey(BlockRenderView blockView, BlockPos pos, BlockState state, Random random) {
		if (((FabricBlockView) blockView).hasBiomes() && ((FabricBlockView) blockView).getBiomeFabric(pos).isIn(biomeTag)) {
			return biomeModel.createGeometryKey(blockView, pos, state, random);
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
		if (((FabricBlockView) blockView).hasBiomes() && ((FabricBlockView) blockView).getBiomeFabric(pos).isIn(biomeTag)) {
			return biomeModel.particleSprite(blockView, pos, state);
		} else {
			return regularModel.particleSprite(blockView, pos, state);
		}
	}

	public record Unbaked(BlockStateModel.Unbaked regularModel, BlockStateModel.Unbaked biomeModel, TagKey<Biome> biomeTag) implements CustomUnbakedBlockStateModel {
		public static final MapCodec<Unbaked> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
				BlockStateModel.Unbaked.CODEC.fieldOf("regular_model").forGetter(Unbaked::regularModel),
				BlockStateModel.Unbaked.CODEC.fieldOf("biome_model").forGetter(Unbaked::biomeModel),
				TagKey.unprefixedCodec(RegistryKeys.BIOME).fieldOf("biome_tag").forGetter(Unbaked::biomeTag)
		).apply(instance, Unbaked::new));

		@Override
		public MapCodec<? extends CustomUnbakedBlockStateModel> codec() {
			return CODEC;
		}

		@Override
		public void resolve(Resolver resolver) {
			regularModel.resolve(resolver);
			biomeModel.resolve(resolver);
		}

		@Override
		public BlockStateModel bake(Baker baker) {
			BlockStateModel bakedRegularModel = regularModel.bake(baker);
			BlockStateModel bakedBiomeModel = biomeModel.bake(baker);
			return new BiomeDependentBlockStateModel(bakedRegularModel, bakedBiomeModel, biomeTag);
		}
	}
}
