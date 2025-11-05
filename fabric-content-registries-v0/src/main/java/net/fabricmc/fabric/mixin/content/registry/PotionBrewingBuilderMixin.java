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

package net.fabricmc.fabric.mixin.content.registry;

import java.util.List;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.core.Holder;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionBrewing;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.crafting.Ingredient;

import net.fabricmc.fabric.api.registry.FabricBrewingRecipeRegistryBuilder;

@Mixin(PotionBrewing.Builder.class)
public abstract class PotionBrewingBuilderMixin implements FabricBrewingRecipeRegistryBuilder {
	@Shadow
	@Final
	private FeatureFlagSet enabledFeatures;

	@Shadow
	private static void expectPotion(Item potionType) {
	}

	@Shadow
	@Final
	private List<PotionBrewing.Mix<Item>> containerMixes;

	@Shadow
	@Final
	private List<PotionBrewing.Mix<Potion>> potionMixes;

	@Inject(method = "build", at = @At("HEAD"))
	private void build(CallbackInfoReturnable<PotionBrewing> cir) {
		FabricBrewingRecipeRegistryBuilder.BUILD.invoker().build((PotionBrewing.Builder) (Object) this);
	}

	@Override
	public void registerItemRecipe(Item input, Ingredient ingredient, Item output) {
		if (input.isEnabled(this.enabledFeatures) && output.isEnabled(this.enabledFeatures)) {
			expectPotion(input);
			expectPotion(output);
			this.containerMixes.add(new PotionBrewing.Mix<>(input.builtInRegistryHolder(), ingredient, output.builtInRegistryHolder()));
		}
	}

	@Override
	public void registerPotionRecipe(Holder<Potion> input, Ingredient ingredient, Holder<Potion> output) {
		if (input.value().isEnabled(this.enabledFeatures) && output.value().isEnabled(this.enabledFeatures)) {
			this.potionMixes.add(new PotionBrewing.Mix<>(input, ingredient, output));
		}
	}

	@Override
	public void registerRecipes(Ingredient ingredient, Holder<Potion> potion) {
		if (potion.value().isEnabled(this.enabledFeatures)) {
			this.registerPotionRecipe(Potions.WATER, ingredient, Potions.MUNDANE);
			this.registerPotionRecipe(Potions.AWKWARD, ingredient, potion);
		}
	}

	@Override
	public FeatureFlagSet getEnabledFeatures() {
		return this.enabledFeatures;
	}
}
