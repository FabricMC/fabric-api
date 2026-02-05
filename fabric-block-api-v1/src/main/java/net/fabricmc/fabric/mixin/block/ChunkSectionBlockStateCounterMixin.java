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

package net.fabricmc.fabric.mixin.block;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

@Mixin(targets = "net.minecraft.world.level.chunk.LevelChunkSection$1BlockCounter")
public class ChunkSectionBlockStateCounterMixin {
	/**
	 * Makes Chunk Sections not have isAir = true modded blocks be replaced with AIR against their will.
	 * Mojang report: https://bugs.mojang.com/browse/MC-232360
	 */
	@Redirect(method = "accept(Lnet/minecraft/world/level/block/state/BlockState;I)V",
			at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;isAir()Z"))
	private boolean modifyAirCheck(BlockState blockState) {
		return blockState.is(Blocks.AIR) || blockState.is(Blocks.CAVE_AIR) || blockState.is(Blocks.VOID_AIR);
	}
}
