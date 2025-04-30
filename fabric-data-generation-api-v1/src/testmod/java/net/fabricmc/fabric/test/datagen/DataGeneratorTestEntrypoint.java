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

package net.fabricmc.fabric.test.datagen;

import static net.fabricmc.fabric.test.datagen.DataGeneratorTestContent.BLOCK_WITHOUT_ITEM;
import static net.fabricmc.fabric.test.datagen.DataGeneratorTestContent.BLOCK_WITHOUT_LOOT_TABLE;
import static net.fabricmc.fabric.test.datagen.DataGeneratorTestContent.ENTITY_TYPE_WITHOUT_LOOT_TABLE;
import static net.fabricmc.fabric.test.datagen.DataGeneratorTestContent.MOD_ID;
import static net.fabricmc.fabric.test.datagen.DataGeneratorTestContent.SIMPLE_BLOCK;
import static net.fabricmc.fabric.test.datagen.DataGeneratorTestContent.SIMPLE_ENTITY_TYPE;
import static net.fabricmc.fabric.test.datagen.DataGeneratorTestContent.SIMPLE_ITEM_GROUP;
import static net.fabricmc.fabric.test.datagen.DataGeneratorTestContent.TEST_DATAGEN_DYNAMIC_REGISTRY_KEY;
import static net.fabricmc.fabric.test.datagen.DataGeneratorTestContent.TEST_DYNAMIC_REGISTRY_EXTRA_ITEM_KEY;
import static net.fabricmc.fabric.test.datagen.DataGeneratorTestContent.TEST_DYNAMIC_REGISTRY_ITEM_KEY;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Path;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.advancement.Advancement;
import net.minecraft.advancement.AdvancementEntry;
import net.minecraft.advancement.AdvancementFrame;
import net.minecraft.advancement.criterion.OnKilledCriterion;
import net.minecraft.block.Block;
import net.minecraft.block.BlockKeys;
import net.minecraft.component.ComponentChanges;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.data.DataOutput;
import net.minecraft.data.recipe.RecipeExporter;
import net.minecraft.data.recipe.RecipeGenerator;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.item.Items;
import net.minecraft.loot.LootPool;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.LootTables;
import net.minecraft.loot.condition.BlockStatePropertyLootCondition;
import net.minecraft.loot.condition.LootCondition;
import net.minecraft.loot.context.LootContextTypes;
import net.minecraft.loot.entry.ItemEntry;
import net.minecraft.loot.provider.number.ConstantLootNumberProvider;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.book.RecipeCategory;
import net.minecraft.registry.ExperimentalRegistriesValidator;
import net.minecraft.registry.Registerable;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryBuilder;
import net.minecraft.registry.RegistryEntryLookup;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.entry.RegistryFixedCodec;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeKeys;
import net.minecraft.world.event.GameEvent;

import net.fabricmc.api.EnvType;
import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.JsonKeySortOrderCallback;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricAdvancementProvider;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricBlockLootTableProvider;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricCodecDataProvider;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricDynamicRegistryProvider;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricEntityLootTableProvider;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricLanguageProvider;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricRecipeProvider;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider;
import net.fabricmc.fabric.api.datagen.v1.provider.SimpleFabricLootTableProvider;
import net.fabricmc.fabric.api.recipe.v1.ingredient.DefaultCustomIngredients;
import net.fabricmc.fabric.api.resource.conditions.v1.ResourceCondition;
import net.fabricmc.fabric.api.resource.conditions.v1.ResourceConditions;
import net.fabricmc.loader.api.FabricLoader;

public class DataGeneratorTestEntrypoint implements DataGeneratorEntrypoint {
	private static final ResourceCondition ALWAYS_LOADED = ResourceConditions.alwaysTrue();
	private static final ResourceCondition NEVER_LOADED = ResourceConditions.not(ALWAYS_LOADED);

	@Override
	public void addJsonKeySortOrders(JsonKeySortOrderCallback callback) {
		callback.add("trigger", 0);
	}

