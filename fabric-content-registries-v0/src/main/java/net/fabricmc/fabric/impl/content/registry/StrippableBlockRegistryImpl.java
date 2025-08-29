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

package net.fabricmc.fabric.impl.content.registry;

import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Objects;

import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;

import net.fabricmc.fabric.api.registry.StrippableBlockRegistry;
import net.fabricmc.fabric.impl.content.registry.util.ImmutableCollectionUtils;
import net.fabricmc.fabric.mixin.content.registry.AxeItemAccessor;

public final class StrippableBlockRegistryImpl {
	private static final Logger LOGGER = LoggerFactory.getLogger(StrippableBlockRegistryImpl.class);
	private static final IdentityHashMap<Block, StrippableBlockRegistry.StrippingTransformer> TRANSFORMERS = new IdentityHashMap<>();

	public static void register(Block input, Block stripped, StrippableBlockRegistry.StrippingTransformer transformer) {
		Objects.requireNonNull(input, "input block cannot be null");
		Objects.requireNonNull(stripped, "stripped block cannot be null");

		Block old = getRegistry().put(input, stripped);
		TRANSFORMERS.put(input, transformer);

		if (old != null) {
			LOGGER.debug("Replaced old stripping mapping from {} to {} with {}", input, old, stripped);
		}
	}

	private static Map<Block, Block> getRegistry() {
		return ImmutableCollectionUtils.getAsMutableMap(AxeItemAccessor::getStrippedBlocks, AxeItemAccessor::setStrippedBlocks);
	}

	@Nullable
	public static BlockState getStrippedBlockState(BlockState state) {
		Block strippedBlock = getRegistry().get(state.getBlock());

		if (strippedBlock == null) {
			return null;
		}

		return TRANSFORMERS.getOrDefault(state.getBlock(), StrippableBlockRegistry.StrippingTransformer.VANILLA).getStrippedBlockState(strippedBlock, state);
	}

	@Nullable
	public static StrippableBlockRegistry.StrippingTransformer getTransformer(Block block) {
		return TRANSFORMERS.get(block);
	}
}
