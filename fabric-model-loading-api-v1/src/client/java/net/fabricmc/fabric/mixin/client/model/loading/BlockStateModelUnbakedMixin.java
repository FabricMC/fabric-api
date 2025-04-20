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

package net.fabricmc.fabric.mixin.client.model.loading;

import java.util.List;
import java.util.function.Function;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import net.minecraft.client.render.model.BlockStateModel;
import net.minecraft.client.render.model.SimpleBlockStateModel;
import net.minecraft.client.render.model.WeightedBlockStateModel;
import net.minecraft.client.render.model.json.ModelVariant;
import net.minecraft.util.collection.Weighted;

import net.fabricmc.fabric.impl.client.model.loading.CustomUnbakedBlockStateModelRegistry;

@Mixin(BlockStateModel.Unbaked.class)
interface BlockStateModelUnbakedMixin {
	@Redirect(method = "<clinit>()V", at = @At(value = "INVOKE", target = "com/mojang/serialization/Codec.flatComapMap(Ljava/util/function/Function;Ljava/util/function/Function;)Lcom/mojang/serialization/Codec;", remap = false, ordinal = 0))
	private static Codec<WeightedBlockStateModel.Unbaked> replaceWeightedCodec(Codec<List<Weighted<ModelVariant>>> codec, Function<?, ?> to, Function<?, ?> from) {
		return CustomUnbakedBlockStateModelRegistry.WEIGHTED_MODEL_CODEC;
	}

	@Redirect(method = "<clinit>()V", at = @At(value = "INVOKE", target = "com/mojang/serialization/Codec.flatComapMap(Ljava/util/function/Function;Ljava/util/function/Function;)Lcom/mojang/serialization/Codec;", remap = false, ordinal = 1))
	private static Codec<BlockStateModel.Unbaked> replaceCodec(Codec<Either<WeightedBlockStateModel.Unbaked, SimpleBlockStateModel.Unbaked>> codec, Function<?, ?> to, Function<?, ?> from) {
		return CustomUnbakedBlockStateModelRegistry.MODEL_CODEC;
	}
}
