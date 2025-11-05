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

package net.fabricmc.fabric.mixin.content.registry;

import com.llamalad7.mixinextras.sugar.Local;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.level.pathfinder.WalkNodeEvaluator;

import net.fabricmc.fabric.api.registry.LandPathNodeTypesRegistry;

// Applied a bit earlier than other mods to ensure changes and optimizations to default vanilla behavior
@Mixin(value = WalkNodeEvaluator.class, priority = 999)
public class WalkNodeEvaluatorMixin {
	/**
	 * Overrides the node type for the specified position, if the position is a direct target in a path.
	 */
	@Inject(method = "getPathTypeFromState", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;getBlock()Lnet/minecraft/world/level/block/Block;"), cancellable = true)
	private static void getCommonNodeType(BlockGetter world, BlockPos pos, CallbackInfoReturnable<PathType> cir, @Local BlockState state) {
		PathType nodeType = LandPathNodeTypesRegistry.getPathNodeType(state, world, pos, false);

		if (nodeType != null) {
			cir.setReturnValue(nodeType);
		}
	}
}