	@Override
	public void onInitializeDataGenerator(FabricDataGenerator dataGenerator) {
		final FabricDataGenerator.Pack pack = dataGenerator.createPack();

		pack.addProvider(TestRecipeProvider::new);
		pack.addProvider(TestAdvancementProvider::new);
		pack.addProvider(TestBlockLootTableProvider::new);
		pack.addProvider(TestEntityLootTableProvider::new);
		pack.addProvider(TestBarterLootTableProvider::new);
		pack.addProvider(ExistingEnglishLangProvider::new);
		pack.addProvider(JapaneseLangProvider::new);
		pack.addProvider(TestDynamicRegistryProvider::new);
		pack.addProvider(TestPredicateProvider::new);
		pack.addProvider(TestCustomCodecProvider::new);

		TestBlockTagProvider blockTagProvider = pack.addProvider(TestBlockTagProvider::new);
		pack.addProvider((output, registries) -> new TestItemTagProvider(output, registries, blockTagProvider));
		pack.addProvider(TestBiomeTagProvider::new);
		pack.addProvider(TestGameEventTagProvider::new);

		// TODO replace with a client only entrypoint with FMJ 2
		if (FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT) {
			try {
				Class<?> clientEntrypointClass = Class.forName("net.fabricmc.fabric.test.datagen.client.DataGeneratorClientTestEntrypoint");
				DataGeneratorEntrypoint entrypoint = (DataGeneratorEntrypoint) clientEntrypointClass.getConstructor().newInstance();
				entrypoint.onInitializeDataGenerator(dataGenerator);
			} catch (ClassNotFoundException | NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
				throw new RuntimeException(e);
			}
		}

		FabricDataGenerator.Pack extraPack = dataGenerator.createBuiltinResourcePack(Identifier.of(MOD_ID, "extra"));
		CompletableFuture<RegistryWrapper.WrapperLookup> extraRegistries = ExperimentalRegistriesValidator.validate(dataGenerator.getRegistries(), new RegistryBuilder()
				.addRegistry(TEST_DATAGEN_DYNAMIC_REGISTRY_KEY, c ->
						c.register(TEST_DYNAMIC_REGISTRY_EXTRA_ITEM_KEY, new DataGeneratorTestContent.TestDatagenObject(":tiny_potato:"))
				)
		).thenApply(RegistryBuilder.FullPatchesRegistriesPair::full);
		extraPack.addProvider((FabricDataOutput out) -> new TestExtraDynamicRegistryProvider(out, extraRegistries));
	}

	@Override
	public void buildRegistry(RegistryBuilder registryBuilder) {
		registryBuilder.addRegistry(
				TEST_DATAGEN_DYNAMIC_REGISTRY_KEY,
				this::bootstrapTestDatagenRegistry
		);
		// do NOT add TEST_DATAGEN_DYNAMIC_EMPTY_REGISTRY_KEY, should still work without it
	}

	private void bootstrapTestDatagenRegistry(Registerable<DataGeneratorTestContent.TestDatagenObject> registerable) {
		registerable.register(TEST_DYNAMIC_REGISTRY_ITEM_KEY, new DataGeneratorTestContent.TestDatagenObject(":tiny_potato:"));
	}

	private static class TestRecipeProvider extends FabricRecipeProvider {
		private TestRecipeProvider(FabricDataOutput output, CompletableFuture<RegistryWrapper.WrapperLookup> registriesFuture) {
			super(output, registriesFuture);
		}

