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

package net.fabricmc.fabric.mixin.datagen;

import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.data.DataProvider;

import net.fabricmc.fabric.api.resource.conditions.v1.ResourceConditions;
import net.fabricmc.fabric.impl.recipe.ingredient.CustomIngredientImpl;

@Mixin(DataProvider.class)
public interface DataProviderMixin {
	/**
	 * Adjust the default sort order of some keys provided by Fabric API.
	 */
	@Inject(method = "lambda$static$0", at = @At("RETURN"))
	private static void addFabricKeySortOrders(Object2IntOpenHashMap<String> map, CallbackInfo ci) {
		map.put(ResourceConditions.CONDITIONS_KEY, -100); // always at the beginning
		map.put(CustomIngredientImpl.TYPE_KEY, 0); // mimic vanilla "type"
	}
}
