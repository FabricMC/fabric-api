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

package net.fabricmc.fabric.api.registry;

import org.jetbrains.annotations.Nullable;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.state.property.Properties;
import net.minecraft.state.property.Property;
import net.minecraft.util.math.Direction;

import net.fabricmc.fabric.impl.content.registry.StrippableBlockRegistryImpl;

/**
 * A registry for axe stripping interactions. A vanilla example is turning logs to stripped logs.
 */
public final class StrippableBlockRegistry {
	private StrippableBlockRegistry() {
	}

	/**
	 * Registers a stripping interaction.
	 * The resulting BlockState of stripping of input will only copy the {@link Properties#AXIS axis} property, if it's present.
	 *
	 * @param input    the input block that can be stripped
	 * @param stripped the stripped result block
	 */
	public static void register(Block input, Block stripped) {
		StrippingTransformer transformer;

		if (input.getDefaultState().contains(Properties.AXIS) && stripped.getDefaultState().contains(Properties.AXIS)) {
			transformer = StrippingTransformer.VANILLA;
		} else {
			transformer = StrippingTransformer.DEFAULT_STATE;
		}

		StrippableBlockRegistryImpl.register(input, stripped, transformer);
	}

	/**
	 * Registers a stripping interaction.
	 * The resulting BlockState of stripping of input will copy all present properties.
	 *
	 * @param input    the input block that can be stripped
	 * @param stripped the stripped result block
	 */
	public static void registerCopyState(Block input, Block stripped) {
		StrippableBlockRegistryImpl.register(input, stripped, StrippingTransformer.COPY);
	}

	/**
	 * Registers a stripping interaction.
	 * The resulting BlockState of stripping of input will depend on provided transformer.
	 *
	 * @param input       the input block that can be stripped
	 * @param stripped    the stripped result block
	 * @param transformer the transformer used to provide the resulting block state
	 */
	public static void register(Block input, Block stripped, StrippingTransformer transformer) {
		StrippableBlockRegistryImpl.register(input, stripped, transformer);
	}

	/**
	 * Provides result of stripping interaction.
	 *
	 * @param blockState original block state
	 * @return stripped block state if successful, otherwise null
	 */
	@Nullable
	public static BlockState getStrippedBlockState(BlockState blockState) {
		return StrippableBlockRegistryImpl.getStrippedBlockState(blockState);
	}

	public interface StrippingTransformer {
		StrippingTransformer DEFAULT_STATE = (strippedBlock, originalState) -> strippedBlock.getDefaultState();
		StrippingTransformer VANILLA = (strippedBlock, originalState) -> strippedBlock.getDefaultState().withIfExists(Properties.AXIS, originalState.get(Properties.AXIS, Direction.Axis.Y));
		StrippingTransformer COPY = Block::getStateWithProperties;

		@Nullable
		BlockState getStrippedBlockState(Block strippedBlock, BlockState originalState);

		static StrippingTransformer copyOf(Property<?>... properties) {
			if (properties.length == 0) {
				return DEFAULT_STATE;
			}

			if (properties.length == 1 && properties[0] == Properties.AXIS) {
				return VANILLA;
			}

			return ((strippedBlock, originalState) -> {
				BlockState state = strippedBlock.getDefaultState();

				//noinspection rawtypes
				for (Property property : properties) {
					if (originalState.contains(property)) {
						//noinspection unchecked
						state = state.withIfExists(property, originalState.get(property));
					}
				}

				return state;
			});
		}
	}
}
