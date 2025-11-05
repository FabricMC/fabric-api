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
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.CollisionGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.level.pathfinder.PathfindingContext;

import net.fabricmc.fabric.api.registry.LandPathNodeTypesRegistry;

@Mixin(PathfindingContext.class)
public abstract class PathfindingContextMixin {
	@Shadow
	public abstract BlockState getBlockState(BlockPos blockPos);

	@Shadow
	public abstract CollisionGetter level();

	/**
	 * Overrides the node type for the specified position, if the position is found as neighbor block in a path.
	 */
	@Inject(method = "getPathTypeFromState", at = @At(value = "INVOKE_ASSIGN", target = "Lnet/minecraft/core/BlockPos$MutableBlockPos;set(III)Lnet/minecraft/core/BlockPos$MutableBlockPos;"), cancellable = true)
	private void onGetNodeType(int x, int y, int z, CallbackInfoReturnable<PathType> cir, @Local BlockPos pos) {
		final PathType neighborNodeType = LandPathNodeTypesRegistry.getPathNodeType(getBlockState(pos), level(), pos, true);

		if (neighborNodeType != null) {
			cir.setReturnValue(neighborNodeType);
		}
	}
}
