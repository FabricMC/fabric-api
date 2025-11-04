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

import org.jspecify.annotations.Nullable;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.Property;

import net.fabricmc.fabric.impl.content.registry.StrippableBlockRegistryImpl;

/**
 * A registry for axe stripping interactions. A vanilla example is turning logs to stripped logs.
 */
public final class StrippableBlockRegistry {
	private StrippableBlockRegistry() {
	}

	/**
	 * Registers a stripping interaction.
	 * The resulting BlockState of stripping of input will only copy the {@link BlockStateProperties#AXIS axis} property, if it's present.
	 *
	 * @param input    the input block that can be stripped
	 * @param stripped the stripped result block
	 */
	public static void register(Block input, Block stripped) {
		StrippingTransformer transformer;

		if (input.defaultBlockState().hasProperty(BlockStateProperties.AXIS) && stripped.defaultBlockState().hasProperty(BlockStateProperties.AXIS)) {
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
		StrippingTransformer DEFAULT_STATE = (strippedBlock, originalState) -> strippedBlock.defaultBlockState();
		StrippingTransformer VANILLA = (strippedBlock, originalState) -> strippedBlock.defaultBlockState().trySetValue(BlockStateProperties.AXIS, originalState.getValueOrElse(BlockStateProperties.AXIS, Direction.Axis.Y));
		StrippingTransformer COPY = Block::withPropertiesOf;

		@Nullable
		BlockState getStrippedBlockState(Block strippedBlock, BlockState originalState);

		static StrippingTransformer copyOf(Property<?>... properties) {
			if (properties.length == 0) {
				return DEFAULT_STATE;
			}

			if (properties.length == 1 && properties[0] == BlockStateProperties.AXIS) {
				return VANILLA;
			}

			return ((strippedBlock, originalState) -> {
				BlockState state = strippedBlock.defaultBlockState();

				//noinspection rawtypes
				for (Property property : properties) {
					if (originalState.hasProperty(property)) {
						//noinspection unchecked
						state = state.trySetValue(property, originalState.getValue(property));
					}
				}

				return state;
			});
		}
	}
}
