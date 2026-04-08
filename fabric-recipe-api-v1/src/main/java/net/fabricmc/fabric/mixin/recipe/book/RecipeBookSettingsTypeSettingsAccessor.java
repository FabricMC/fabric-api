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

package net.fabricmc.fabric.mixin.recipe.book;

import com.mojang.serialization.MapCodec;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import net.minecraft.stats.RecipeBookSettings;

@Mixin(RecipeBookSettings.TypeSettings.class)
public interface RecipeBookSettingsTypeSettingsAccessor {
	@Invoker("codec")
	static MapCodec<RecipeBookSettings.TypeSettings> invokeCodec(String openFieldName, String filteringFieldName) {
		throw new RuntimeException("Implemented via mixin");
	}
}
