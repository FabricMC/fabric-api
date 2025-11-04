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

package net.fabricmc.fabric.impl.tag.convention;

import java.util.AbstractMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.tag.convention.v1.ConventionalBiomeTags;
import net.fabricmc.fabric.api.tag.convention.v1.ConventionalBlockTags;
import net.fabricmc.fabric.api.tag.convention.v1.ConventionalEnchantmentTags;
import net.fabricmc.fabric.api.tag.convention.v1.ConventionalItemTags;
import net.fabricmc.fabric.api.tag.convention.v2.TagUtil;
import net.fabricmc.loader.api.FabricLoader;

public class ConventionLogWarnings implements ModInitializer {
	private static final Logger LOGGER = LoggerFactory.getLogger(ConventionLogWarnings.class);

	private static final LogWarningMode LOG_LEGACY_WARNING_MODE = setupLogWarningModeProperty();

	private static LogWarningMode setupLogWarningModeProperty() {
		String property = System.getProperty("fabric-tag-conventions-v1.legacyTagWarning", LogWarningMode.SHORT.name()).toUpperCase(Locale.ROOT);

		try {
			return LogWarningMode.valueOf(property);
		} catch (Exception e) {
			LOGGER.error("Unknown entry `{}` for property `fabric-tag-conventions-v1.legacyTagWarning`.", property);
			return LogWarningMode.SILENCED;
		}
	}

	private enum LogWarningMode {
		SILENCED,
		SHORT,
		VERBOSE,
		FAIL;

		boolean isVerbose() {
			return this == VERBOSE || this == FAIL;
		}
	}

