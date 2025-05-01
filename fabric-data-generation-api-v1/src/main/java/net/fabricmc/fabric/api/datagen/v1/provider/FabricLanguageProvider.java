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

package net.fabricmc.fabric.api.datagen.v1.provider;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.concurrent.CompletableFuture;

import com.google.gson.JsonObject;
import org.jetbrains.annotations.ApiStatus;

import net.minecraft.block.Block;
import net.minecraft.data.DataOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.DataWriter;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.stat.StatType;
import net.minecraft.text.TextContent;
import net.minecraft.text.TranslatableTextContent;
import net.minecraft.util.Identifier;
import net.minecraft.util.StrictJsonParser;
import net.minecraft.util.Util;

import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;

/**
 * Extend this class and implement {@link FabricLanguageProvider#generateTranslations}.
 * Make sure to use {@link FabricLanguageProvider#FabricLanguageProvider(FabricDataOutput, String, CompletableFuture) FabricLanguageProvider} to declare what language code is being generated if it isn't {@code en_us}.
 *
 * <p>Register an instance of the class with {@link FabricDataGenerator.Pack#addProvider} in a {@link net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint}.
 */
public abstract class FabricLanguageProvider implements DataProvider {
	protected final FabricDataOutput dataOutput;
	private final String languageCode;
	private final CompletableFuture<RegistryWrapper.WrapperLookup> registryLookup;

	protected FabricLanguageProvider(FabricDataOutput dataOutput, CompletableFuture<RegistryWrapper.WrapperLookup> registryLookup) {
		this(dataOutput, "en_us", registryLookup);
	}

	protected FabricLanguageProvider(FabricDataOutput dataOutput, String languageCode, CompletableFuture<RegistryWrapper.WrapperLookup> registryLookup) {
		this.dataOutput = dataOutput;
		this.languageCode = languageCode;
		this.registryLookup = registryLookup;
	}

	/**
	 * Implement this method to register languages.
	 *
	 * <p>Call {@link TranslationBuilder#add(String, String)} to add a translation.
	 */
	public abstract void generateTranslations(RegistryWrapper.WrapperLookup registryLookup, TranslationBuilder translationBuilder);

	@Override
	public CompletableFuture<?> run(DataWriter writer) {
		TreeMap<String, String> translationEntries = new TreeMap<>();

		return this.registryLookup.thenCompose(lookup -> {
			generateTranslations(lookup, (String key, String value) -> {
				Objects.requireNonNull(key);
				Objects.requireNonNull(value);

				if (translationEntries.containsKey(key)) {
					throw new RuntimeException("Existing translation key found - " + key + " - Duplicate will be ignored.");
				}

				translationEntries.put(key, value);
			});

			JsonObject langEntryJson = new JsonObject();

			for (Map.Entry<String, String> entry : translationEntries.entrySet()) {
				langEntryJson.addProperty(entry.getKey(), entry.getValue());
			}

			return DataProvider.writeToPath(writer, langEntryJson, getLangFilePath(this.languageCode));
		});
	}

	private Path getLangFilePath(String code) {
		return dataOutput
				.getResolver(DataOutput.OutputType.RESOURCE_PACK, "lang")
				.resolveJson(Identifier.of(dataOutput.getModId(), code));
	}

	@Override
	public String getName() {
		return "Language (%s)".formatted(languageCode);
	}

	/**
	 * A consumer used by {@link FabricLanguageProvider#generateTranslations}.
	 */
	@ApiStatus.NonExtendable
	@FunctionalInterface
	public interface TranslationBuilder {
		/**
		 * Adds a translation.
		 *
		 * @param translationKey The key of the translation.
		 * @param value          The value of the entry.
		 */
		void add(String translationKey, String value);

		/**
		 * Adds a translation for an {@link Item}.
		 *
		 * @param item  The {@link Item} to get the translation key from.
		 * @param value The value of the entry.
		 */
		default void add(Item item, String value) {
			add(item.getTranslationKey(), value);
		}

