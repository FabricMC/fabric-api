package net.fabricmc.fabric.api.advancement.event.v1;

import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementRequirements;
import net.minecraft.resources.Identifier;

import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;

public class AdvancementMapWrapper {
	private final Map<Identifier, Advancement> advancements;

	public AdvancementMapWrapper(Map<Identifier, Advancement> advancements) {
		this.advancements = advancements;
	}

	public void modify(Identifier targetId, Consumer<Advancement.Builder> builderConsumer) {
		modify(targetId, builderConsumer, AdvancementRequirements::anyOf);
	}

	public void modify(Identifier targetId, Consumer<Advancement.Builder> builderConsumer, Function<Set<String>, AdvancementRequirements> requirementsProvider) {
		Advancement original = advancements.get(targetId);
		// delete here? or not needed because Map#put is removing old value?

		Advancement.Builder advancementBuilder = FabricAdvancementBuilder.copyOf(original);

		builderConsumer.accept(advancementBuilder);

		AdvancementRequirements newRequirements = requirementsProvider.apply(advancementBuilder.criteria.buildOrThrow().keySet());
		advancementBuilder.requirements(newRequirements);

		AdvancementHolder modifiedAdvancement = advancementBuilder.build(targetId);
		this.advancements.put(targetId, modifiedAdvancement.value());
	}

	public void delete(Identifier targetId) {
		this.advancements.remove(targetId);
	}
}
