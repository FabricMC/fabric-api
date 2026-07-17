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
		AdvancementEvents.MODIFY.register((identifier, builder, source) -> {
			if(identifier.equals(Identifier.withDefaultNamespace("husbandry/tactical_fishing"))) {
				builder.addCriterion("stone_pickaxe", InventoryChangeTrigger.TriggerInstance.hasItems(Items.STONE_PICKAXE));
				builder.requirements(AdvancementRequirements.Strategy.OR);
			}
		});

		AdvancementEvents.REPLACE.register((identifier, original, source) -> {
			if(identifier.equals(Identifier.withDefaultNamespace("husbandry/tactical_fishing"))) {
				return Advancement.Builder.advancement()
						.display(
								Items.SALMON,                                     // Icon
								Component.literal("Testing"),                     // Title
								Component.literal("This is a custom advancement"),// Description
								null,                                             // Background (optional)
								AdvancementType.GOAL,                             // Frame
								true,                                             // Show toast
								true,                                             // Announce chat
								false                                             // Hidden
						)
						// Add a dummy criterion so it's valid
						.addCriterion("diamond_axe", InventoryChangeTrigger.TriggerInstance.hasItems(Items.DIAMOND_AXE))
						.requirements(AdvancementRequirements.Strategy.AND)
						// Build returns the Holder, .value() gives you the Advancement record needed
						.build(identifier).value();
			}
			return original;
		});

		AdvancementEvents.ALL_LOADED.register((resourceManager, advancements) -> {
			// Do something when all advancements are loaded
		});
	}
}