		/**
		 * Adds a translation for a {@link Block}.
		 *
		 * @param block The {@link Block} to get the translation key from.
		 * @param value The value of the entry.
		 */
		default void add(Block block, String value) {
			add(block.getTranslationKey(), value);
		}

		/**
		 * Adds a translation for an {@link ItemGroup}.
		 *
		 * @param registryKey The {@link RegistryKey} to get the translation key from.
		 * @param value The value of the entry.
		 */
		default void add(RegistryKey<ItemGroup> registryKey, String value) {
			final ItemGroup group = Registries.ITEM_GROUP.getValueOrThrow(registryKey);
			final TextContent content = group.getDisplayName().getContent();

			if (content instanceof TranslatableTextContent translatableTextContent) {
				add(translatableTextContent.getKey(), value);
				return;
			}

			throw new UnsupportedOperationException("Cannot add language entry for ItemGroup (%s) as the display name is not translatable.".formatted(group.getDisplayName().getString()));
		}

		/**
		 * Adds a translation for an {@link EntityType}.
		 *
		 * @param entityType The {@link EntityType} to get the translation key from.
		 * @param value      The value of the entry.
		 */
		default void add(EntityType<?> entityType, String value) {
			add(entityType.getTranslationKey(), value);
		}

		/**
		 * Adds a translation for an {@link Enchantment}.
		 *
		 * @param enchantment The {@link Enchantment} to get the translation key from.
		 * @param value       The value of the entry.
		 */
		default void addEnchantment(RegistryKey<Enchantment> enchantment, String value) {
			add(Util.createTranslationKey("enchantment", enchantment.getValue()), value);
		}

		/**
		 * Adds a translation for an {@link EntityAttribute}.
		 *
		 * @param entityAttribute The {@link EntityAttribute} to get the translation key from.
		 * @param value           The value of the entry.
		 */
		default void add(RegistryEntry<EntityAttribute> entityAttribute, String value) {
			add(entityAttribute.value().getTranslationKey(), value);
		}

		/**
		 * Adds a translation for a {@link StatType}.
		 *
		 * @param statType The {@link StatType} to get the translation key from.
		 * @param value    The value of the entry.
		 */
		default void add(StatType<?> statType, String value) {
			add("stat_type." + Registries.STAT_TYPE.getId(statType).toString().replace(':', '.'), value);
		}

		/**
		 * Adds a translation for a {@link StatusEffect}.
		 *
		 * @param statusEffect The {@link StatusEffect} to get the translation key from.
		 * @param value        The value of the entry.
		 */
		default void add(StatusEffect statusEffect, String value) {
			add(statusEffect.getTranslationKey(), value);
		}

		/**
		 * Adds a translation for an {@link Identifier}.
		 *
		 * @param identifier The {@link Identifier} to get the translation key from.
		 * @param value      The value of the entry.
		 */
		default void add(Identifier identifier, String value) {
			add(identifier.toTranslationKey(), value);
		}

		/**
		 * Adds a translation for a {@link TagKey}.
		 *
		 * @param tagKey the {@link TagKey} to get the translation key from
		 * @param value  the value of the entry
		 */
		default void add(TagKey<?> tagKey, String value) {
			add(tagKey.getTranslationKey(), value);
		}

		/**
		 * Merges an existing language file into the generated language file.
		 *
		 * @param existingLanguageFile The path to the existing language file.
		 * @throws IOException If loading the language file failed.
		 */
		default void add(Path existingLanguageFile) throws IOException {
			try (Reader reader = Files.newBufferedReader(existingLanguageFile)) {
				JsonObject translations = StrictJsonParser.parse(reader).getAsJsonObject();

				for (String key : translations.keySet()) {
					add(key, translations.get(key).getAsString());
				}
			}
		}
	}
}
