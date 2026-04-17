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
import net.minecraft.world.item.alchemy.Potion;

import net.fabricmc.fabric.impl.tag.convention.v2.TagRegistration;

public final class ConventionalPotionTags {
	private ConventionalPotionTags() {
	}

	/**
	 * Tag that holds all enchantments that recipe viewers should not show to users.
	 * Recipe viewers may use this to automatically find the corresponding Potion items to hide.
	 */
	public static final TagKey<Potion> HIDDEN_FROM_RECIPE_VIEWERS = register("hidden_from_recipe_viewers");

	private static TagKey<Potion> register(String tagId) {
		return TagRegistration.POTION_TAG.registerC(tagId);
	}
}
