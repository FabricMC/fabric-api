package net.fabricmc.fabric.api.advancement.event.v1;

import net.minecraft.advancements.Advancement;

import net.minecraft.advancements.AdvancementRequirements;
import net.minecraft.advancements.AdvancementRewards;
import net.minecraft.advancements.DisplayInfo;
import net.minecraft.advancements.triggers.Criterion;

import net.minecraft.resources.Identifier;

import org.jetbrains.annotations.ApiStatus;

import java.util.List;
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

	default boolean isSendsTelemetryEvent() {
		throw new UnsupportedOperationException("Implemented via mixin");
	}

	/**
	 * Marks a single criterion as required to obtain the advancement, on top of
	 * whatever requirements are already configured on this builder.
	 *
	 * <p>This appends a new requirement group containing just {@code name}, which is
	 * AND'd together with any existing requirement groups. Unlike setting the whole
	 * builder's {@link AdvancementRequirements.Strategy}, this does not touch or loosen
	 * any previously configured requirement.
	 *
	 * @param name the name of the criterion to require
	 */
	default void requireCriterion(String name) {
		throw new UnsupportedOperationException("Implemented via mixin");
	}

	/**
	 * Marks a group of criteria as required to obtain the advancement, where satisfying
	 * <b>any one</b> of {@code names} fulfills this requirement, on top of whatever
	 * requirements are already configured on this builder.
	 *
	 * <p>This appends a new requirement group containing {@code names}, which is AND'd
	 * together with any existing requirement groups. Unlike setting the whole builder's
	 * {@link AdvancementRequirements.Strategy}, this does not touch or loosen any
	 * previously configured requirement.
	 *
	 * @param names the names of the criteria, any one of which fulfills this requirement group
	 */
	default void requireCriteria(List<String> names) {
		throw new UnsupportedOperationException("Implemented via mixin");
	}

	static Advancement.Builder copyOf(Advancement advancement) {
		Advancement.Builder advancementBuilder = new Advancement.Builder();
		advancement.parent().ifPresent(advancementBuilder::parent);
		advancement.criteria().forEach(advancementBuilder::addCriterion);
		advancementBuilder.requirements(advancement.requirements());
		advancementBuilder.rewards(advancement.rewards());
		advancement.display().ifPresent(advancementBuilder::display);
		if (advancement.sendsTelemetryEvent()) {
			advancementBuilder.sendsTelemetryEvent();
		}
		return advancementBuilder;
	}
}
