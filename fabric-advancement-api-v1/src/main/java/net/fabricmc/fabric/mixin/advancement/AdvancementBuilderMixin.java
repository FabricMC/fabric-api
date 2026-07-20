package net.fabricmc.fabric.mixin.advancement;

import com.google.common.collect.ImmutableMap;
import com.llamalad7.mixinextras.injector.ModifyReceiver;
import net.fabricmc.fabric.api.advancement.event.v1.FabricAdvancementBuilder;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementRequirements;
import net.minecraft.advancements.AdvancementRewards;
import net.minecraft.advancements.DisplayInfo;
import net.minecraft.advancements.triggers.Criterion;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Mixin(Advancement.Builder.class)
public abstract class AdvancementBuilderMixin implements FabricAdvancementBuilder {
	@Shadow
	@Final
	private ImmutableMap.Builder<String, Criterion<?>> criteria;

	@Shadow
	private Optional<Identifier> parent;

	@Shadow
	private Optional<DisplayInfo> display;

	@Shadow
	private AdvancementRewards rewards;

	@Shadow
	private Optional<AdvancementRequirements> requirements;

	@Shadow
	private AdvancementRequirements.Strategy requirementsStrategy;

	@Shadow
	private boolean sendsTelemetryEvent;

	@Unique
	private final Set<String> removedCriteria = new HashSet<>();

	@Override
	public void removeCriterion(String name) {
		this.removedCriteria.add(name);
	}

	@Override
	public Map<String, Criterion<?>> getCriteria() {
		// Create a snapshot of the current criteria
		Map<String, Criterion<?>> map = new HashMap<>(this.criteria.build());
		// Remove from the map the criteria that were removed
		this.removedCriteria.forEach(map::remove);
		return map;
	}

	@Override
	public Optional<Identifier> getParent() {
		return this.parent;
	}

	@Override
	public Optional<DisplayInfo> getDisplay() {
		return this.display;
	}

	@Override
	public AdvancementRewards getRewards() {
		return this.rewards;
	}

	@Override
	public Optional<AdvancementRequirements> getRequirements() {
		return this.requirements;
	}

	@Override
	public AdvancementRequirements.Strategy getRequirementsStrategy() {
		return this.requirementsStrategy;
	}

	@Override
	public boolean sendsTelemetryEvent() {
		return this.sendsTelemetryEvent;
	}

	// Intercept the build process to filter the criteria map before it becomes immutable
	@ModifyReceiver(
			method = "build(Lnet/minecraft/resources/Identifier;)Lnet/minecraft/advancements/AdvancementHolder;",
			at = @At(value = "INVOKE", target = "Lcom/google/common/collect/ImmutableMap$Builder;buildOrThrow()Lcom/google/common/collect/ImmutableMap;")
	)
	private ImmutableMap.Builder<String, Criterion<?>> applyFabricRemovals(ImmutableMap.Builder<String, Criterion<?>> original) {
		if (this.removedCriteria.isEmpty()) {
			return original;
		}

		ImmutableMap.Builder<String, Criterion<?>> newBuilder = ImmutableMap.builder();
		original.buildOrThrow().forEach((key, value) -> {
			if (!this.removedCriteria.contains(key)) {
				newBuilder.put(key, value);
			}
		});
		return newBuilder;
	}
}
