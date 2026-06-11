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

package net.fabricmc.fabric.test.holder.component;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.minecraft.core.component.DataComponents;
import net.minecraft.references.BlockIds;
import net.minecraft.references.ItemIds;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.animal.pig.PigVariants;
import net.minecraft.world.item.DyeColor;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.holder.component.v1.FabricDataComponentInitializers;

public class HolderComponentTestModEntrypoint implements ModInitializer {
	public static final String MODID = "fabric-holder-component-api-v1-testmod";
	private static final Logger LOGGER = LoggerFactory.getLogger(MODID);

	@Override
	public void onInitialize() {
		CommandRegistrationCallback.EVENT.register((dispatcher, buildContext, selection) -> {
			HolderComponentCommand.register(dispatcher, buildContext);
		});

		final Identifier first = Identifier.fromNamespaceAndPath("fabric", "first");
		final Identifier second = Identifier.fromNamespaceAndPath("fabric", "second");
		final Identifier third = Identifier.fromNamespaceAndPath("fabric", "third");

		FabricDataComponentInitializers.registerInitializer(third, context -> {
			LOGGER.info("I should be third!");
			// Blue Shulker Color value will be overwritten if this initializer isn't third.
			context.builder(BlockIds.DIRT).set(DataComponents.SHULKER_COLOR, DyeColor.BLUE);
		});
		FabricDataComponentInitializers.registerInitializer(first, context -> {
			LOGGER.info("I should be first!");
			// Red Sheep Color and Shulker Color should be overwritten if this initializer is first.
			// Red Cat Color is set to verify that this initializer ran in the first place.
			context.builder(BlockIds.DIRT)
					.set(DataComponents.CAT_COLLAR, DyeColor.RED)
					.set(DataComponents.SHEEP_COLOR, DyeColor.RED)
					.set(DataComponents.SHULKER_COLOR, DyeColor.RED);
		});
		FabricDataComponentInitializers.registerInitializer(second, context -> {
			LOGGER.info("I should be second!");
			// Green Sheep Color will be overwritten if this initializer isn't second.
			context.builder(BlockIds.DIRT)
					.set(DataComponents.SHEEP_COLOR, DyeColor.GREEN)
					.set(DataComponents.SHULKER_COLOR, DyeColor.GREEN);
		});

		FabricDataComponentInitializers.addInitializerOrdering(first, second);
		FabricDataComponentInitializers.addInitializerOrdering(second, third);

		final Identifier melonSeeds = Identifier.fromNamespaceAndPath("fabric", "melon_seeds");
		FabricDataComponentInitializers.registerInitializer(melonSeeds, context ->
				context.builder(ItemIds.MELON_SEEDS)
						.set(DataComponents.PIG_VARIANT, context.lookupProvider().getOrThrow(PigVariants.TEMPERATE))
						.set(DataComponents.ITEM_NAME, null)
		);
	}
}
