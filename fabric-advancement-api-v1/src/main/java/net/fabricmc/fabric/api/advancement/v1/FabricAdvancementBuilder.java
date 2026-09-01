/*
 * Copyright (c) 2016, 2017, 2018, 2019 FabricMC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.fabricmc.fabric.api.advancement.v1;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.jetbrains.annotations.ApiStatus;

import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementRequirements;
import net.minecraft.advancements.AdvancementRewards;
import net.minecraft.advancements.DisplayInfo;
import net.minecraft.advancements.triggers.Criterion;
import net.minecraft.resources.Identifier;

/**
 * Convenience extensions to {@link Advancement.Builder} for reading the values already set on the
 * builder and for removing or requiring criteria.
 *
 * <p>This interface is automatically injected to {@link Advancement.Builder}.
 */
@ApiStatus.NonExtendable
public interface FabricAdvancementBuilder {
	/**
	 * Returns the criteria of this builder.
	 *
	 * <p>The returned map is an immutable copy; use {@link #setCriteria(Map)} to write modified
	 * criteria back to this builder.
	 *
	 * @return the criteria, keyed by their name
	 */
	default Map<String, Criterion<?>> getCriteria() {
		throw new UnsupportedOperationException("Implemented via mixin");
	}

	/**
	 * Replaces all criteria of this builder.
	 *
	 * <p>Requirements referring to criteria that are not in {@code criteria} are dropped,
	 * just like they are by {@link #removeCriterion(String)}.
	 *
	 * @param criteria the new criteria, keyed by their name
	 * @return this builder
	 */
	default Advancement.Builder setCriteria(Map<String, Criterion<?>> criteria) {
		throw new UnsupportedOperationException("Implemented via mixin");
	}

	/**
	 * Removes a criterion from this builder.
	 *
	 * <p>The criterion is also removed from the {@linkplain #getRequirements() requirements}.
	 * A requirement group that only contained the removed criterion is dropped entirely, which
	 * makes the remaining groups the only way to obtain the advancement.
	 *
	 * <p>The criterion can be added back with {@link Advancement.Builder#addCriterion(String, Criterion)}.
	 *
	 * @param name the name of the removed criterion
	 * @return this builder
	 */
	default Advancement.Builder removeCriterion(String name) {
		throw new UnsupportedOperationException("Implemented via mixin");
	}

	/**
	 * Makes a criterion required to obtain the advancement, in addition to the requirements
	 * already set on this builder.
	 *
	 * @param name the name of the required criterion
	 * @return this builder
	 */
	default Advancement.Builder requireCriterion(String name) {
		throw new UnsupportedOperationException("Implemented via mixin");
	}

	/**
	 * Makes a group of criteria required to obtain the advancement, in addition to the requirements
	 * already set on this builder. Completing any one of {@code names} fulfills the added group.
	 *
	 * @param names the names of the criteria of the added requirement group
	 * @return this builder
	 */
	default Advancement.Builder requireCriteria(List<String> names) {
		throw new UnsupportedOperationException("Implemented via mixin");
	}

	/**
	 * Returns the requirements of this builder, or {@link AdvancementRequirements#EMPTY}
	 * if none have been set.
	 *
	 * @return the requirements
	 */
	default AdvancementRequirements getRequirements() {
		throw new UnsupportedOperationException("Implemented via mixin");
	}

	/**
	 * Returns the identifier of the parent advancement of this builder.
	 *
	 * @return the parent identifier, or an empty optional if this is a root advancement
	 */
	default Optional<Identifier> getParent() {
		throw new UnsupportedOperationException("Implemented via mixin");
	}

	/**
	 * Returns the display info of this builder.
	 *
	 * @return the display info, or an empty optional if the advancement is not displayed
	 */
	default Optional<DisplayInfo> getDisplay() {
		throw new UnsupportedOperationException("Implemented via mixin");
	}

	/**
	 * Returns the rewards granted when the advancement is obtained.
	 *
	 * @return the rewards
	 */
	default AdvancementRewards getRewards() {
		throw new UnsupportedOperationException("Implemented via mixin");
	}

	/**
	 * Returns whether the advancement sends a telemetry event when it is obtained.
	 *
	 * @return {@code true} if a telemetry event is sent, {@code false} otherwise
	 */
	default boolean isSendsTelemetryEvent() {
		throw new UnsupportedOperationException("Implemented via mixin");
	}

	/**
	 * Creates a builder copy of an advancement.
	 *
	 * @param advancement the advancement
	 * @return the copied builder
	 */
	// Advancement only stores the identifier of its parent, so the deprecated overload is the only one usable here.
	@SuppressWarnings("removal")
	static Advancement.Builder copyOf(Advancement advancement) {
		// Not Advancement.Builder#advancement, which always enables telemetry events.
		Advancement.Builder builder = new Advancement.Builder();
		advancement.parent().ifPresent(builder::parent);
		advancement.criteria().forEach(builder::addCriterion);
		// The requirements have to be copied explicitly, otherwise the builder would recreate them
		// from all criteria with its default strategy, making every criterion required.
		builder.requirements(advancement.requirements());
		builder.rewards(advancement.rewards());
		advancement.display().ifPresent(builder::display);

		if (advancement.sendsTelemetryEvent()) {
			builder.sendsTelemetryEvent();
		}

		return builder;
	}
}
