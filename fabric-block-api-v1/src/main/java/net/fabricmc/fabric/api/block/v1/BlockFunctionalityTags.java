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

package net.fabricmc.fabric.api.block.v1;

import net.minecraft.block.Block;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;

/**
 * Contains block tags that add extra functionality to blocks.
 */
public final class BlockFunctionalityTags {
	/**
	 * Blocks in this tag let the player climb open trapdoors above them.
	 *
	 * <p>If a tagged block is a {@link net.minecraft.block.LadderBlock}, the block state's {@code facing}
	 * property must additionally match the trapdoor's direction, to match how vanilla ladders work.
	 */
	public static final TagKey<Block> CAN_CLIMB_TRAPDOOR_ABOVE = create("can_climb_trapdoor_above");

	private BlockFunctionalityTags() {
	}

	private static TagKey<Block> create(String name) {
		return TagKey.of(RegistryKeys.BLOCK, Identifier.of("fabric", name));
	}
}
