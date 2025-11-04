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
import static net.fabricmc.fabric.test.datagen.DataGeneratorTestContent.TEST_SOUND;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Path;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementType;
import net.minecraft.advancements.criterion.KilledTrigger;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.registries.RegistryPatchGenerator;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.network.chat.Component;
import net.minecraft.references.Blocks;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.RegistryFixedCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.predicates.LootItemBlockStatePropertyCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;

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

		FabricDataGenerator.Pack extraPack = dataGenerator.createBuiltinResourcePack(Identifier.fromNamespaceAndPath(MOD_ID, "extra"));
		CompletableFuture<HolderLookup.Provider> extraRegistries = RegistryPatchGenerator.createLookup(dataGenerator.getRegistries(), new RegistrySetBuilder()
				.add(TEST_DATAGEN_DYNAMIC_REGISTRY_KEY, c ->
						c.register(TEST_DYNAMIC_REGISTRY_EXTRA_ITEM_KEY, new DataGeneratorTestContent.TestDatagenObject(":tiny_potato:"))
				)
		).thenApply(RegistrySetBuilder.PatchedRegistries::full);
		extraPack.addProvider((FabricDataOutput out) -> new TestExtraDynamicRegistryProvider(out, extraRegistries));
	}

	@Override
	public void buildRegistry(RegistrySetBuilder registryBuilder) {
		registryBuilder.add(
				TEST_DATAGEN_DYNAMIC_REGISTRY_KEY,
				this::bootstrapTestDatagenRegistry
		);
		// do NOT add TEST_DATAGEN_DYNAMIC_EMPTY_REGISTRY_KEY, should still work without it
	}

	private void bootstrapTestDatagenRegistry(BootstrapContext<DataGeneratorTestContent.TestDatagenObject> registerable) {
		registerable.register(TEST_DYNAMIC_REGISTRY_ITEM_KEY, new DataGeneratorTestContent.TestDatagenObject(":tiny_potato:"));
	}

	private static class TestRecipeProvider extends FabricRecipeProvider {
		private TestRecipeProvider(FabricDataOutput output, CompletableFuture<HolderLookup.Provider> registriesFuture) {
			super(output, registriesFuture);
		}

		@Override
		protected RecipeProvider createRecipeProvider(HolderLookup.Provider registryLookup, RecipeOutput exporter) {
			return new RecipeProvider(registryLookup, exporter) {
				@Override
				public void buildRecipes() {
					planksFromLog(SIMPLE_BLOCK, ItemTags.ACACIA_LOGS, 1);

					shapeless(RecipeCategory.MISC, Items.DIAMOND_ORE, 4).requires(Items.ITEM_FRAME)
							.unlockedBy("has_frame", has(Items.ITEM_FRAME))
							.save(withConditions(output, ResourceConditions.registryContains(Registries.ITEM, BuiltInRegistries.ITEM.getKey(Items.DIAMOND_BLOCK))));
					shapeless(RecipeCategory.MISC, Items.EMERALD, 4).requires(Items.ITEM_FRAME, 2)
							.unlockedBy("has_frame", has(Items.ITEM_FRAME))
							.save(withConditions(output, ResourceConditions.registryContains(Biomes.PLAINS, Biomes.BADLANDS)));

					shapeless(RecipeCategory.MISC, Items.GOLD_INGOT).requires(Items.DIRT).unlockedBy("has_dirt", has(Items.DIRT)).save(withConditions(output, NEVER_LOADED));
					shapeless(RecipeCategory.MISC, Items.DIAMOND).requires(Items.STICK).unlockedBy("has_stick", has(Items.STICK)).save(withConditions(output, ALWAYS_LOADED));

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
					shapeless(RecipeCategory.MISC, Items.DIAMOND_BLOCK)
							.requires(Ingredient.of(Items.DIAMOND_PICKAXE))
							.requires(Ingredient.of(Items.DIAMOND_PICKAXE))
							.requires(Ingredient.of(Items.DIAMOND_PICKAXE))
							.requires(Ingredient.of(Items.DIAMOND_PICKAXE))
							.requires(DefaultCustomIngredients.components(
								Ingredient.of(Items.DIAMOND_PICKAXE),
								DataComponentPatch.builder()
									.set(DataComponents.DAMAGE, 0)
									.build()
								)
							)
							.requires(Ingredient.of(Items.DIAMOND_PICKAXE))
							.requires(Ingredient.of(Items.DIAMOND_PICKAXE))
							.requires(Ingredient.of(Items.DIAMOND_PICKAXE))
							.requires(Ingredient.of(Items.DIAMOND_PICKAXE))
							.unlockedBy("has_pickaxe", has(Items.DIAMOND_PICKAXE))
							.save(output);

					// Test AND
					// To test: charcoal should give a torch, but coal should not.
					shapeless(RecipeCategory.MISC, Items.TORCH)
							// charcoal only
							.requires(DefaultCustomIngredients.all(tag(ItemTags.COALS), Ingredient.of(Items.CHARCOAL)))
							.unlockedBy("has_charcoal", has(Items.CHARCOAL))
							.save(output);

					// Test OR
					// To test: a golden pickaxe or a golden shovel should give a block of gold.
					shapeless(RecipeCategory.MISC, Items.GOLD_BLOCK)
							.requires(DefaultCustomIngredients.any(Ingredient.of(Items.GOLDEN_PICKAXE), Ingredient.of(Items.GOLDEN_SHOVEL)))
							.unlockedBy("has_pickaxe", has(Items.GOLDEN_PICKAXE))
							.unlockedBy("has_shovel", has(Items.GOLDEN_SHOVEL))
							.save(output);

					// Test difference
					// To test: only copper, netherite and emerald should match the recipe.
					shapeless(RecipeCategory.MISC, Items.BEACON)
							.requires(DefaultCustomIngredients.difference(
									DefaultCustomIngredients.any(
											tag(ItemTags.BEACON_PAYMENT_ITEMS),
											Ingredient.of(Items.COPPER_INGOT)),
									Ingredient.of(Items.IRON_INGOT, Items.GOLD_INGOT, Items.DIAMOND)))
							.unlockedBy("has_payment", has(ItemTags.BEACON_PAYMENT_ITEMS))
							.save(output);
				}
			};
		}

		@Override
		public String getName() {
			return "Test Recipes";
		}
	}

	private static class ExistingEnglishLangProvider extends FabricLanguageProvider {
		private ExistingEnglishLangProvider(FabricDataOutput output, CompletableFuture<HolderLookup.Provider> registriesFuture) {
			super(output, registriesFuture);
		}

		@Override
		public void generateTranslations(HolderLookup.Provider registryLookup, TranslationBuilder translationBuilder) {
			translationBuilder.add(SIMPLE_BLOCK, "Simple Block");
			translationBuilder.add(Identifier.fromNamespaceAndPath(MOD_ID, "identifier_test"), "Identifier Test");
			translationBuilder.add(EntityType.ALLAY, "Allay");
			translationBuilder.add(Attributes.ARMOR, "Generic Armor");
			translationBuilder.add(TEST_SOUND, "Test Sound");

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
		private JapaneseLangProvider(FabricDataOutput output, CompletableFuture<HolderLookup.Provider> registriesFuture) {
			super(output, "ja_jp", registriesFuture);
		}

		@Override
		public void generateTranslations(HolderLookup.Provider registryLookup, TranslationBuilder translationBuilder) {
			translationBuilder.add(SIMPLE_BLOCK, "シンプルブロック");
			translationBuilder.add(SIMPLE_ITEM_GROUP, "データ生成項目");
			translationBuilder.add("this.is.a.test", "こんにちは");
		}
	}

	private static class TestBlockTagProvider extends FabricTagProvider.BlockTagProvider {
		TestBlockTagProvider(FabricDataOutput output, CompletableFuture<HolderLookup.Provider> registriesFuture) {
			super(output, registriesFuture);
		}

		@Override
		protected void addTags(HolderLookup.Provider registries) {
			valueLookupBuilder(BlockTags.FIRE).setReplace(true).add(SIMPLE_BLOCK);
			valueLookupBuilder(BlockTags.DIRT).add(SIMPLE_BLOCK);
			valueLookupBuilder(BlockTags.ACACIA_LOGS).forceAddTag(BlockTags.ANIMALS_SPAWNABLE_ON);

			aliasGroup("flowers")
					.add(BlockTags.FLOWERS, BlockTags.FLOWER_POTS);
			aliasGroup(Identifier.fromNamespaceAndPath("other_namespace", "flowers"))
					.add(BlockTags.FLOWERS, BlockTags.FLOWER_POTS);
		}
	}

	private static class TestItemTagProvider extends FabricTagProvider.ItemTagProvider {
		private TestItemTagProvider(FabricDataOutput output, CompletableFuture<HolderLookup.Provider> registriesFuture, BlockTagProvider blockTagProvider) {
			super(output, registriesFuture, blockTagProvider);
		}

		@Override
		protected void addTags(HolderLookup.Provider registries) {
			copy(BlockTags.DIRT, ItemTags.DIRT);
		}
	}

	private static class TestBiomeTagProvider extends FabricTagProvider<Biome> {
		private TestBiomeTagProvider(FabricDataOutput output, CompletableFuture<HolderLookup.Provider> registriesFuture) {
			super(output, Registries.BIOME, registriesFuture);
		}

		@Override
		protected void addTags(HolderLookup.Provider registries) {
			builder(TagKey.create(Registries.BIOME, Identifier.fromNamespaceAndPath(MOD_ID, "biome_tag_test")))
					.add(Biomes.BADLANDS)
					.add(Biomes.BAMBOO_JUNGLE)
					.add(Biomes.BASALT_DELTAS);
		}
	}

	private static class TestGameEventTagProvider extends FabricTagProvider<GameEvent> {
		private TestGameEventTagProvider(FabricDataOutput output, CompletableFuture<HolderLookup.Provider> registriesFuture) {
			super(output, Registries.GAME_EVENT, registriesFuture);
		}

		@Override
		protected void addTags(HolderLookup.Provider registries) {
			builder(TagKey.create(Registries.GAME_EVENT, Identifier.fromNamespaceAndPath(MOD_ID, "game_event_tag_test")))
					.add(GameEvent.SHRIEK.key());
		}
	}

	private static class TestAdvancementProvider extends FabricAdvancementProvider {
		private TestAdvancementProvider(FabricDataOutput output, CompletableFuture<HolderLookup.Provider> registryLookup) {
			super(output, registryLookup);
		}

		@Override
		public void generateAdvancement(HolderLookup.Provider registryLookup, Consumer<AdvancementHolder> consumer) {
			AdvancementHolder root = Advancement.Builder.advancement()
					.display(
							SIMPLE_BLOCK,
							Component.translatable("advancements.test.root.title"),
							Component.translatable("advancements.test.root.description"),
							Identifier.withDefaultNamespace("textures/gui/advancements/backgrounds/end.png"),
							AdvancementType.TASK,
							false, false, false)
					.addCriterion("killed_something", KilledTrigger.TriggerInstance.playerKilledEntity())
					.save(consumer, MOD_ID + ":test/root");
			AdvancementHolder rootNotLoaded = Advancement.Builder.advancement()
					.display(
							SIMPLE_BLOCK,
							Component.translatable("advancements.test.root_not_loaded.title"),
							Component.translatable("advancements.test.root_not_loaded.description"),
							Identifier.withDefaultNamespace("textures/gui/advancements/backgrounds/end.png"),
							AdvancementType.TASK,
							false, false, false)
					.addCriterion("killed_something", KilledTrigger.TriggerInstance.playerKilledEntity())
					.save(withConditions(consumer, NEVER_LOADED), MOD_ID + ":test/root_not_loaded");
		}
	}

	private static class TestBlockLootTableProvider extends FabricBlockLootTableProvider {
		private TestBlockLootTableProvider(FabricDataOutput output, CompletableFuture<HolderLookup.Provider> registryLookup) {
			super(output, registryLookup);
		}

		@Override
		public void generate() {
			// Same condition twice to test recursive condition adding
			withConditions(ALWAYS_LOADED).withConditions(ResourceConditions.not(NEVER_LOADED)).dropSelf(SIMPLE_BLOCK);
			add(BLOCK_WITHOUT_ITEM, createSingleItemTable(SIMPLE_BLOCK));

			excludeFromStrictValidation(BLOCK_WITHOUT_LOOT_TABLE);
		}
	}

	public static class TestEntityLootTableProvider extends FabricEntityLootTableProvider {
		private TestEntityLootTableProvider(FabricDataOutput output, CompletableFuture<HolderLookup.Provider> registryLookup) {
			super(output, registryLookup);
		}

		@Override
		public void generate() {
			this.withConditions(ALWAYS_LOADED)
					.withConditions(ResourceConditions.not(NEVER_LOADED))
					.add(
							SIMPLE_ENTITY_TYPE,
							LootTable.lootTable().withPool(LootPool.lootPool().add(LootItem.lootTableItem(SIMPLE_BLOCK.asItem())))
					);

			this.excludeFromStrictValidation(ENTITY_TYPE_WITHOUT_LOOT_TABLE);
		}
	}

	private static class TestBarterLootTableProvider extends SimpleFabricLootTableProvider {
		private TestBarterLootTableProvider(FabricDataOutput output, CompletableFuture<HolderLookup.Provider> registryLookup) {
			super(output, registryLookup, LootContextParamSets.PIGLIN_BARTER);
		}

		@Override
		public void generate(BiConsumer<ResourceKey<LootTable>, LootTable.Builder> consumer) {
			withConditions(consumer, ALWAYS_LOADED).accept(
					BuiltInLootTables.PIGLIN_BARTERING,
					LootTable.lootTable().withPool(
							LootPool.lootPool().setRolls(ConstantValue.exactly(1.0F)).add(LootItem.lootTableItem(SIMPLE_BLOCK))
					)
			);
		}
	}

	/**
	 * Tests generating files for a custom dynamic registry.
	 * Note that Biome API testmod provides the test for vanilla dynamic registries.
	 */
	private static class TestDynamicRegistryProvider extends FabricDynamicRegistryProvider {
		TestDynamicRegistryProvider(FabricDataOutput output, CompletableFuture<HolderLookup.Provider> registriesFuture) {
			super(output, registriesFuture);
		}

		@Override
		protected void configure(HolderLookup.Provider registries, Entries entries) {
			entries.add(
					registries.lookupOrThrow(TEST_DATAGEN_DYNAMIC_REGISTRY_KEY), TEST_DYNAMIC_REGISTRY_ITEM_KEY,
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
		TestExtraDynamicRegistryProvider(FabricDataOutput output, CompletableFuture<HolderLookup.Provider> registriesFuture) {
			super(output, registriesFuture);
		}

		@Override
		protected void configure(HolderLookup.Provider registries, Entries entries) {
			entries.add(registries.lookupOrThrow(TEST_DATAGEN_DYNAMIC_REGISTRY_KEY), TEST_DYNAMIC_REGISTRY_EXTRA_ITEM_KEY);
		}

		@Override
		public String getName() {
			return "Test Dynamic Registry";
		}
	}

	private static class TestPredicateProvider extends FabricCodecDataProvider<LootItemCondition> {
		private TestPredicateProvider(FabricDataOutput dataOutput, CompletableFuture<HolderLookup.Provider> registriesFuture) {
			super(dataOutput, registriesFuture, Registries.PREDICATE, LootItemCondition.DIRECT_CODEC);
		}

		@Override
		protected void configure(BiConsumer<Identifier, LootItemCondition> provider, HolderLookup.Provider lookup) {
			HolderGetter<Block> blocks = lookup.lookupOrThrow(Registries.BLOCK);
			provider.accept(Identifier.fromNamespaceAndPath(MOD_ID, "predicate_test"), LootItemBlockStatePropertyCondition.hasBlockStateProperties(
					blocks.getOrThrow(Blocks.MELON).value()).build()); // Pretend this actually does something and we cannot access the blocks directly
		}

		@Override
		public String getName() {
			return "Predicates";
		}
	}

	private static class TestCustomCodecProvider extends FabricCodecDataProvider<TestCustomCodecProvider.Entry> {
		private TestCustomCodecProvider(FabricDataOutput dataOutput, CompletableFuture<HolderLookup.Provider> registriesFuture) {
			super(dataOutput, registriesFuture, PackOutput.Target.DATA_PACK, "biome_entry", Entry.CODEC);
		}

		@Override
		protected void configure(BiConsumer<Identifier, Entry> provider, HolderLookup.Provider lookup) {
			HolderGetter<Biome> biomes = lookup.lookupOrThrow(Registries.BIOME);
			provider.accept(Identifier.fromNamespaceAndPath(MOD_ID, "custom_codec_test"), new Entry(biomes.getOrThrow(Biomes.PLAINS)));
		}

		@Override
		public String getName() {
			return "Codec Test Using Dynamic Registry";
		}

		private record Entry(Holder<Biome> biome) {
			private static final Codec<Entry> CODEC = RecordCodecBuilder.create(instance -> instance.group(
					RegistryFixedCodec.create(Registries.BIOME).fieldOf("biome").forGetter(Entry::biome)
			).apply(instance, Entry::new));
		}
	}
}
