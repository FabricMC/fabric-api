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

package net.fabricmc.fabric.api.tag.convention.v1;

import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;

import net.fabricmc.fabric.impl.tag.convention.TagRegistration;

/**
 * @deprecated Please use {@link net.fabricmc.fabric.api.tag.convention.v2.ConventionalBlockTags}
 */
@Deprecated
public final class ConventionalBlockTags {
	private ConventionalBlockTags() {
	}

	// Ores and ingots - broad categories
	public static final TagKey<Block> ORES = register("ores");
	// Ores and ingots - vanilla instances
	public static final TagKey<Block> QUARTZ_ORES = register("quartz_ores");

	public static final TagKey<Block> BOOKSHELVES = register("bookshelves");
	public static final TagKey<Block> CHESTS = register("chests");
	public static final TagKey<Block> GLASS_BLOCKS = register("glass_blocks");
	public static final TagKey<Block> GLASS_PANES = register("glass_panes");
	public static final TagKey<Block> SHULKER_BOXES = register("shulker_boxes");
	public static final TagKey<Block> WOODEN_BARRELS = register("wooden_barrels");

	// Related to budding mechanics
	public static final TagKey<Block> BUDDING_BLOCKS = register("budding_blocks");
	public static final TagKey<Block> BUDS = register("buds");
	public static final TagKey<Block> CLUSTERS = register("clusters");

	public static final TagKey<Block> VILLAGER_JOB_SITES = register("villager_job_sites");

	// Sandstone
	public static final TagKey<Block> SANDSTONE_BLOCKS = register("sandstone_blocks");
	public static final TagKey<Block> SANDSTONE_SLABS = register("sandstone_slabs");
	public static final TagKey<Block> SANDSTONE_STAIRS = register("sandstone_stairs");
	public static final TagKey<Block> RED_SANDSTONE_BLOCKS = register("red_sandstone_blocks");
	public static final TagKey<Block> RED_SANDSTONE_SLABS = register("red_sandstone_slabs");
	public static final TagKey<Block> RED_SANDSTONE_STAIRS = register("red_sandstone_stairs");
	public static final TagKey<Block> UNCOLORED_SANDSTONE_BLOCKS = register("uncolored_sandstone_blocks");
	public static final TagKey<Block> UNCOLORED_SANDSTONE_SLABS = register("uncolored_sandstone_slabs");
	public static final TagKey<Block> UNCOLORED_SANDSTONE_STAIRS = register("uncolored_sandstone_stairs");

	/**
	 * Blocks should be included in this tag if their movement can cause serious issues such as world corruption
	 * upon being moved, such as chunk loaders or pipes,
	 * for mods that move blocks but do not respect {@link BlockBehaviour.BlockStateBase#getPistonPushReaction}.
	 */
	public static final TagKey<Block> MOVEMENT_RESTRICTED = register("movement_restricted");

	private static TagKey<Block> register(String tagID) {
		return TagRegistration.BLOCK_TAG_REGISTRATION.registerC(tagID);
	}
}
