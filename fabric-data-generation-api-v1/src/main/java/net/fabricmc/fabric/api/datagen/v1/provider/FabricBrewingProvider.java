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

package net.fabricmc.fabric.api.datagen.v1.provider;

import net.minecraft.data.recipes.BrewingProvider;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.world.item.Items;

public abstract class FabricBrewingProvider extends BrewingProvider {
	protected FabricBrewingProvider(RecipeOutput output) {
		super(output);
	}

	@Override
	protected void addContainers() {
		this.addContainer(Items.LINGERING_POTION);
		this.addContainer(Items.POTION);
		this.addContainer(Items.SPLASH_POTION);
	}

	@Override
	protected void addContainerTransformations() {
		this.addContainerTransformation(Items.POTION, Items.GUNPOWDER, Items.SPLASH_POTION);
		this.addContainerTransformation(Items.SPLASH_POTION, Items.DRAGON_BREATH, Items.LINGERING_POTION);
	}
}
