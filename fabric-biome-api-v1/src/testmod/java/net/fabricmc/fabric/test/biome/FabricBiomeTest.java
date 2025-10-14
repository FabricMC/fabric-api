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

package net.fabricmc.fabric.test.biome;

import static net.fabricmc.fabric.test.biome.DataGeneratorEntrypoint.PLACED_COMMON_DESERT_WELL;
import static net.fabricmc.fabric.test.biome.DataGeneratorEntrypoint.PLACED_COMMON_ORE;

import com.google.common.base.Preconditions;

import net.minecraft.class_12206;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;
import net.minecraft.world.biome.BiomeKeys;
import net.minecraft.world.biome.source.util.MultiNoiseUtil;
import net.minecraft.world.gen.GenerationStep;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.fabricmc.fabric.api.biome.v1.ModificationPhase;
import net.fabricmc.fabric.api.biome.v1.NetherBiomes;
import net.fabricmc.fabric.api.biome.v1.TheEndBiomes;

/**
 * <b>NOTES FOR TESTING:</b>
 * When running with this test-mod, also test this when running a dedicated server since there
 * are significant differences between server + client and how they sync biomes.
 *
 * <p>Ingame, you can use <code>/locatebiome</code> since we use nether- and end-biomes in the overworld,
 * and vice-versa, making them easy to find to verify the injection worked.
 *
 * <p>If you don't find a biome right away, teleport far away (~10000 blocks) from spawn and try again.
 */
public class FabricBiomeTest implements ModInitializer {
	public static final String MOD_ID = "fabric-biome-api-v1-testmod";

	@Override
	public void onInitialize() {
		Preconditions.checkArgument(NetherBiomes.canGenerateInNether(BiomeKeys.NETHER_WASTES));
		Preconditions.checkArgument(!NetherBiomes.canGenerateInNether(BiomeKeys.END_HIGHLANDS));

		NetherBiomes.addNetherBiome(BiomeKeys.PLAINS, MultiNoiseUtil.createNoiseHypercube(0.0F, 0.5F, 0.0F, 0.0F, 0.0f, 0, 0.1F));
		NetherBiomes.addNetherBiome(TestBiomes.TEST_CRIMSON_FOREST, MultiNoiseUtil.createNoiseHypercube(0.0F, -0.15F, 0.0f, 0.0F, 0.0f, 0.0F, 0.2F));

		Preconditions.checkArgument(NetherBiomes.canGenerateInNether(TestBiomes.TEST_CRIMSON_FOREST));

		// TESTING HINT: to get to the end:
		// /execute in minecraft:the_end run tp @s 0 90 0
		TheEndBiomes.addHighlandsBiome(BiomeKeys.PLAINS, 5.0);
		TheEndBiomes.addHighlandsBiome(TestBiomes.TEST_END_HIGHLANDS, 5.0);
		TheEndBiomes.addMidlandsBiome(TestBiomes.TEST_END_HIGHLANDS, TestBiomes.TEST_END_MIDLANDS, 10.0);
		TheEndBiomes.addBarrensBiome(TestBiomes.TEST_END_HIGHLANDS, TestBiomes.TEST_END_BARRRENS, 10.0);

		BiomeModifications.create(Identifier.of("fabric", "test_mod"))
				.add(ModificationPhase.ADDITIONS,
						BiomeSelectors.foundInOverworld(),
						modification -> modification.getWeather().setDownfall(100))
				.add(ModificationPhase.ADDITIONS,
						BiomeSelectors.includeByKey(BiomeKeys.DESERT), // TODO: switch to fabric desert biome tag once it is there?
						context -> {
							context.getGenerationSettings().addFeature(GenerationStep.Feature.TOP_LAYER_MODIFICATION,
									PLACED_COMMON_DESERT_WELL
							);
						})
				.add(ModificationPhase.ADDITIONS,
						BiomeSelectors.tag(TagKey.of(RegistryKeys.BIOME, Identifier.of(MOD_ID, "tag_selector_test"))),
						context -> context.getAttributes().set(class_12206.SKY_COLOR_VISUAL, 0x770000))
				.add(ModificationPhase.ADDITIONS, BiomeSelectors.foundInOverworld(), context ->
						context.getGenerationSettings().addFeature(GenerationStep.Feature.UNDERGROUND_ORES, PLACED_COMMON_ORE)
				);

		// Make sure data packs can define dynamic registry contents
		// See #2225, #2261
		BiomeModifications.addFeature(
				BiomeSelectors.foundInOverworld(),
				GenerationStep.Feature.VEGETAL_DECORATION,
				RegistryKey.of(RegistryKeys.PLACED_FEATURE, Identifier.of(MOD_ID, "concrete_pile"))
		);

		// Make sure data packs can define biomes
		NetherBiomes.addNetherBiome(
				TestBiomes.EXAMPLE_BIOME,
				MultiNoiseUtil.createNoiseHypercube(1.0f, 0.0f, 0.0f, 0.0f, 0.2f, 0.5f, 0.3f)
		);
		TheEndBiomes.addHighlandsBiome(
				TestBiomes.EXAMPLE_BIOME,
				10.0
		);
	}
}