	/**
	 * Old `c` tags that we migrated to a new tag under a new convention.
	 * May also contain commonly used `c` tags that are not following convention.
	 */
	private static final Map<TagKey<?>, TagKey<?>> LEGACY_C_TAGS = Map.<TagKey<?>, TagKey<?>>ofEntries(
			// Old v1 tags that are discouraged
			createMapEntry(ConventionalBlockTags.MOVEMENT_RESTRICTED, net.fabricmc.fabric.api.tag.convention.v2.ConventionalBlockTags.RELOCATION_NOT_SUPPORTED),
			createMapEntry(ConventionalBlockTags.QUARTZ_ORES, net.fabricmc.fabric.api.tag.convention.v2.ConventionalBlockTags.QUARTZ_ORES),
			createMapEntry(ConventionalBlockTags.WOODEN_BARRELS, net.fabricmc.fabric.api.tag.convention.v2.ConventionalBlockTags.WOODEN_BARRELS),
			createMapEntry(ConventionalBlockTags.SANDSTONE_BLOCKS, net.fabricmc.fabric.api.tag.convention.v2.ConventionalBlockTags.SANDSTONE_BLOCKS),
			createMapEntry(ConventionalBlockTags.SANDSTONE_STAIRS, net.fabricmc.fabric.api.tag.convention.v2.ConventionalBlockTags.SANDSTONE_STAIRS),
			createMapEntry(ConventionalBlockTags.SANDSTONE_SLABS, net.fabricmc.fabric.api.tag.convention.v2.ConventionalBlockTags.SANDSTONE_SLABS),
			createMapEntry(ConventionalBlockTags.RED_SANDSTONE_BLOCKS, net.fabricmc.fabric.api.tag.convention.v2.ConventionalBlockTags.RED_SANDSTONE_BLOCKS),
			createMapEntry(ConventionalBlockTags.RED_SANDSTONE_STAIRS, net.fabricmc.fabric.api.tag.convention.v2.ConventionalBlockTags.RED_SANDSTONE_STAIRS),
			createMapEntry(ConventionalBlockTags.RED_SANDSTONE_SLABS, net.fabricmc.fabric.api.tag.convention.v2.ConventionalBlockTags.RED_SANDSTONE_SLABS),
			createMapEntry(ConventionalBlockTags.UNCOLORED_SANDSTONE_BLOCKS, net.fabricmc.fabric.api.tag.convention.v2.ConventionalBlockTags.UNCOLORED_SANDSTONE_BLOCKS),
			createMapEntry(ConventionalBlockTags.UNCOLORED_SANDSTONE_STAIRS, net.fabricmc.fabric.api.tag.convention.v2.ConventionalBlockTags.UNCOLORED_SANDSTONE_STAIRS),
			createMapEntry(ConventionalBlockTags.UNCOLORED_SANDSTONE_SLABS, net.fabricmc.fabric.api.tag.convention.v2.ConventionalBlockTags.UNCOLORED_SANDSTONE_SLABS),

			createMapEntry(ConventionalItemTags.QUARTZ_ORES, net.fabricmc.fabric.api.tag.convention.v2.ConventionalItemTags.QUARTZ_ORES),
			createMapEntry(ConventionalItemTags.WOODEN_BARRELS, net.fabricmc.fabric.api.tag.convention.v2.ConventionalItemTags.WOODEN_BARRELS),
			createMapEntry(ConventionalItemTags.SANDSTONE_BLOCKS, net.fabricmc.fabric.api.tag.convention.v2.ConventionalItemTags.SANDSTONE_BLOCKS),
			createMapEntry(ConventionalItemTags.SANDSTONE_STAIRS, net.fabricmc.fabric.api.tag.convention.v2.ConventionalItemTags.SANDSTONE_STAIRS),
			createMapEntry(ConventionalItemTags.SANDSTONE_SLABS, net.fabricmc.fabric.api.tag.convention.v2.ConventionalItemTags.SANDSTONE_SLABS),
			createMapEntry(ConventionalItemTags.RED_SANDSTONE_BLOCKS, net.fabricmc.fabric.api.tag.convention.v2.ConventionalItemTags.RED_SANDSTONE_BLOCKS),
			createMapEntry(ConventionalItemTags.RED_SANDSTONE_STAIRS, net.fabricmc.fabric.api.tag.convention.v2.ConventionalItemTags.RED_SANDSTONE_STAIRS),
			createMapEntry(ConventionalItemTags.RED_SANDSTONE_SLABS, net.fabricmc.fabric.api.tag.convention.v2.ConventionalItemTags.RED_SANDSTONE_SLABS),
			createMapEntry(ConventionalItemTags.UNCOLORED_SANDSTONE_BLOCKS, net.fabricmc.fabric.api.tag.convention.v2.ConventionalItemTags.UNCOLORED_SANDSTONE_BLOCKS),
			createMapEntry(ConventionalItemTags.UNCOLORED_SANDSTONE_STAIRS, net.fabricmc.fabric.api.tag.convention.v2.ConventionalItemTags.UNCOLORED_SANDSTONE_STAIRS),
			createMapEntry(ConventionalItemTags.BLACK_DYES, net.fabricmc.fabric.api.tag.convention.v2.ConventionalItemTags.BLACK_DYES),
			createMapEntry(ConventionalItemTags.BLUE_DYES, net.fabricmc.fabric.api.tag.convention.v2.ConventionalItemTags.BLUE_DYES),
			createMapEntry(ConventionalItemTags.BROWN_DYES, net.fabricmc.fabric.api.tag.convention.v2.ConventionalItemTags.BROWN_DYES),
			createMapEntry(ConventionalItemTags.GREEN_DYES, net.fabricmc.fabric.api.tag.convention.v2.ConventionalItemTags.GREEN_DYES),
			createMapEntry(ConventionalItemTags.RED_DYES, net.fabricmc.fabric.api.tag.convention.v2.ConventionalItemTags.RED_DYES),
			createMapEntry(ConventionalItemTags.WHITE_DYES, net.fabricmc.fabric.api.tag.convention.v2.ConventionalItemTags.WHITE_DYES),
			createMapEntry(ConventionalItemTags.YELLOW_DYES, net.fabricmc.fabric.api.tag.convention.v2.ConventionalItemTags.YELLOW_DYES),
			createMapEntry(ConventionalItemTags.LIGHT_BLUE_DYES, net.fabricmc.fabric.api.tag.convention.v2.ConventionalItemTags.LIGHT_BLUE_DYES),
			createMapEntry(ConventionalItemTags.LIGHT_GRAY_DYES, net.fabricmc.fabric.api.tag.convention.v2.ConventionalItemTags.LIGHT_GRAY_DYES),
			createMapEntry(ConventionalItemTags.LIME_DYES, net.fabricmc.fabric.api.tag.convention.v2.ConventionalItemTags.LIME_DYES),
			createMapEntry(ConventionalItemTags.MAGENTA_DYES, net.fabricmc.fabric.api.tag.convention.v2.ConventionalItemTags.MAGENTA_DYES),
			createMapEntry(ConventionalItemTags.ORANGE_DYES, net.fabricmc.fabric.api.tag.convention.v2.ConventionalItemTags.ORANGE_DYES),
			createMapEntry(ConventionalItemTags.PINK_DYES, net.fabricmc.fabric.api.tag.convention.v2.ConventionalItemTags.PINK_DYES),
			createMapEntry(ConventionalItemTags.CYAN_DYES, net.fabricmc.fabric.api.tag.convention.v2.ConventionalItemTags.CYAN_DYES),
			createMapEntry(ConventionalItemTags.GRAY_DYES, net.fabricmc.fabric.api.tag.convention.v2.ConventionalItemTags.GRAY_DYES),
			createMapEntry(ConventionalItemTags.PURPLE_DYES, net.fabricmc.fabric.api.tag.convention.v2.ConventionalItemTags.PURPLE_DYES),
			createMapEntry(ConventionalItemTags.RAW_IRON_ORES, net.fabricmc.fabric.api.tag.convention.v2.ConventionalItemTags.IRON_RAW_MATERIALS),
			createMapEntry(ConventionalItemTags.RAW_GOLD_ORES, net.fabricmc.fabric.api.tag.convention.v2.ConventionalItemTags.GOLD_RAW_MATERIALS),
			createMapEntry(ConventionalItemTags.DIAMONDS, net.fabricmc.fabric.api.tag.convention.v2.ConventionalItemTags.DIAMOND_GEMS),
			createMapEntry(ConventionalItemTags.LAPIS, net.fabricmc.fabric.api.tag.convention.v2.ConventionalItemTags.LAPIS_GEMS),
			createMapEntry(ConventionalItemTags.EMERALDS, net.fabricmc.fabric.api.tag.convention.v2.ConventionalItemTags.EMERALD_GEMS),
			createMapEntry(ConventionalItemTags.QUARTZ, net.fabricmc.fabric.api.tag.convention.v2.ConventionalItemTags.QUARTZ_GEMS),
			createMapEntry(ConventionalItemTags.SHEARS, net.fabricmc.fabric.api.tag.convention.v2.ConventionalItemTags.SHEAR_TOOLS),
			createMapEntry(ConventionalItemTags.SPEARS, net.fabricmc.fabric.api.tag.convention.v2.ConventionalItemTags.SPEAR_TOOLS),
			createMapEntry(ConventionalItemTags.BOWS, net.fabricmc.fabric.api.tag.convention.v2.ConventionalItemTags.BOW_TOOLS),
			createMapEntry(ConventionalItemTags.SHIELDS, net.fabricmc.fabric.api.tag.convention.v2.ConventionalItemTags.SHIELD_TOOLS),

			createMapEntry(ConventionalEnchantmentTags.INCREASES_BLOCK_DROPS, net.fabricmc.fabric.api.tag.convention.v2.ConventionalEnchantmentTags.INCREASE_BLOCK_DROPS),
			createMapEntry(ConventionalEnchantmentTags.INCREASES_ENTITY_DROPS, net.fabricmc.fabric.api.tag.convention.v2.ConventionalEnchantmentTags.INCREASE_ENTITY_DROPS),
			createMapEntry(ConventionalEnchantmentTags.ENTITY_MOVEMENT_ENHANCEMENT, net.fabricmc.fabric.api.tag.convention.v2.ConventionalEnchantmentTags.ENTITY_SPEED_ENHANCEMENTS),
			createMapEntry(ConventionalEnchantmentTags.ENTITY_DEFENSE_ENHANCEMENT, net.fabricmc.fabric.api.tag.convention.v2.ConventionalEnchantmentTags.ENTITY_DEFENSE_ENHANCEMENTS),

			createMapEntry(ConventionalBiomeTags.IN_NETHER, net.fabricmc.fabric.api.tag.convention.v2.ConventionalBiomeTags.IS_NETHER),
			createMapEntry(ConventionalBiomeTags.IN_THE_END, net.fabricmc.fabric.api.tag.convention.v2.ConventionalBiomeTags.IS_END),
			createMapEntry(ConventionalBiomeTags.IN_OVERWORLD, net.fabricmc.fabric.api.tag.convention.v2.ConventionalBiomeTags.IS_OVERWORLD),
			createMapEntry(ConventionalBiomeTags.CAVES, net.fabricmc.fabric.api.tag.convention.v2.ConventionalBiomeTags.IS_CAVE),
			createMapEntry(ConventionalBiomeTags.CLIMATE_COLD, net.fabricmc.fabric.api.tag.convention.v2.ConventionalBiomeTags.IS_COLD),
			createMapEntry(ConventionalBiomeTags.CLIMATE_TEMPERATE, net.fabricmc.fabric.api.tag.convention.v2.ConventionalBiomeTags.IS_TEMPERATE),
			createMapEntry(ConventionalBiomeTags.CLIMATE_HOT, net.fabricmc.fabric.api.tag.convention.v2.ConventionalBiomeTags.IS_HOT),
			createMapEntry(ConventionalBiomeTags.CLIMATE_WET, net.fabricmc.fabric.api.tag.convention.v2.ConventionalBiomeTags.IS_WET),
			createMapEntry(ConventionalBiomeTags.CLIMATE_DRY, net.fabricmc.fabric.api.tag.convention.v2.ConventionalBiomeTags.IS_DRY),
			createMapEntry(ConventionalBiomeTags.VEGETATION_DENSE, net.fabricmc.fabric.api.tag.convention.v2.ConventionalBiomeTags.IS_VEGETATION_DENSE),
			createMapEntry(ConventionalBiomeTags.VEGETATION_SPARSE, net.fabricmc.fabric.api.tag.convention.v2.ConventionalBiomeTags.IS_VEGETATION_SPARSE),
			createMapEntry(ConventionalBiomeTags.TREE_CONIFEROUS, net.fabricmc.fabric.api.tag.convention.v2.ConventionalBiomeTags.IS_CONIFEROUS_TREE),
			createMapEntry(ConventionalBiomeTags.TREE_DECIDUOUS, net.fabricmc.fabric.api.tag.convention.v2.ConventionalBiomeTags.IS_DECIDUOUS_TREE),
			createMapEntry(ConventionalBiomeTags.TREE_JUNGLE, net.fabricmc.fabric.api.tag.convention.v2.ConventionalBiomeTags.IS_JUNGLE_TREE),
			createMapEntry(ConventionalBiomeTags.TREE_SAVANNA, net.fabricmc.fabric.api.tag.convention.v2.ConventionalBiomeTags.IS_SAVANNA_TREE),
			createMapEntry(ConventionalBiomeTags.MOUNTAIN_PEAK, net.fabricmc.fabric.api.tag.convention.v2.ConventionalBiomeTags.IS_MOUNTAIN_PEAK),
			createMapEntry(ConventionalBiomeTags.MOUNTAIN_SLOPE, net.fabricmc.fabric.api.tag.convention.v2.ConventionalBiomeTags.IS_MOUNTAIN_SLOPE),
			createMapEntry(ConventionalBiomeTags.END_ISLANDS, net.fabricmc.fabric.api.tag.convention.v2.ConventionalBiomeTags.IS_OUTER_END_ISLAND),
			createMapEntry(ConventionalBiomeTags.NETHER_FORESTS, net.fabricmc.fabric.api.tag.convention.v2.ConventionalBiomeTags.IS_NETHER_FOREST),
			createMapEntry(ConventionalBiomeTags.FLOWER_FORESTS, net.fabricmc.fabric.api.tag.convention.v2.ConventionalBiomeTags.IS_FLOWER_FOREST),

			// Commonly used `c` tags that are using discouraged conventions. (Not plural or not folder form)
			createMapEntry(Registries.BLOCK, "barrel", net.fabricmc.fabric.api.tag.convention.v2.ConventionalBlockTags.BARRELS),
			createMapEntry(Registries.BLOCK, "chest", net.fabricmc.fabric.api.tag.convention.v2.ConventionalBlockTags.CHESTS),
			createMapEntry(Registries.BLOCK, "wooden_chests", net.fabricmc.fabric.api.tag.convention.v2.ConventionalBlockTags.WOODEN_CHESTS),
			createMapEntry(Registries.BLOCK, "glass", net.fabricmc.fabric.api.tag.convention.v2.ConventionalBlockTags.GLASS_BLOCKS),
			createMapEntry(Registries.BLOCK, "glass_pane", net.fabricmc.fabric.api.tag.convention.v2.ConventionalBlockTags.GLASS_PANES),
			createMapEntry(Registries.BLOCK, "immobile", net.fabricmc.fabric.api.tag.convention.v2.ConventionalBlockTags.RELOCATION_NOT_SUPPORTED),
			createMapEntry(Registries.BLOCK, "stone", net.fabricmc.fabric.api.tag.convention.v2.ConventionalBlockTags.STONES),
			createMapEntry(Registries.BLOCK, "cobblestone", net.fabricmc.fabric.api.tag.convention.v2.ConventionalBlockTags.COBBLESTONES),
			createMapEntry(Registries.BLOCK, "workbench", net.fabricmc.fabric.api.tag.convention.v2.ConventionalBlockTags.VILLAGER_JOB_SITES),
			createMapEntry(Registries.BLOCK, "workbenches", net.fabricmc.fabric.api.tag.convention.v2.ConventionalBlockTags.VILLAGER_JOB_SITES),
			createMapEntry(Registries.BLOCK, "workstation", net.fabricmc.fabric.api.tag.convention.v2.ConventionalBlockTags.VILLAGER_JOB_SITES),
			createMapEntry(Registries.BLOCK, "workstations", net.fabricmc.fabric.api.tag.convention.v2.ConventionalBlockTags.VILLAGER_JOB_SITES),
			createMapEntry(Registries.BLOCK, "crafting_table", net.fabricmc.fabric.api.tag.convention.v2.ConventionalBlockTags.PLAYER_WORKSTATIONS_CRAFTING_TABLES),
			createMapEntry(Registries.BLOCK, "crafting_tables", net.fabricmc.fabric.api.tag.convention.v2.ConventionalBlockTags.PLAYER_WORKSTATIONS_CRAFTING_TABLES),
			createMapEntry(Registries.BLOCK, "furnace", net.fabricmc.fabric.api.tag.convention.v2.ConventionalBlockTags.PLAYER_WORKSTATIONS_FURNACES),
			createMapEntry(Registries.BLOCK, "furnaces", net.fabricmc.fabric.api.tag.convention.v2.ConventionalBlockTags.PLAYER_WORKSTATIONS_FURNACES),

			createMapEntry(Registries.ITEM, "axes", ItemTags.AXES),
			createMapEntry(Registries.ITEM, "pickaxes", ItemTags.PICKAXES),
			createMapEntry(Registries.ITEM, "hoes", ItemTags.HOES),
			createMapEntry(Registries.ITEM, "shovels", ItemTags.SHOVELS),
			createMapEntry(Registries.ITEM, "swords", ItemTags.SWORDS),
			createMapEntry(createTagKeyUnderFabric(Registries.ITEM, "axes"), ItemTags.AXES),
			createMapEntry(createTagKeyUnderFabric(Registries.ITEM, "pickaxes"), ItemTags.PICKAXES),
			createMapEntry(createTagKeyUnderFabric(Registries.ITEM, "hoes"), ItemTags.HOES),
			createMapEntry(createTagKeyUnderFabric(Registries.ITEM, "shovels"), ItemTags.SHOVELS),
			createMapEntry(createTagKeyUnderFabric(Registries.ITEM, "swords"), ItemTags.SWORDS),
			createMapEntry(Registries.ITEM, "wrenches", net.fabricmc.fabric.api.tag.convention.v2.ConventionalItemTags.WRENCH_TOOLS),
			createMapEntry(Registries.ITEM, "tools/wrenches", net.fabricmc.fabric.api.tag.convention.v2.ConventionalItemTags.WRENCH_TOOLS),
			createMapEntry(Registries.ITEM, "barrel", net.fabricmc.fabric.api.tag.convention.v2.ConventionalItemTags.BARRELS),
			createMapEntry(Registries.ITEM, "chest", net.fabricmc.fabric.api.tag.convention.v2.ConventionalItemTags.CHESTS),
			createMapEntry(Registries.ITEM, "glass", net.fabricmc.fabric.api.tag.convention.v2.ConventionalItemTags.GLASS_BLOCKS),
			createMapEntry(Registries.ITEM, "glass_pane", net.fabricmc.fabric.api.tag.convention.v2.ConventionalItemTags.GLASS_PANES),
			createMapEntry(Registries.ITEM, "glowstone_dusts", net.fabricmc.fabric.api.tag.convention.v2.ConventionalItemTags.GLOWSTONE_DUSTS),
			createMapEntry(Registries.ITEM, "redstone_dusts", net.fabricmc.fabric.api.tag.convention.v2.ConventionalItemTags.REDSTONE_DUSTS),
			createMapEntry(Registries.ITEM, "stone", net.fabricmc.fabric.api.tag.convention.v2.ConventionalItemTags.STONES),
			createMapEntry(Registries.ITEM, "string", net.fabricmc.fabric.api.tag.convention.v2.ConventionalItemTags.STRINGS),
			createMapEntry(Registries.ITEM, "sticks", net.fabricmc.fabric.api.tag.convention.v2.ConventionalItemTags.WOODEN_RODS),
			createMapEntry(Registries.ITEM, "wooden_rods", net.fabricmc.fabric.api.tag.convention.v2.ConventionalItemTags.WOODEN_RODS),
			createMapEntry(Registries.ITEM, "food", net.fabricmc.fabric.api.tag.convention.v2.ConventionalItemTags.FOODS),
			createMapEntry(Registries.ITEM, "fruit", net.fabricmc.fabric.api.tag.convention.v2.ConventionalItemTags.FRUIT_FOODS),
			createMapEntry(Registries.ITEM, "fruits", net.fabricmc.fabric.api.tag.convention.v2.ConventionalItemTags.FRUIT_FOODS),
			createMapEntry(Registries.ITEM, "vegetable", net.fabricmc.fabric.api.tag.convention.v2.ConventionalItemTags.VEGETABLE_FOODS),
			createMapEntry(Registries.ITEM, "vegetables", net.fabricmc.fabric.api.tag.convention.v2.ConventionalItemTags.VEGETABLE_FOODS),
			createMapEntry(Registries.ITEM, "berry", net.fabricmc.fabric.api.tag.convention.v2.ConventionalItemTags.BERRY_FOODS),
			createMapEntry(Registries.ITEM, "berries", net.fabricmc.fabric.api.tag.convention.v2.ConventionalItemTags.BERRY_FOODS),
			createMapEntry(Registries.ITEM, "bread", net.fabricmc.fabric.api.tag.convention.v2.ConventionalItemTags.BREAD_FOODS),
			createMapEntry(Registries.ITEM, "breads", net.fabricmc.fabric.api.tag.convention.v2.ConventionalItemTags.BREAD_FOODS),
			createMapEntry(Registries.ITEM, "cookie", net.fabricmc.fabric.api.tag.convention.v2.ConventionalItemTags.COOKIE_FOODS),
			createMapEntry(Registries.ITEM, "cookies", net.fabricmc.fabric.api.tag.convention.v2.ConventionalItemTags.COOKIE_FOODS),
			createMapEntry(Registries.ITEM, "raw_meat", net.fabricmc.fabric.api.tag.convention.v2.ConventionalItemTags.RAW_MEAT_FOODS),
			createMapEntry(Registries.ITEM, "raw_meats", net.fabricmc.fabric.api.tag.convention.v2.ConventionalItemTags.RAW_MEAT_FOODS),
			createMapEntry(Registries.ITEM, "raw_fish", net.fabricmc.fabric.api.tag.convention.v2.ConventionalItemTags.RAW_FISH_FOODS),
			createMapEntry(Registries.ITEM, "raw_fishes", net.fabricmc.fabric.api.tag.convention.v2.ConventionalItemTags.RAW_FISH_FOODS),
			createMapEntry(Registries.ITEM, "cooked_meat", net.fabricmc.fabric.api.tag.convention.v2.ConventionalItemTags.COOKED_MEAT_FOODS),
			createMapEntry(Registries.ITEM, "cooked_meats", net.fabricmc.fabric.api.tag.convention.v2.ConventionalItemTags.COOKED_MEAT_FOODS),
			createMapEntry(Registries.ITEM, "cooked_fish", net.fabricmc.fabric.api.tag.convention.v2.ConventionalItemTags.COOKED_FISH_FOODS),
			createMapEntry(Registries.ITEM, "cooked_fishes", net.fabricmc.fabric.api.tag.convention.v2.ConventionalItemTags.COOKED_FISH_FOODS),
			createMapEntry(Registries.ITEM, "soup", net.fabricmc.fabric.api.tag.convention.v2.ConventionalItemTags.SOUP_FOODS),
			createMapEntry(Registries.ITEM, "soups", net.fabricmc.fabric.api.tag.convention.v2.ConventionalItemTags.SOUP_FOODS),
			createMapEntry(Registries.ITEM, "stew", net.fabricmc.fabric.api.tag.convention.v2.ConventionalItemTags.SOUP_FOODS),
			createMapEntry(Registries.ITEM, "stews", net.fabricmc.fabric.api.tag.convention.v2.ConventionalItemTags.SOUP_FOODS),
			createMapEntry(Registries.ITEM, "candy", net.fabricmc.fabric.api.tag.convention.v2.ConventionalItemTags.CANDY_FOODS),
			createMapEntry(Registries.ITEM, "candies", net.fabricmc.fabric.api.tag.convention.v2.ConventionalItemTags.CANDY_FOODS),
			createMapEntry(Registries.ITEM, "pie", net.fabricmc.fabric.api.tag.convention.v2.ConventionalItemTags.PIE_FOODS),
			createMapEntry(Registries.ITEM, "pies", net.fabricmc.fabric.api.tag.convention.v2.ConventionalItemTags.PIE_FOODS),
			createMapEntry(TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath("minecraft", "music_discs")), net.fabricmc.fabric.api.tag.convention.v2.ConventionalItemTags.MUSIC_DISCS),
			createMapEntry(TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath("minecraft", "tall_flowers")), net.fabricmc.fabric.api.tag.convention.v2.ConventionalItemTags.TALL_FLOWERS),
			createMapEntry(TagKey.create(Registries.BLOCK, Identifier.fromNamespaceAndPath("minecraft", "tall_flowers")), net.fabricmc.fabric.api.tag.convention.v2.ConventionalBlockTags.TALL_FLOWERS)
	);

	@Override
	public void onInitialize() {
		if (FabricLoader.getInstance().isDevelopmentEnvironment() && LOG_LEGACY_WARNING_MODE != LogWarningMode.SILENCED) {
			setupLegacyTagWarning();
		}
	}

	private static void setupLegacyTagWarning() {
		// Log tags that are still using legacy conventions under 'c' namespace
		ServerLifecycleEvents.SERVER_STARTED.register(server -> {
			List<TagKey<?>> legacyTags = new ObjectArrayList<>();
			RegistryAccess.Frozen dynamicRegistries = server.registryAccess();

			// We only care about vanilla registries
			dynamicRegistries.registries().forEach(registryEntry -> {
				if (registryEntry.key().identifier().getNamespace().equals(Identifier.DEFAULT_NAMESPACE)) {
					registryEntry.value().getTags().forEach(listEntry -> {
						TagKey<?> tagKey = TagKey.create(registryEntry.key(), listEntry.key().location());

						// Grab legacy tags we migrated or discourage
						if (LEGACY_C_TAGS.containsKey(tagKey)) {
							legacyTags.add(tagKey);
						}
					});
				}
			});

			if (legacyTags.isEmpty()) {
				return;
			}

			StringBuilder stringBuilder = new StringBuilder();
			stringBuilder.append("""
					\n	Dev warning - Legacy Tags detected. Please migrate your old tags to our new format that follows better conventions!
						See classes under net.fabricmc.fabric.api.tag.convention.v2 package for all tags.

						NOTE: Many tags have been moved around or renamed. Some new ones were added so please review the new tags.
						And make sure you follow tag conventions for new tags! The convention is `c` with nouns generally being plural and adjectives being singular.
						You can disable this message by this system property to your runs: `-Dfabric-tag-conventions-v1.legacyTagWarning=SILENCED`.
						To see individual legacy tags found, set the system property to `-Dfabric-tag-conventions-v1.legacyTagWarning=VERBOSE` instead. Default is `SHORT`.
					""");

			// Print out all legacy tags when desired.
			if (LOG_LEGACY_WARNING_MODE.isVerbose()) {
				stringBuilder.append("\nLegacy tags and their replacement:");

				for (TagKey<?> tagKey : legacyTags) {
					stringBuilder.append("\n     ").append(tagKey).append("  ->  ").append(LEGACY_C_TAGS.get(tagKey));
				}
			}

			LOGGER.warn(stringBuilder.toString());

			if (LOG_LEGACY_WARNING_MODE == LogWarningMode.FAIL) {
				throw new RuntimeException("Legacy Tag validation failed");
			}
		});
	}

	private static <T> AbstractMap.SimpleEntry<TagKey<T>, TagKey<T>> createMapEntry(TagKey<T> tag1, TagKey<T> tag2) {
		return new AbstractMap.SimpleEntry<>(tag1, tag2);
	}

	private static <T> AbstractMap.SimpleEntry<TagKey<T>, TagKey<T>> createMapEntry(ResourceKey<Registry<T>> registryKey, String tagId1, TagKey<T> tag2) {
		return new AbstractMap.SimpleEntry<>(createTagKeyUnderC(registryKey, tagId1), tag2);
	}

	private static <T> AbstractMap.SimpleEntry<TagKey<T>, TagKey<T>> createMapEntry(ResourceKey<Registry<T>> registryKey, String tagId1, String tagId2) {
		return new AbstractMap.SimpleEntry<>(createTagKeyUnderC(registryKey, tagId1), createTagKeyUnderC(registryKey, tagId2));
	}

	private static <T> TagKey<T> createTagKeyUnderC(ResourceKey<Registry<T>> registryKey, String tagId) {
		return TagKey.create(registryKey, Identifier.fromNamespaceAndPath(TagUtil.C_TAG_NAMESPACE, tagId));
	}

	private static <T> TagKey<T> createTagKeyUnderFabric(ResourceKey<Registry<T>> registryKey, String tagId) {
		return TagKey.create(registryKey, Identifier.fromNamespaceAndPath(TagUtil.FABRIC_TAG_NAMESPACE, tagId));
	}
}
