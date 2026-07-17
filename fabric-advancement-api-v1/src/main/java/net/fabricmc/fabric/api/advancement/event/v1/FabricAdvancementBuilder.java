package net.fabricmc.fabric.api.advancement.event.v1;

import net.minecraft.advancements.Advancement;

import net.minecraft.advancements.AdvancementRequirements;

import org.jetbrains.annotations.ApiStatus;

// TODO: Same as FabricAdvancementHolder in datagen.v1.advancement
@ApiStatus.NonExtendable
public interface FabricAdvancementBuilder {
	static Advancement.Builder copyOf(Advancement advancement) {
		Advancement.Builder advancementBuilder = new Advancement.Builder();

		// TODO: Migrate from deprecated parent method to AdvancementHolder
		advancement.parent().ifPresent(advancementBuilder::parent);

		advancement.criteria().forEach(advancementBuilder::addCriterion);

		advancementBuilder.rewards(advancement.rewards());
		advancement.display().ifPresent(advancementBuilder::display);
		if(advancement.sendsTelemetryEvent()) {
			advancementBuilder.sendsTelemetryEvent();
		}

		return advancementBuilder;
	}
}
