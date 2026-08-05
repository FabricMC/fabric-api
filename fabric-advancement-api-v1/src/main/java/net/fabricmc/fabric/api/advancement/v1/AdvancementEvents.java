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

package net.fabricmc.fabric.api.advancement.v1;

import java.util.Map;

import org.jspecify.annotations.Nullable;

import net.minecraft.advancements.Advancement;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;

/**
 * Events for manipulating advancements.
 */
public final class AdvancementEvents {
	private AdvancementEvents() {
	}

	/**
	 * This event can be used to replace advancements.
	 * If an advancement is replaced, its source will be marked as {@link AdvancementSource#REPLACED}.
	 */
	public static final Event<Replace> REPLACE = EventFactory.createArrayBacked(Replace.class, listeners -> (id, original, source, registries) -> {
		for (Replace listener : listeners) {
			@Nullable Advancement replaced = listener.replaceAdvancement(id, original, source, registries);

			if (replaced != null) {
				return replaced;
			}
		}

		return null;
	});

	/**
	 * This event can be used to modify advancements.
	 * The main use case is to add new criteria to vanilla or mod advancements (e.g. modded lava bucket to "Hot Stuff").
	 *
	 * <p>You can also modify advancements that are created by {@link #REPLACE}.
	 * They have the advancement source {@link AdvancementSource#REPLACED}.
	 *
	 * <h4>Example: adding a stone pickaxe criterion to tactical fishing</h4>
	 *
	 * <p>WARNING: When adding a new criterion, you must explicitly update the advancement requirements
	 * if you want the new criterion to trigger the advancement on its own. By default, the builder retains the original requirements.
	 *
	 * {@snippet :
	 * // Name of the advancement to modify
	 * Identifier advancementId = Identifier.withDefaultNamespace("husbandry/tactical_fishing");
	 * AdvancementEvents.MODIFY.register((id, builder, source, registries) -> {
	 *     if (id.equals(advancementId)) {
	 *         // Add your criterion
	 *         builder.addCriterion("stone_pickaxe", InventoryChangeTrigger.TriggerInstance.hasItems(Items.STONE_PICKAXE));
	 *
	 *         // Require the new criterion in addition to whatever was already required,
	 *         // without loosening the existing requirements
	 *         builder.requireCriterion("stone_pickaxe");
	 *     }
	 * });
	 * }
	 */
	public static final Event<Modify> MODIFY = EventFactory.createArrayBacked(Modify.class, listeners -> (id, builder, source, registries) -> {
		for (Modify listener : listeners) {
			listener.modifyAdvancement(id, builder, source, registries);
		}
	});

	/**
	 * This event can be used for post-processing after all advancements have been loaded, replaced, and modified.
	 */
	public static final Event<Loaded> ALL_LOADED = EventFactory.createArrayBacked(Loaded.class, listeners -> (resourceManager, advancements, registries) -> {
		for (Loaded listener : listeners) {
			listener.onAdvancementsLoaded(resourceManager, advancements, registries);
		}
	});

	@FunctionalInterface
	public interface Replace {
		/**
		 * Replaces advancements.
		 *
		 * @param id        the advancement identifier
		 * @param original  the original advancement
		 * @param source    the source of the original advancement
		 * @return the new advancement, or null if it wasn't replaced
		 */
		@Nullable
		Advancement replaceAdvancement(Identifier id, Advancement original, AdvancementSource source, HolderLookup.Provider registries);
	}

	@FunctionalInterface
	public interface Modify {
		/**
		 * Called when an advancement is loading to modify advancements.
		 *
		 * @param id        the advancement identifier
		 * @param builder   a builder of the advancement being loaded
		 * @param source    the source of the advancement
		 */
		void modifyAdvancement(Identifier id, Advancement.Builder builder, AdvancementSource source, HolderLookup.Provider registries);
	}

	@FunctionalInterface
	public interface Loaded {
		/**
		 * Called when all advancements have been loaded and {@link AdvancementEvents#REPLACE} and {@link AdvancementEvents#MODIFY} have been invoked.
		 *
		 * @param resourceManager the server resource manager
		 * @param advancements    an immutable map of all loaded advancements
		 */
		void onAdvancementsLoaded(ResourceManager resourceManager, Map<Identifier, Advancement> advancements, HolderLookup.Provider registries);
	}
}
