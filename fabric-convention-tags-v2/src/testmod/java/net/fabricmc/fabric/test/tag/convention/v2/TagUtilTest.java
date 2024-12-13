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

package net.fabricmc.fabric.test.tag.convention.v2;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.minecraft.block.Blocks;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.world.biome.BiomeKeys;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.tag.convention.v2.ConventionalBiomeTags;
import net.fabricmc.fabric.api.tag.convention.v2.ConventionalBlockTags;
import net.fabricmc.fabric.api.tag.convention.v2.ConventionalEnchantmentTags;
import net.fabricmc.fabric.api.tag.convention.v2.ConventionalEntityTypeTags;
import net.fabricmc.fabric.api.tag.convention.v2.ConventionalFluidTags;
import net.fabricmc.fabric.api.tag.convention.v2.ConventionalItemTags;
import net.fabricmc.fabric.api.tag.convention.v2.ConventionalStructureTags;
import net.fabricmc.fabric.api.tag.convention.v2.TagUtil;

public class TagUtilTest implements ModInitializer {
	private static final Logger LOGGER = LoggerFactory.getLogger(TagUtilTest.class);

	private static final Class<?>[] TAG_CLASSES = {
			ConventionalBiomeTags.class,
			ConventionalBlockTags.class,
			ConventionalEnchantmentTags.class,
			ConventionalEntityTypeTags.class,
			ConventionalFluidTags.class,
			ConventionalItemTags.class,
			ConventionalStructureTags.class,
	};

	// Think twice before choosing an inconsistent name
	private static final Set<TagKey<?>> IGNORED_TAGS = Set.of(
			ConventionalBiomeTags.IS_CONIFEROUS_TREE,
			ConventionalBiomeTags.IS_DECIDUOUS_TREE,
			ConventionalBiomeTags.IS_JUNGLE_TREE,
			ConventionalBiomeTags.IS_SAVANNA_TREE,
			ConventionalBiomeTags.IS_VEGETATION_DENSE,
			ConventionalBiomeTags.IS_VEGETATION_DENSE_OVERWORLD,
			ConventionalBiomeTags.IS_VEGETATION_DENSE_NETHER,
			ConventionalBiomeTags.IS_VEGETATION_DENSE_END,
			ConventionalBiomeTags.IS_VEGETATION_SPARSE,
			ConventionalBiomeTags.IS_VEGETATION_SPARSE_OVERWORLD,
			ConventionalBiomeTags.IS_VEGETATION_SPARSE_NETHER,
			ConventionalBiomeTags.IS_VEGETATION_SPARSE_END,

			ConventionalBlockTags.RED_SANDSTONE_BLOCKS,
			ConventionalBlockTags.RED_SANDSTONE_SLABS,
			ConventionalBlockTags.RED_SANDSTONE_STAIRS,
			ConventionalBlockTags.UNCOLORED_SANDSTONE_BLOCKS,
			ConventionalBlockTags.UNCOLORED_SANDSTONE_SLABS,
			ConventionalBlockTags.UNCOLORED_SANDSTONE_STAIRS,

			ConventionalItemTags.BOTTLE_POTIONS,
			ConventionalItemTags.RED_SANDSTONE_BLOCKS,
			ConventionalItemTags.RED_SANDSTONE_SLABS,
			ConventionalItemTags.RED_SANDSTONE_STAIRS,
			ConventionalItemTags.UNCOLORED_SANDSTONE_BLOCKS,
			ConventionalItemTags.UNCOLORED_SANDSTONE_SLABS,
			ConventionalItemTags.UNCOLORED_SANDSTONE_STAIRS
	);

	private static boolean isValidTagField(Field field) {
		if (!Modifier.isStatic(field.getModifiers())) {
			// Ignore instance fields
			return false;
		}

		if (field.getType() != TagKey.class) {
			// Ignore fields that are not of type TagKey<?>
			return false;
		}

		if (field.getAnnotation(Deprecated.class) != null) {
			// Ignore deprecated fields
			return false;
		}

		return true;
	}

	private static void validateTagKey(TagKey<?> key, Field field, Class<?> clazz) {
		String path = key.id().getPath();
		String uppercasePath = path.toUpperCase(Locale.ROOT);

		String expected = field.getName();
		String[] acceptables;

		if (uppercasePath.contains("/")) {
			String[] parts = uppercasePath.split("/");
			List<String> reversedParts = List.of(parts).reversed();

			acceptables = new String[] {
				String.join("_", parts),
				String.join("_", reversedParts),
			};
		} else {
			acceptables = new String[] {
				uppercasePath,
			};
		}

		boolean found = false;

		for (String acceptable : acceptables) {
			if (expected.equals(acceptable)) {
				found = true;
				break;
			}
		}

		if (!found) {
			String fullName = clazz.getSimpleName() + "." + expected;
			throw new AssertionError("Expected field " + fullName + " to match one of " + Arrays.toString(acceptables) + " for tag " + key);
		}
	}

	@Override
	public void onInitialize() {
		for (Class<?> clazz : TAG_CLASSES) {
			for (Field field : clazz.getFields()) {
				if (isValidTagField(field)) {
					try {
						TagKey<?> key = (TagKey<?>) field.get(null);

						if (!IGNORED_TAGS.contains(key)) {
							validateTagKey(key, field, clazz);
						}
					} catch (IllegalAccessException | IllegalArgumentException e) {
						throw new AssertionError("Failed to access field " + field, e);
					}
				}
			}
		}

		ServerLifecycleEvents.SERVER_STARTED.register(server -> {
			if (!TagUtil.isIn(server.getRegistryManager(), ConventionalEnchantmentTags.INCREASE_BLOCK_DROPS, server.getRegistryManager().getOrThrow(RegistryKeys.ENCHANTMENT).get(Enchantments.FORTUNE))) {
				throw new AssertionError("Failed to find fortune in c:increase_block_drops!");
			}

			if (TagUtil.isIn(ConventionalBiomeTags.IS_OVERWORLD, server.getRegistryManager().getOrThrow(RegistryKeys.BIOME).get(BiomeKeys.BADLANDS))) {
				throw new AssertionError("Found a dynamic entry in a static registry?!");
			}

			// If this fails, the tag is missing a biome or the util is broken
			if (!TagUtil.isIn(server.getRegistryManager(), ConventionalBiomeTags.IS_OVERWORLD, server.getRegistryManager().getOrThrow(RegistryKeys.BIOME).get(BiomeKeys.BADLANDS))) {
				throw new AssertionError("Failed to find an overworld biome (%s) in c:in_overworld!".formatted(BiomeKeys.BADLANDS));
			}

			if (!TagUtil.isIn(server.getRegistryManager(), ConventionalBlockTags.ORES, Blocks.DIAMOND_ORE)) {
				throw new AssertionError("Failed to find diamond ore in c:ores!");
			}

			//Success!
			LOGGER.info("Completed TagUtil tests!");
		});
	}
}