		@Override
		protected RecipeGenerator getRecipeGenerator(RegistryWrapper.WrapperLookup registryLookup, RecipeExporter exporter) {
			return new RecipeGenerator(registryLookup, exporter) {
				@Override
				public void generate() {
					offerPlanksRecipe2(SIMPLE_BLOCK, ItemTags.ACACIA_LOGS, 1);

					createShapeless(RecipeCategory.MISC, Items.DIAMOND_ORE, 4).input(Items.ITEM_FRAME)
							.criterion("has_frame", conditionsFromItem(Items.ITEM_FRAME))
							.offerTo(withConditions(exporter, ResourceConditions.registryContains(RegistryKeys.ITEM, Registries.ITEM.getId(Items.DIAMOND_BLOCK))));
					createShapeless(RecipeCategory.MISC, Items.EMERALD, 4).input(Items.ITEM_FRAME, 2)
							.criterion("has_frame", conditionsFromItem(Items.ITEM_FRAME))
							.offerTo(withConditions(exporter, ResourceConditions.registryContains(BiomeKeys.PLAINS, BiomeKeys.BADLANDS)));

					createShapeless(RecipeCategory.MISC, Items.GOLD_INGOT).input(Items.DIRT).criterion("has_dirt", conditionsFromItem(Items.DIRT)).offerTo(withConditions(exporter, NEVER_LOADED));
					createShapeless(RecipeCategory.MISC, Items.DIAMOND).input(Items.STICK).criterion("has_stick", conditionsFromItem(Items.STICK)).offerTo(withConditions(exporter, ALWAYS_LOADED));

					/* Generate test recipes using all types of custom ingredients for easy testing */
					// Testing procedure for vanilla and fabric clients:
					// - Create a new fabric server with the ingredient API.
					// - Copy the generated recipes to a datapack, for example to world/datapacks/<packname>/data/test/recipe/.
					// - Remember to also include a pack.mcmeta file in world/datapacks/<packname>.
					// (see https://minecraft.wiki/w/Tutorials/Creating_a_data_pack)
					// - Start the server and connect to it with a vanilla client.
					// - Test all the following recipes

					// Test partial NBT
					// 1 undamaged pickaxe + 8 pickaxes with any damage value to test shapeless matching logic.
					// Interesting test cases:
					// - 9 damaged pickaxes should not match.
					// - 9 undamaged pickaxes should match.
					// - 1 undamaged pickaxe + 8 damaged pickaxes should match (regardless of the position).
					// - 1 undamaged renamed pickaxe + 8 damaged pickaxes should match (components are not strictly matched here).
					createShapeless(RecipeCategory.MISC, Items.DIAMOND_BLOCK)
							.input(Ingredient.ofItems(Items.DIAMOND_PICKAXE))
							.input(Ingredient.ofItems(Items.DIAMOND_PICKAXE))
							.input(Ingredient.ofItems(Items.DIAMOND_PICKAXE))
							.input(Ingredient.ofItems(Items.DIAMOND_PICKAXE))
							.input(DefaultCustomIngredients.components(
								Ingredient.ofItems(Items.DIAMOND_PICKAXE),
								ComponentChanges.builder()
									.add(DataComponentTypes.DAMAGE, 0)
									.build()
								)
							)
							.input(Ingredient.ofItems(Items.DIAMOND_PICKAXE))
							.input(Ingredient.ofItems(Items.DIAMOND_PICKAXE))
							.input(Ingredient.ofItems(Items.DIAMOND_PICKAXE))
							.input(Ingredient.ofItems(Items.DIAMOND_PICKAXE))
							.criterion("has_pickaxe", conditionsFromItem(Items.DIAMOND_PICKAXE))
							.offerTo(exporter);

					// Test AND
					// To test: charcoal should give a torch, but coal should not.
					createShapeless(RecipeCategory.MISC, Items.TORCH)
							// charcoal only
							.input(DefaultCustomIngredients.all(ingredientFromTag(ItemTags.COALS), Ingredient.ofItems(Items.CHARCOAL)))
							.criterion("has_charcoal", conditionsFromItem(Items.CHARCOAL))
							.offerTo(exporter);

					// Test OR
					// To test: a golden pickaxe or a golden shovel should give a block of gold.
					createShapeless(RecipeCategory.MISC, Items.GOLD_BLOCK)
							.input(DefaultCustomIngredients.any(Ingredient.ofItems(Items.GOLDEN_PICKAXE), Ingredient.ofItems(Items.GOLDEN_SHOVEL)))
							.criterion("has_pickaxe", conditionsFromItem(Items.GOLDEN_PICKAXE))
							.criterion("has_shovel", conditionsFromItem(Items.GOLDEN_SHOVEL))
							.offerTo(exporter);

					// Test difference
					// To test: only copper, netherite and emerald should match the recipe.
					createShapeless(RecipeCategory.MISC, Items.BEACON)
							.input(DefaultCustomIngredients.difference(
									DefaultCustomIngredients.any(
											ingredientFromTag(ItemTags.BEACON_PAYMENT_ITEMS),
											Ingredient.ofItems(Items.COPPER_INGOT)),
									Ingredient.ofItems(Items.IRON_INGOT, Items.GOLD_INGOT, Items.DIAMOND)))
							.criterion("has_payment", conditionsFromTag(ItemTags.BEACON_PAYMENT_ITEMS))
							.offerTo(exporter);
				}
			};
		}

