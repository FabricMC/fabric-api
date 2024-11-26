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

package net.fabricmc.fabric.api.command.v2;

import java.util.function.Predicate;

import net.minecraft.command.EntitySelectorOptions;
import net.minecraft.command.EntitySelectorReader;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import net.fabricmc.fabric.mixin.command.EntitySelectorOptionsAccessor;

/**
 * Contains a function to register an entity selector option.
 */
public final class EntitySelectorOptionRegistry {
	private EntitySelectorOptionRegistry() {
	}

	/**
	 * Registers an entity selector option. The added option is available under the underscore
	 * separated ID.
	 *
	 * <p>Here's an example of a custom entity selector option. The option is registered under
	 * {@code example_min_health} and can be used like {@code @e[example_min_health=5]}.
	 * <pre>{@code
	 * EntitySelectorOptionRegistry.register(
	 * 	Identifier.of("example", "min_health"),
	 * 	Text.literal("Minimum entity health"),
	 * 	(reader) -> {
	 * 	    final float minHealth = reader.getReader().readFloat();
	 *
	 * 	    if (minHealth > 0) {
	 * 	        reader.addPredicate((entity) -> entity instanceof LivingEntity livingEntity && livingEntity.getHealth() >= minHealth);
	 * 	    }
	 * 	},
	 * 	(reader) -> true
	 * );
	 * }</pre>
	 *
	 * <p>By default, a selector option can be used multiple times. To make a non-repeatable
	 * option, either use {@link FabricEntitySelectorReader} to flag the existence of an option
	 * and check it inside {@code canUse}, or use {@link #registerNonRepeatable} instead of this
	 * method.
	 *
	 * @param id the ID of the option
	 * @param description the description of the option
	 * @param handler the handler for the entity option that reads and sets the predicate
	 * @param canUse the predicate that checks whether the option is syntactically valid
	 */
	public static void register(Identifier id, Text description, EntitySelectorOptions.SelectorHandler handler, Predicate<EntitySelectorReader> canUse) {
		EntitySelectorOptionsAccessor.callPutOption(id.toUnderscoreSeparatedString(), handler, canUse, description);
	}

	/**
	 * Registers an entity selector option. The added option is available under the underscore
	 * separated ID. The added option cannot be used multiple times within a single selector.
	 *
	 * @param id the ID of the option
	 * @param description the description of the option
	 * @param handler the handler for the entity option that reads and sets the predicate
	 */
	public static void registerNonRepeatable(Identifier id, Text description, EntitySelectorOptions.SelectorHandler handler) {
		register(id, description, (reader) -> {
			handler.handle(reader);
			reader.setCustomFlag(id, true);
		}, (reader) -> !reader.getCustomFlag(id)); // has a flag = used before
	}
}
