package net.fabricmc.fabric.test.advancement.event;

import com.mojang.logging.LogUtils;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.advancement.event.v1.AdvancementEvents;

import net.minecraft.advancements.AdvancementRequirements;
import net.minecraft.advancements.AdvancementRewards;
import net.minecraft.advancements.AdvancementType;
import net.minecraft.advancements.DisplayInfo;
import net.minecraft.advancements.triggers.InventoryChangeTrigger;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Items;

import org.slf4j.Logger;

import java.util.Optional;

public class AdvancementEventTest implements ModInitializer {
	private static final Logger LOGGER = LogUtils.getLogger();

	@Override
	public void onInitialize() {
		// REPLACE EVENT
		// This event allows you to completely swap an existing advancement for a new one.
		AdvancementEvents.REPLACE.register((id, original, source, registries) -> {
			if (id.equals(Identifier.withDefaultNamespace("husbandry/tactical_fishing"))) {
				LOGGER.info("Replacing tactical_fishing advancement...");
				// You could return a completely new Advancement here if you wanted.
			}
			return null; // Return null to let the advancement load as normal
		});

		// MODIFY EVENT
		// This is the core event where you use the injected Interface methods.
		AdvancementEvents.MODIFY.register((id, builder, source, registries) -> {
			if (id.equals(Identifier.withDefaultNamespace("husbandry/tactical_fishing"))) {
				LOGGER.info("Modifying Tactical Fishing with source: " + source);

				// Test Getters (Verify data is accessible)
				Optional<DisplayInfo> display = builder.getDisplay();
				LOGGER.info("Original title: " + display.map(DisplayInfo::getTitle).orElse(Component.literal("None")));
				LOGGER.info("Original telemetry: " + builder.sendsTelemetryEvent());

				// Remove Criterion
				builder.removeCriterion("fishing_rod");

				// Add Criteria
				builder.addCriterion("stone_pickaxe", InventoryChangeTrigger.TriggerInstance.hasItems(Items.STONE_PICKAXE));

				// Update Requirements (Using the getter to get keys)
				builder.requirements(AdvancementRequirements.anyOf(builder.getCriteria().keySet()));

				// Modify Display (Change icon to a Diamond Sword)
				// Note: Please try keeping datas that you don't modify from the original DisplayInfo
				builder.display(Items.DIAMOND_SWORD,
						Component.literal("Tactical Warrior"),
						Component.literal("Caught a fish, or found a pickaxe!"),
						null,
						AdvancementType.TASK,
						true, true, false);

				// Modify Rewards
				builder.rewards(AdvancementRewards.Builder.experience(50).build());

				// Set Telemetry
				builder.sendsTelemetryEvent();
			}
		});

		// ALL_LOADED EVENT
		// Used for post-processing or validation
		AdvancementEvents.ALL_LOADED.register((manager, advancements, registries) -> {
			LOGGER.info("All advancements loaded! Total count: " + advancements.size());

			// Example: check if our modified advancement is actually present in the map
			Identifier tacticalFishingId = Identifier.withDefaultNamespace("husbandry/tactical_fishing");
			if (advancements.containsKey(tacticalFishingId)) {
				LOGGER.info("Tactical Fishing exists in registry!");
			}
		});
	}
}