		@Override
		public String getName() {
			return "Test Recipes";
		}
	}

	private static class ExistingEnglishLangProvider extends FabricLanguageProvider {
		private ExistingEnglishLangProvider(FabricDataOutput output, CompletableFuture<RegistryWrapper.WrapperLookup> registriesFuture) {
			super(output, registriesFuture);
		}

		@Override
		public void generateTranslations(RegistryWrapper.WrapperLookup registryLookup, TranslationBuilder translationBuilder) {
			translationBuilder.add(SIMPLE_BLOCK, "Simple Block");
			translationBuilder.add(Identifier.of(MOD_ID, "identifier_test"), "Identifier Test");
			translationBuilder.add(EntityType.ALLAY, "Allay");
			translationBuilder.add(EntityAttributes.ARMOR, "Generic Armor");

			try {
				Optional<Path> path = dataOutput.getModContainer().findPath("assets/testmod/lang/en_us.base.json");

				if (path.isPresent()) {
					translationBuilder.add(path.get());
				} else {
					throw new RuntimeException("The existing language file could not be found in the testmod assets!");
				}
			} catch (IOException e) {
				throw new RuntimeException(e);
			}

			try {
				translationBuilder.add(EntityType.ALLAY, "Allay Duplicate Test");
			} catch (RuntimeException e) {
				LOGGER.info("Duplicate test passed.");
			}
		}
	}

	private static class JapaneseLangProvider extends FabricLanguageProvider {
		private JapaneseLangProvider(FabricDataOutput output, CompletableFuture<RegistryWrapper.WrapperLookup> registriesFuture) {
			super(output, "ja_jp", registriesFuture);
		}

		@Override
		public void generateTranslations(RegistryWrapper.WrapperLookup registryLookup, TranslationBuilder translationBuilder) {
			translationBuilder.add(SIMPLE_BLOCK, "シンプルブロック");
			translationBuilder.add(SIMPLE_ITEM_GROUP, "データ生成項目");
			translationBuilder.add("this.is.a.test", "こんにちは");
		}
	}

	private static class TestBlockTagProvider extends FabricTagProvider.BlockTagProvider {
		TestBlockTagProvider(FabricDataOutput output, CompletableFuture<RegistryWrapper.WrapperLookup> registriesFuture) {
			super(output, registriesFuture);
		}

		@Override
		protected void configure(RegistryWrapper.WrapperLookup registries) {
			valueLookupBuilder(BlockTags.FIRE).setReplace(true).add(SIMPLE_BLOCK);
			valueLookupBuilder(BlockTags.DIRT).add(SIMPLE_BLOCK);
			valueLookupBuilder(BlockTags.ACACIA_LOGS).forceAddTag(BlockTags.ANIMALS_SPAWNABLE_ON);

			aliasGroup("flowers")
					.add(BlockTags.FLOWERS, BlockTags.FLOWER_POTS);
			aliasGroup(Identifier.of("other_namespace", "flowers"))
					.add(BlockTags.FLOWERS, BlockTags.FLOWER_POTS);
		}
	}

	private static class TestItemTagProvider extends FabricTagProvider.ItemTagProvider {
		private TestItemTagProvider(FabricDataOutput output, CompletableFuture<RegistryWrapper.WrapperLookup> registriesFuture, BlockTagProvider blockTagProvider) {
			super(output, registriesFuture, blockTagProvider);
		}

		@Override
		protected void configure(RegistryWrapper.WrapperLookup registries) {
			copy(BlockTags.DIRT, ItemTags.DIRT);
		}
	}

	private static class TestBiomeTagProvider extends FabricTagProvider<Biome> {
		private TestBiomeTagProvider(FabricDataOutput output, CompletableFuture<RegistryWrapper.WrapperLookup> registriesFuture) {
			super(output, RegistryKeys.BIOME, registriesFuture);
		}

