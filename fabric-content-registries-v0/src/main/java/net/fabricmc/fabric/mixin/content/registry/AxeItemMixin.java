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

import java.util.function.Function;

import com.llamalad7.mixinextras.sugar.Local;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.item.AxeItem;

import net.fabricmc.fabric.api.registry.StrippableBlockRegistry;
import net.fabricmc.fabric.impl.content.registry.StrippableBlockRegistryImpl;

@Mixin(AxeItem.class)
public class AxeItemMixin {
	@ModifyArg(method = "getStrippedState", at = @At(value = "INVOKE", target = "Ljava/util/Optional;map(Ljava/util/function/Function;)Ljava/util/Optional;"))
	private Function<Block, BlockState> handleCustomStrippingBehavior(Function<Block, BlockState> mapper, @Local(argsOnly = true) BlockState state) {
		StrippableBlockRegistry.StrippingTransformer transformer = StrippableBlockRegistryImpl.getTransformer(state.getBlock());

		if (transformer != null) {
			return block -> transformer.getStrippedBlockState(block, state);
		}

		return mapper;
	}
}
