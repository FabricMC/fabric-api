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

package net.fabricmc.fabric.mixin.renderer.client;

import java.util.List;
import java.util.function.Predicate;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import net.minecraft.block.BlockState;
import net.minecraft.class_10895;
import net.minecraft.client.render.model.BlockStateModel;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.BlockRenderView;

import net.fabricmc.fabric.api.renderer.v1.mesh.QuadEmitter;

@Mixin(class_10895.class)
abstract class MultipartBlockStateModelMixin implements BlockStateModel {
	@Shadow
	@Final
	private class_10895.MultipartBakedModel field_57948;

	@Shadow
	@Final
	private BlockState field_57949;

	@Shadow
	@Nullable
	private List<BlockStateModel> field_57950;

	@Unique
	private boolean isVanillaComputed = false;

	@Unique
	private boolean isVanilla = true;

	@Override
	public boolean isVanillaAdapter() {
		if (!isVanillaComputed) {
			if (field_57950 == null) {
				field_57950 = field_57948.method_68528(field_57949);
			}

			for (BlockStateModel model : field_57950) {
				if (!model.isVanillaAdapter()) {
					isVanilla = false;
					break;
				}
			}

			isVanillaComputed = true;
		}

		return isVanilla;
	}

	@Override
	public void emitQuads(QuadEmitter emitter, BlockRenderView blockView, BlockPos pos, BlockState state, Random random, Predicate<@Nullable Direction> cullTest) {
		if (field_57950 == null) {
			field_57950 = field_57948.method_68528(field_57949);
		}

		long seed = random.nextLong();

		for (BlockStateModel model : field_57950) {
			random.setSeed(seed);
			model.emitQuads(emitter, blockView, pos, state, random, cullTest);
		}
	}
}