		@Override
		protected void configure(RegistryWrapper.WrapperLookup registries) {
			builder(TagKey.of(RegistryKeys.BIOME, Identifier.of(MOD_ID, "biome_tag_test")))
					.add(BiomeKeys.BADLANDS)
					.add(BiomeKeys.BAMBOO_JUNGLE)
					.add(BiomeKeys.BASALT_DELTAS);
		}
	}

	private static class TestGameEventTagProvider extends FabricTagProvider<GameEvent> {
		private TestGameEventTagProvider(FabricDataOutput output, CompletableFuture<RegistryWrapper.WrapperLookup> registriesFuture) {
			super(output, RegistryKeys.GAME_EVENT, registriesFuture);
		}

		@Override
		protected void configure(RegistryWrapper.WrapperLookup registries) {
			builder(TagKey.of(RegistryKeys.GAME_EVENT, Identifier.of(MOD_ID, "game_event_tag_test")))
					.add(GameEvent.SHRIEK.registryKey());
		}
	}

	private static class TestAdvancementProvider extends FabricAdvancementProvider {
		private TestAdvancementProvider(FabricDataOutput output, CompletableFuture<RegistryWrapper.WrapperLookup> registryLookup) {
			super(output, registryLookup);
		}

		@Override
		public void generateAdvancement(RegistryWrapper.WrapperLookup registryLookup, Consumer<AdvancementEntry> consumer) {
			AdvancementEntry root = Advancement.Builder.create()
					.display(
							SIMPLE_BLOCK,
							Text.translatable("advancements.test.root.title"),
							Text.translatable("advancements.test.root.description"),
							Identifier.ofVanilla("textures/gui/advancements/backgrounds/end.png"),
							AdvancementFrame.TASK,
							false, false, false)
					.criterion("killed_something", OnKilledCriterion.Conditions.createPlayerKilledEntity())
					.build(consumer, MOD_ID + ":test/root");
			AdvancementEntry rootNotLoaded = Advancement.Builder.create()
					.display(
							SIMPLE_BLOCK,
							Text.translatable("advancements.test.root_not_loaded.title"),
							Text.translatable("advancements.test.root_not_loaded.description"),
							Identifier.ofVanilla("textures/gui/advancements/backgrounds/end.png"),
							AdvancementFrame.TASK,
							false, false, false)
					.criterion("killed_something", OnKilledCriterion.Conditions.createPlayerKilledEntity())
					.build(withConditions(consumer, NEVER_LOADED), MOD_ID + ":test/root_not_loaded");
		}
	}

	private static class TestBlockLootTableProvider extends FabricBlockLootTableProvider {
		private TestBlockLootTableProvider(FabricDataOutput output, CompletableFuture<RegistryWrapper.WrapperLookup> registryLookup) {
			super(output, registryLookup);
		}

		@Override
		public void generate() {
			// Same condition twice to test recursive condition adding
			withConditions(ALWAYS_LOADED).withConditions(ResourceConditions.not(NEVER_LOADED)).addDrop(SIMPLE_BLOCK);
			addDrop(BLOCK_WITHOUT_ITEM, drops(SIMPLE_BLOCK));

			excludeFromStrictValidation(BLOCK_WITHOUT_LOOT_TABLE);
		}
	}

	public static class TestEntityLootTableProvider extends FabricEntityLootTableProvider {
		private TestEntityLootTableProvider(FabricDataOutput output, CompletableFuture<RegistryWrapper.WrapperLookup> registryLookup) {
			super(output, registryLookup);
		}

		@Override
		public void generate() {
			this.withConditions(ALWAYS_LOADED)
					.withConditions(ResourceConditions.not(NEVER_LOADED))
					.register(
							SIMPLE_ENTITY_TYPE,
							LootTable.builder().pool(LootPool.builder().with(ItemEntry.builder(SIMPLE_BLOCK.asItem())))
					);

			this.excludeFromStrictValidation(ENTITY_TYPE_WITHOUT_LOOT_TABLE);
		}
	}

	private static class TestBarterLootTableProvider extends SimpleFabricLootTableProvider {
		private TestBarterLootTableProvider(FabricDataOutput output, CompletableFuture<RegistryWrapper.WrapperLookup> registryLookup) {
			super(output, registryLookup, LootContextTypes.BARTER);
		}

