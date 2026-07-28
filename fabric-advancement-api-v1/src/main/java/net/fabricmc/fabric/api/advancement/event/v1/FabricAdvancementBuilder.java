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

@ApiStatus.NonExtendable
public interface FabricAdvancementBuilder {
	default void removeCriterion(String name) {
		throw new UnsupportedOperationException("Implemented via mixin");
	}

	default Map<String, Criterion<?>> getCriteria() {
		throw new UnsupportedOperationException("Implemented via mixin");
	}

	default Optional<Identifier> getParent() {
		throw new UnsupportedOperationException("Implemented via mixin");
	}

	default Optional<DisplayInfo> getDisplay() {
		throw new UnsupportedOperationException("Implemented via mixin");
	}

	default AdvancementRewards getRewards() {
		throw new UnsupportedOperationException("Implemented via mixin");
	}

	default AdvancementRequirements getRequirements() {
		throw new UnsupportedOperationException("Implemented via mixin");
	}

	default AdvancementRequirements.Strategy getRequirementsStrategy() {
		throw new UnsupportedOperationException("Implemented via mixin");
	}

	default boolean sendsTelemetryEvent() {
		throw new UnsupportedOperationException("Implemented via mixin");
	}

	static Advancement.Builder copyOf(Advancement advancement) {
		Advancement.Builder advancementBuilder = new Advancement.Builder();
		advancement.parent().ifPresent(advancementBuilder::parent);
		advancement.criteria().forEach(advancementBuilder::addCriterion);
		advancementBuilder.rewards(advancement.rewards());
		advancement.display().ifPresent(advancementBuilder::display);
		if (advancement.sendsTelemetryEvent()) {
			advancementBuilder.sendsTelemetryEvent();
		}
		return advancementBuilder;
	}
}
