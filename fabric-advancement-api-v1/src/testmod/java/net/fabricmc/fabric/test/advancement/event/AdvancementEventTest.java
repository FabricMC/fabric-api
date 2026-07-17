package net.fabricmc.fabric.test.advancement.event;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.advancement.event.v1.AdvancementEvents;

import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementRequirements;
import net.minecraft.advancements.AdvancementType;
import net.minecraft.advancements.triggers.InventoryChangeTrigger;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Items;

public class AdvancementEventTest implements ModInitializer {
	@Override
	public void onInitialize() {
		AdvancementEvents.MODIFY.register((identifier, builder, source, registries) -> {
			if(identifier.equals(Identifier.withDefaultNamespace("husbandry/tactical_fishing"))) {
				// Remove the criteria added in the REPLACE event for testing, can be any criteria of husbandry/tactical_fishing
				builder.removeCriterion("diamond_axe");

				// Adding own criteria, which triggers when having a stone pickaxe
				builder.addCriterion("stone_pickaxe", InventoryChangeTrigger.TriggerInstance.hasItems(Items.STONE_PICKAXE));
				// Setting the requirements to at least one criteria needed
				builder.requirements(AdvancementRequirements.Strategy.OR);
			}
		});

		AdvancementEvents.REPLACE.register((identifier, original, source, registries) -> {
			if(identifier.equals(Identifier.withDefaultNamespace("husbandry/tactical_fishing"))) {
				return Advancement.Builder.advancement()
						.display(
								Items.SALMON,
								Component.literal("Testing"),
								Component.literal("This is a custom advancement"),
								null,
								AdvancementType.GOAL,
								true,
								true,
								false
						)
						// Add a diamond_axe criterion
						.addCriterion("diamond_axe", InventoryChangeTrigger.TriggerInstance.hasItems(Items.DIAMOND_AXE))
						.requirements(AdvancementRequirements.Strategy.AND)
						// Build returns the AdvancementHolder, .value() gives the Advancement
						.build(identifier).value();
			}
			return original;
		});

		AdvancementEvents.ALL_LOADED.register((resourceManager, advancements, registries) -> {
			// Do something when all advancements are loaded
		});
	}
}
