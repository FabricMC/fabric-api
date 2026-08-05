package net.fabricmc.fabric.mixin.advancement;

import com.google.common.collect.ImmutableMap;
import com.llamalad7.mixinextras.injector.ModifyReceiver;
import net.fabricmc.fabric.api.advancement.v1.FabricAdvancementBuilder;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementRequirements;
import net.minecraft.advancements.AdvancementRewards;
import net.minecraft.advancements.DisplayInfo;
import net.minecraft.advancements.triggers.Criterion;
import net.minecraft.resources.Identifier;

import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
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
	private boolean sendsTelemetryEvent;

	@Unique
	@Nullable
	private Set<String> removedCriteria;

	@Override
	public void removeCriterion(String name) {
		if (this.removedCriteria == null) {
			this.removedCriteria = new HashSet<>();
		}

		this.removedCriteria.add(name);
	}

	@Override
	public Map<String, Criterion<?>> getCriteria() {
		// Create a snapshot of the current criteria
		Map<String, Criterion<?>> map = new HashMap<>(this.criteria.build());

		// Remove from the map the criteria that were removed
		if (this.removedCriteria != null) {
			this.removedCriteria.forEach(map::remove);
		}

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
	public AdvancementRequirements getRequirements() {
		return this.requirements.orElse(AdvancementRequirements.EMPTY);
	}

	@Override
	public boolean isSendsTelemetryEvent() {
		return this.sendsTelemetryEvent;
	}

	@Override
	public void requireCriterion(String name) {
		requireCriteria(List.of(name));
	}

	@Override
	public void requireCriteria(List<String> names) {
		if (names.isEmpty()) {
			throw new IllegalArgumentException("Cannot require an empty list of criteria");
		}

		List<List<String>> newRequirements = new ArrayList<>();

		// Keep whatever requirement groups were already configured (e.g. via a Strategy
		// or a previous call to this method) completely untouched.
		this.requirements.ifPresent(existing -> newRequirements.addAll(existing.requirements()));

		// Add the new group last; it's AND'd with everything already present, and
		// satisfying any single criterion within it is enough to fulfill this group.
		newRequirements.add(List.copyOf(names));

		this.requirements = Optional.of(new AdvancementRequirements(newRequirements));
	}

	// Intercept the build process to filter the criteria map before it becomes immutable
	@ModifyReceiver(
			method = "build(Lnet/minecraft/resources/Identifier;)Lnet/minecraft/advancements/AdvancementHolder;",
			at = @At(value = "INVOKE", target = "Lcom/google/common/collect/ImmutableMap$Builder;buildOrThrow()Lcom/google/common/collect/ImmutableMap;")
	)
	private ImmutableMap.Builder<String, Criterion<?>> applyFabricRemovals(ImmutableMap.Builder<String, Criterion<?>> original) {
		if (this.removedCriteria == null || this.removedCriteria.isEmpty()) {
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
