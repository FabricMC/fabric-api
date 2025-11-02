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

package net.fabricmc.fabric.mixin.recipe.client.sync;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import net.minecraft.client.recipebook.ClientRecipeManager;

import net.fabricmc.fabric.api.recipe.v1.FabricRecipeManager;
import net.fabricmc.fabric.api.recipe.v1.sync.SynchronizedRecipes;
import net.fabricmc.fabric.impl.recipe.sync.SynchronizedRecipesImpl;
import net.fabricmc.fabric.impl.recipe.sync.client.SynchronizedClientRecipesSetter;

@Mixin(ClientRecipeManager.class)
public class ClientRecipeManagerMixin implements FabricRecipeManager, SynchronizedClientRecipesSetter {
	@Unique
	private SynchronizedRecipes synchronizedClientRecipes = SynchronizedRecipesImpl.EMPTY;

	@Override
	public SynchronizedRecipes getSynchronizedRecipes() {
		return this.synchronizedClientRecipes;
	}

	@Override
	public void fabric_setSynchronizedClientRecipes(SynchronizedRecipes recipes) {
		this.synchronizedClientRecipes = recipes;
	}
}
