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

package net.fabricmc.fabric.api.gamerule.v1;

import java.util.Optional;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.gamerules.GameRule;

import net.fabricmc.fabric.impl.gamerule.RuleCategoryExtensions;

/**
 * Utility class for creating custom game rule categories with full control over the name.
 *
 * @see net.minecraft.world.level.gamerules.GameRuleCategory
 */
@SuppressWarnings("ClassCanBeRecord")
public final class CustomGameRuleCategory {
	private final Identifier id;
	private final Component name;

	/**
	 * Creates a custom game rule category.
	 *
	 * @param id   the id of this category
	 * @param name the name of this category
	 */
	public CustomGameRuleCategory(Identifier id, Component name) {
		this.id = id;
		this.name = name;
	}

	public Identifier getId() {
		return this.id;
	}

	public Component getName() {
		return this.name;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		CustomGameRuleCategory that = (CustomGameRuleCategory) o;

		return this.id.equals(that.id);
	}

	@Override
	public int hashCode() {
		return this.id.hashCode();
	}

	/**
	 * Gets the custom category a {@linkplain GameRule game rule} is registered to.
	 *
	 * @param rule the rule
	 * @param <T>  the type of value the rule holds
	 * @return the custom category this rule belongs to. Otherwise {@linkplain Optional#empty() empty}
	 */
	public static <T> Optional<CustomGameRuleCategory> getCategory(GameRule<T> rule) {
		return Optional.ofNullable(((RuleCategoryExtensions) (Object) rule).fabric_getCustomCategory());
	}
}
