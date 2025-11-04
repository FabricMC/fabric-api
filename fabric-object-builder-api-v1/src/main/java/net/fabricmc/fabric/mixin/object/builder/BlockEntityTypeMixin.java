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

package net.fabricmc.fabric.mixin.object.builder;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;

import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityType;

@Mixin(BlockEntityType.class)
public class BlockEntityTypeMixin<T extends BlockEntity> implements FabricBlockEntityType {
	@Mutable
	@Shadow
	@Final
	private Set<Block> validBlocks;

	@Inject(method = "<init>", at = @At("RETURN"))
	private void mutableBlocks(BlockEntityType.BlockEntitySupplier<? extends T> factory, Set<Block> blocks, CallbackInfo ci) {
		if (!(this.validBlocks instanceof HashSet)) {
			this.validBlocks = new HashSet<>(this.validBlocks);
		}
	}

	@Override
	public void addSupportedBlock(Block block) {
		Objects.requireNonNull(block, "block");
		validBlocks.add(block);
	}
}
