package net.fabricmc.fabric.api.advancement.event.v1;

import net.minecraft.advancements.Advancement;

import net.minecraft.advancements.AdvancementRequirements;
import net.minecraft.advancements.AdvancementRewards;
import net.minecraft.advancements.DisplayInfo;
import net.minecraft.advancements.triggers.Criterion;

import net.minecraft.resources.Identifier;

import org.jetbrains.annotations.ApiStatus;

import java.util.Map;
import java.util.Optional;

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

	// Accessors for internal fields
	Optional<Identifier> getParent();
	Optional<DisplayInfo> getDisplay();
	AdvancementRewards getRewards();
	Optional<AdvancementRequirements> getRequirements();
	boolean getSendsTelemetryEvent();

	default void removeCriterion(String name) {
		this.fabric_removeCriterion(name);
	}

	default Map<String, Criterion<?>> getCriteria() {
		return this.fabric_getCriteria();
	}

	void fabric_removeCriterion(String name);
	Map<String, Criterion<?>> fabric_getCriteria();
}
