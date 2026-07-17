package net.fabricmc.fabric.api.advancement.event.v1;

import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementRequirements;
import net.minecraft.advancements.triggers.Criterion;
import net.minecraft.resources.Identifier;

import java.util.HashMap;
import java.util.Map;
import java.util.function.UnaryOperator;

public class AdvancementMapWrapper {
	private final Map<Identifier, Advancement> advancements;

	public AdvancementMapWrapper(Map<Identifier, Advancement> advancements) {
		this.advancements = advancements;
	}

	public void modify(Identifier targetId, UnaryOperator<AdvancementMapBuilder> builderOperator) {
		AdvancementMapBuilder advancementMapBuilder = new AdvancementMapBuilder(targetId, advancements);
		AdvancementMapBuilder result = builderOperator.apply(advancementMapBuilder);
		result.build();
	}

	public void remove(Identifier targetId) {
		AdvancementMapBuilder advancementMapBuilder = new AdvancementMapBuilder(targetId, advancements);
		for (String name : advancementMapBuilder.criteria.keySet()) {
			advancementMapBuilder = advancementMapBuilder.deleteCriterion(name);
		}
		advancementMapBuilder.build();
	}

	public static class AdvancementMapBuilder {
		private final Identifier targetId;
		private final Map<Identifier, Advancement> advancements;
		private final Advancement original;
		private final Map<String, Criterion<?>> criteria;
		private boolean modified = false;

		public AdvancementMapBuilder(Identifier targetId, Map<Identifier, Advancement> advancements) {
			this.targetId = targetId;
			this.advancements = advancements;
			this.original = advancements.get(targetId);
			// Allow for non-existing advancements to be modified/added directly at runtime?
			this.criteria = this.original != null ? new HashMap<>(this.original.criteria()) : new HashMap<>();
		}

		public AdvancementMapBuilder addCriterion(String name, Criterion<?> criterion) {
			this.criteria.put(name, criterion);
			this.modified = true;
			return this;
		}

		public AdvancementMapBuilder deleteCriterion(String name) {
			this.criteria.remove(name);
			this.modified = true;
			return this;
		}

		public void build() {
			// Only if the builder has been modified
			if (this.modified) {
				AdvancementRequirements newRequirements = AdvancementRequirements.anyOf(this.criteria.keySet());

				Advancement modifiedAdvancement = new Advancement(
						this.original.parent(),
						this.original.display(),
						this.original.rewards(),
						Map.copyOf(this.criteria),
						newRequirements,
						this.original.sendsTelemetryEvent()
				);

				this.advancements.put(this.targetId, modifiedAdvancement);
				this.modified = false;
			}
		}
	}
}
