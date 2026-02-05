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

package net.fabricmc.fabric.impl.tag.convention.v2;

import java.util.List;
import java.util.Locale;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.locale.Language;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.loader.api.FabricLoader;

public class TranslationConventionLogWarnings implements ModInitializer {
	private static final Logger LOGGER = LoggerFactory.getLogger(TranslationConventionLogWarnings.class);

	/**
	 * A config option mainly for developers.
	 * Logs out modded item tags that do not have translations when running on integrated server.
	 * Defaults to SHORT.
	 */
	private static final LogWarningMode LOG_UNTRANSLATED_WARNING_MODE = setupLogWarningModeProperty();

	private static LogWarningMode setupLogWarningModeProperty() {
		final LogWarningMode defaultMode = FabricLoader.getInstance().isDevelopmentEnvironment() ? LogWarningMode.SHORT : LogWarningMode.SILENCED;
		String property = System.getProperty("fabric-tag-conventions-v2.missingTagTranslationWarning", defaultMode.name()).toUpperCase(Locale.ROOT);

		try {
			return LogWarningMode.valueOf(property);
		} catch (Exception e) {
			LOGGER.error("Unknown entry `{}` for property `fabric-tag-conventions-v2.missingTagTranslationWarning`.", property);
			return LogWarningMode.SILENCED;
		}
	}

	private enum LogWarningMode {
		SILENCED,
		SHORT,
		VERBOSE,
		FAIL;

		boolean verbose() {
			return this == VERBOSE || this == FAIL;
		}
	}

	public void onInitialize() {
		if (LOG_UNTRANSLATED_WARNING_MODE != LogWarningMode.SILENCED) {
			setupUntranslatedItemTagWarning();
		}
	}

	private static void setupUntranslatedItemTagWarning() {
		// Log missing item tag translations only when world is started.
		ServerLifecycleEvents.SERVER_STARTED.register(server -> {
			Language language = Language.getInstance();
			Registry<Item> itemRegistry = server.registryAccess().lookupOrThrow(Registries.ITEM);
			List<TagKey<Item>> untranslatedItemTags = new ObjectArrayList<>();
			itemRegistry.getTags().forEach(itemTagKey -> {
				// We do not translate vanilla's tags at this moment.
				if (itemTagKey.key().location().getNamespace().equals(Identifier.DEFAULT_NAMESPACE)) {
					return;
				}

				if (!language.has(itemTagKey.key().getTranslationKey())) {
					untranslatedItemTags.add(itemTagKey.key());
				}
			});

			if (untranslatedItemTags.isEmpty()) {
				return;
			}

			StringBuilder stringBuilder = new StringBuilder();
			stringBuilder.append("""
					\n	Dev warning - Untranslated Item Tags detected. Please translate your item tags so other mods such as recipe viewers can properly display your tag's name.
						The format desired is tag.item.<namespace>.<path> for the translation key with slashes in path turned into periods.
						To disable this message, set this system property in your runs: `-Dfabric-tag-conventions-v2.missingTagTranslationWarning=SILENCED`.
						To see individual untranslated item tags found, set the system property to `-Dfabric-tag-conventions-v2.missingTagTranslationWarning=VERBOSE`.
						Default is `SHORT`.
					""");

			// Print out all untranslated tags when desired.
			if (LOG_UNTRANSLATED_WARNING_MODE.verbose()) {
				stringBuilder.append("\nUntranslated item tags:");

				for (TagKey<Item> tagKey : untranslatedItemTags) {
					stringBuilder.append("\n     ").append(tagKey.location());
				}
			}

			LOGGER.warn(stringBuilder.toString());

			if (LOG_UNTRANSLATED_WARNING_MODE == LogWarningMode.FAIL) {
				throw new RuntimeException("Tag translation validation failed");
			}
		});
	}
}
