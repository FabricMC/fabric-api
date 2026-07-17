package net.fabricmc.fabric.test.advancement.event;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.advancement.event.v1.AdvancementEvents;

import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementRequirements;
import net.minecraft.advancements.triggers.Criterion;
import net.minecraft.advancements.triggers.InventoryChangeTrigger;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Items;

import java.util.HashMap;
import java.util.Map;

public class AdvancementEventTest implements ModInitializer {
	@Override
	public void onInitialize() {
		AdvancementEvents.MODIFY.register((advancementsMap) -> {
			Identifier targetId = Identifier.withDefaultNamespace("husbandry/tactical_fishing");
			Advancement original = advancementsMap.get(targetId);

			if (original != null) {
				// Create a new criterion map based on the original one (or remove original.criteria() to replace existing criterions)
				Map<String, Criterion<?>> newCriteria = new HashMap<>(original.criteria());

				// Add new criterion
				newCriteria.put("stone_pickaxe", InventoryChangeTrigger.TriggerInstance.hasItems(Items.STONE_PICKAXE));

				// Any of the actual criterions validates the advancement
				AdvancementRequirements newRequirements = AdvancementRequirements.anyOf(newCriteria.keySet());

				// Build the advancement
				Advancement modifiedAdvancement = new Advancement(
						original.parent(),
						original.display(),
						original.rewards(),
						newCriteria,
						newRequirements,
						original.sendsTelemetryEvent()
				);

				advancementsMap.put(targetId, modifiedAdvancement);
			}
		});
	}
}
