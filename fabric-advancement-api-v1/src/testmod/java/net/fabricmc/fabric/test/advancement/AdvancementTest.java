package net.fabricmc.fabric.test.advancement;

import java.util.Optional;

import net.fabricmc.fabric.api.advancement.v1.AdvancementEvents;
import net.fabricmc.fabric.api.advancement.v1.AdvancementSource;
import net.fabricmc.fabric.api.advancement.v1.FabricAdvancementBuilder;

import net.minecraft.advancements.AdvancementRewards;
import net.minecraft.advancements.AdvancementType;
import net.minecraft.advancements.DisplayInfo;
import net.minecraft.advancements.triggers.InventoryChangeTrigger;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Items;

import net.fabricmc.api.ModInitializer;

public class AdvancementTest implements ModInitializer {
	private static final Identifier TACTICAL_FISHING = Identifier.withDefaultNamespace("husbandry/tactical_fishing");
	private static final Identifier RECIPE_BOOK = Identifier.withDefaultNamespace("recipes/root");

	@Override
	public void onInitialize() {
		// Test advancement replace event
		// The Advancement.Builder here should use FabricAdvancementBuilder#copyOf
		// to test the injected copy helper, similarly to how LootTest rebuilds pools/tables.
		AdvancementEvents.REPLACE.register((id, original, source, registries) -> {
			if (RECIPE_BOOK.equals(id)) {
				if (source != AdvancementSource.VANILLA) {
					throw new AssertionError("recipes/root advancement should have AdvancementSource.VANILLA, got " + source);
				}

				// Replace the recipe book root advancement's rewards with some experience,
				// leaving everything else untouched.
				return FabricAdvancementBuilder.copyOf(original)
						.rewards(AdvancementRewards.Builder.experience(1).build())
						.build(id)
						.value();
			}

			return null;
		});

		// Test that the event is stopped when the advancement is replaced
		AdvancementEvents.REPLACE.register((id, original, source, registries) -> {
			if (RECIPE_BOOK.equals(id)) {
				throw new AssertionError("Event should have been stopped from replaced advancement");
			}

			return null;
		});

		// Test advancement modify event
		AdvancementEvents.MODIFY.register((id, builder, source, registries) -> {
			if (TACTICAL_FISHING.equals(id)) {
				if (source != AdvancementSource.VANILLA) {
					throw new AssertionError("tactical_fishing advancement should have AdvancementSource.VANILLA, got " + source);
				}

				// Sanity check the getters before mutating anything
				Optional<DisplayInfo> display = builder.getDisplay();

				if (display.isEmpty()) {
					throw new AssertionError("tactical_fishing advancement should have display info");
				}

				if (!builder.getCriteria().containsKey("pufferfish_bucket")) {
					throw new AssertionError("tactical_fishing advancement should originally have a pufferfish_bucket criterion");
				}

				// Remove the vanilla criterion and add our own
				builder.removeCriterion("pufferfish_bucket");
				builder.addCriterion("stone_pickaxe", InventoryChangeTrigger.TriggerInstance.hasItems(Items.STONE_PICKAXE));

				if (builder.getCriteria().containsKey("pufferfish_bucket")) {
					throw new AssertionError("pufferfish_bucket criterion should have been removed from getCriteria()");
				}

				if (!builder.getCriteria().containsKey("stone_pickaxe")) {
					throw new AssertionError("stone_pickaxe criterion should have been added");
				}

				// Require the new criterion without touching other configured requirements
				builder.requireCriterion("stone_pickaxe");

				// Swap the display but keep the rest of the original display's properties
				DisplayInfo originalDisplay = display.get();
				builder.display(Items.DIAMOND_SWORD,
						Component.literal("Tactical Warrior"),
						originalDisplay.getDescription(),
						null,
						AdvancementType.TASK,
						originalDisplay.shouldShowToast(),
						originalDisplay.shouldAnnounceChat(),
						originalDisplay.isHidden());

				builder.rewards(AdvancementRewards.Builder.experience(50).build());
				builder.sendsTelemetryEvent();
			}

			// We modify the recipe book advancement's criteria in the test mod resources.
			if (RECIPE_BOOK.equals(id) && source != AdvancementSource.REPLACED) {
				throw new AssertionError("recipes/root advancement should have AdvancementSource.REPLACED, got " + source);
			}
		});

		// Test advancement all loaded event
		AdvancementEvents.ALL_LOADED.register((manager, advancements, registries) -> {
			if (advancements.isEmpty()) {
				throw new AssertionError("advancements map should not be empty");
			}

			if (!advancements.containsKey(TACTICAL_FISHING)) {
				throw new AssertionError("tactical_fishing advancement should exist in the loaded map");
			}

			var tacticalFishing = advancements.get(TACTICAL_FISHING);

			if (tacticalFishing.criteria().containsKey("pufferfish_bucket")) {
				throw new AssertionError("pufferfish_bucket criterion should not be present on the loaded advancement");
			}

			if (!tacticalFishing.criteria().containsKey("stone_pickaxe")) {
				throw new AssertionError("stone_pickaxe criterion should be present on the loaded advancement");
			}

			if (!tacticalFishing.sendsTelemetryEvent()) {
				throw new AssertionError("tactical_fishing advancement should send telemetry events after modification");
			}
		});
	}
}
