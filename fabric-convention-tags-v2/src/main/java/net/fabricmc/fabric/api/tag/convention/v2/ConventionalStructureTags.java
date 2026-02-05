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

package net.fabricmc.fabric.api.tag.convention.v2;

import net.minecraft.tags.TagKey;
import net.minecraft.world.level.levelgen.structure.Structure;

import net.fabricmc.fabric.impl.tag.convention.v2.TagRegistration;

/**
 * See {@link net.minecraft.tags.StructureTags} for vanilla tags.
 */
public final class ConventionalStructureTags {
	private ConventionalStructureTags() {
	}

	/**
	 * Structures that should not show up on minimaps or world map views from mods/sites.
	 * No effect on vanilla map items.
	 */
	public static final TagKey<Structure> HIDDEN_FROM_DISPLAYERS = register("hidden_from_displayers");

	/**
	 * Structures that should not be locatable/selectable by modded structure-locating items or abilities.
	 * No effect on vanilla map items.
	 */
	public static final TagKey<Structure> HIDDEN_FROM_LOCATOR_SELECTION = register("hidden_from_locator_selection");

	private static TagKey<Structure> register(String tagId) {
		return TagRegistration.STRUCTURE_TAG.registerC(tagId);
	}
}
