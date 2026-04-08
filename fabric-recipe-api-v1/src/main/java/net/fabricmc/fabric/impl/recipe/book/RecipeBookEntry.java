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

package net.fabricmc.fabric.impl.recipe.book;

import com.mojang.serialization.MapCodec;

import net.minecraft.resources.Identifier;
import net.minecraft.stats.RecipeBookSettings;

import net.fabricmc.fabric.mixin.recipe.book.RecipeBookSettingsTypeSettingsAccessor;
import net.fabricmc.loader.impl.util.StringUtil;

public record RecipeBookEntry(Identifier id, MapCodec<RecipeBookSettings.TypeSettings> typeSettingsCodec) {
	public RecipeBookEntry(Identifier id) {
		this(
				id,
				RecipeBookSettingsTypeSettingsAccessor.invokeCodec(
						"is" + toFieldName(id.getNamespace()) + toFieldName(id.getPath()) + "GuiOpen",
						"is" + toFieldName(id.getNamespace()) + toFieldName(id.getPath()) + "FilteringCraftable"
				)
		);
	}

	private static String toFieldName(String value) {
		StringBuilder builder = new StringBuilder();

		for (String split : value.split("[.\\-_]")) {
			builder.append(StringUtil.capitalize(split));
		}

		return builder.toString();
	}
}
