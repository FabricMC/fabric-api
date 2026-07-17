package net.fabricmc.fabric.test.advancement.event;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.advancement.event.v1.AdvancementEvents;

import net.minecraft.advancements.triggers.InventoryChangeTrigger;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Items;

public class AdvancementEventTest implements ModInitializer {
	@Override
	public void onInitialize() {
		AdvancementEvents.MODIFY.register((mapWrapper) -> {
			Identifier targetId = Identifier.withDefaultNamespace("husbandry/tactical_fishing");

			mapWrapper.modify(targetId, builder -> {
				builder.addCriterion("stone_pickaxe", InventoryChangeTrigger.TriggerInstance.hasItems(Items.STONE_PICKAXE));
			});
		});
	}
}
