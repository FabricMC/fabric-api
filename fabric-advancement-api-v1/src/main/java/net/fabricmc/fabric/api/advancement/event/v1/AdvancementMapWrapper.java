package net.fabricmc.fabric.api.advancement.event.v1;

import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementRequirements;
import net.minecraft.resources.Identifier;

import java.util.Map;
import java.util.function.Consumer;

public class AdvancementMapWrapper {
	private final Map<Identifier, Advancement> advancements;

	public AdvancementMapWrapper(Map<Identifier, Advancement> advancements) {
		this.advancements = advancements;
	}

	public void modify(Identifier targetId, Consumer<Advancement.Builder> builderConsumer) {
		Advancement original = advancements.get(targetId);
		delete(targetId);

		Advancement.Builder advancementBuilder = FabricAdvancementBuilder.copyOf(original);

		builderConsumer.accept(advancementBuilder);

		AdvancementRequirements newRequirements = AdvancementRequirements.anyOf(advancementBuilder.criteria.buildOrThrow().keySet());
		advancementBuilder.requirements(newRequirements);

		AdvancementHolder modifiedAdvancement = advancementBuilder.build(targetId);
		this.advancements.put(targetId, modifiedAdvancement.value());
	}

	public void delete(Identifier targetId) {
		this.advancements.remove(targetId);
	}
}