		@Override
		public void accept(BiConsumer<RegistryKey<LootTable>, LootTable.Builder> consumer) {
			withConditions(consumer, ALWAYS_LOADED).accept(
					LootTables.PIGLIN_BARTERING_GAMEPLAY,
					LootTable.builder().pool(
							LootPool.builder().rolls(ConstantLootNumberProvider.create(1.0F)).with(ItemEntry.builder(SIMPLE_BLOCK))
					)
			);
		}
	}

	/**
	 * Tests generating files for a custom dynamic registry.
	 * Note that Biome API testmod provides the test for vanilla dynamic registries.
	 */
	private static class TestDynamicRegistryProvider extends FabricDynamicRegistryProvider {
		TestDynamicRegistryProvider(FabricDataOutput output, CompletableFuture<RegistryWrapper.WrapperLookup> registriesFuture) {
			super(output, registriesFuture);
		}

		@Override
		protected void configure(RegistryWrapper.WrapperLookup registries, Entries entries) {
			entries.add(
					registries.getOrThrow(TEST_DATAGEN_DYNAMIC_REGISTRY_KEY), TEST_DYNAMIC_REGISTRY_ITEM_KEY,
					ResourceConditions.allModsLoaded(MOD_ID)
			);
		}

		@Override
		public String getName() {
			return "Test Dynamic Registry";
		}
	}

	/**
	 * Test generating files for a patched/extended dynamic registry.
	 */
	private static class TestExtraDynamicRegistryProvider extends FabricDynamicRegistryProvider {
		TestExtraDynamicRegistryProvider(FabricDataOutput output, CompletableFuture<RegistryWrapper.WrapperLookup> registriesFuture) {
			super(output, registriesFuture);
		}

		@Override
		protected void configure(RegistryWrapper.WrapperLookup registries, Entries entries) {
			entries.add(registries.getOrThrow(TEST_DATAGEN_DYNAMIC_REGISTRY_KEY), TEST_DYNAMIC_REGISTRY_EXTRA_ITEM_KEY);
		}

		@Override
		public String getName() {
			return "Test Dynamic Registry";
		}
	}

	private static class TestPredicateProvider extends FabricCodecDataProvider<LootCondition> {
		private TestPredicateProvider(FabricDataOutput dataOutput, CompletableFuture<RegistryWrapper.WrapperLookup> registriesFuture) {
			super(dataOutput, registriesFuture, RegistryKeys.PREDICATE, LootCondition.CODEC);
		}

		@Override
		protected void configure(BiConsumer<Identifier, LootCondition> provider, RegistryWrapper.WrapperLookup lookup) {
			RegistryEntryLookup<Block> blocks = lookup.getOrThrow(RegistryKeys.BLOCK);
			provider.accept(Identifier.of(MOD_ID, "predicate_test"), BlockStatePropertyLootCondition.builder(
					blocks.getOrThrow(BlockKeys.MELON).value()).build()); // Pretend this actually does something and we cannot access the blocks directly
		}

		@Override
		public String getName() {
			return "Predicates";
		}
	}

	private static class TestCustomCodecProvider extends FabricCodecDataProvider<TestCustomCodecProvider.Entry> {
		private TestCustomCodecProvider(FabricDataOutput dataOutput, CompletableFuture<RegistryWrapper.WrapperLookup> registriesFuture) {
			super(dataOutput, registriesFuture, DataOutput.OutputType.DATA_PACK, "biome_entry", Entry.CODEC);
		}

		@Override
		protected void configure(BiConsumer<Identifier, Entry> provider, RegistryWrapper.WrapperLookup lookup) {
			RegistryEntryLookup<Biome> biomes = lookup.getOrThrow(RegistryKeys.BIOME);
			provider.accept(Identifier.of(MOD_ID, "custom_codec_test"), new Entry(biomes.getOrThrow(BiomeKeys.PLAINS)));
		}

		@Override
		public String getName() {
			return "Codec Test Using Dynamic Registry";
		}

		private record Entry(RegistryEntry<Biome> biome) {
			private static final Codec<Entry> CODEC = RecordCodecBuilder.create(instance -> instance.group(
					RegistryFixedCodec.of(RegistryKeys.BIOME).fieldOf("biome").forGetter(Entry::biome)
			).apply(instance, Entry::new));
		}
	}
}
