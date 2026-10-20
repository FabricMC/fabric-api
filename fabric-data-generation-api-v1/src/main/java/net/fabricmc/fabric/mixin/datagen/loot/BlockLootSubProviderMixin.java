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

package net.fabricmc.fabric.mixin.datagen.loot;

import java.util.Iterator;

import com.google.common.collect.Iterators;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.data.loot.packs.VanillaBlockLoot;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;

import net.fabricmc.fabric.api.datagen.v1.loot.FabricBlockLootSubProvider;

@Mixin(BlockLootSubProvider.class)
class BlockLootSubProviderMixin implements FabricBlockLootSubProvider {
	@ModifyExpressionValue(method = "run", at = @At(value = "INVOKE", target = "Lnet/minecraft/core/DefaultedRegistry;iterator()Ljava/util/Iterator;"))
	private Iterator<Block> onlyVanillaBlocks(Iterator<Block> blocks) {
		if ((Object) this instanceof VanillaBlockLoot) {
			return Iterators.filter(blocks, block -> BuiltInRegistries.BLOCK.getKey(block).getNamespace().equals(Identifier.DEFAULT_NAMESPACE));
		}

		return blocks;
	}
}
