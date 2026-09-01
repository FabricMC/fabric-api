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

package net.fabricmc.fabric.mixin.advancement;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import com.google.common.collect.ImmutableMap;
import com.llamalad7.mixinextras.injector.ModifyReceiver;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementRequirements;
import net.minecraft.advancements.AdvancementRewards;
import net.minecraft.advancements.DisplayInfo;
import net.minecraft.advancements.triggers.Criterion;
import net.minecraft.resources.Identifier;

import net.fabricmc.fabric.api.advancement.v1.FabricAdvancementBuilder;

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
	private Map<String, Criterion<?>> modifiedCriteria;

	@Unique
	private Advancement.Builder self() {
		return (Advancement.Builder) (Object) this;
	}

	/**
	 * Returns the mutable criteria map, creating it from the vanilla criteria on first use.
	 */
	@Unique
	private Map<String, Criterion<?>> mutableCriteria() {
		if (this.modifiedCriteria == null) {
			this.modifiedCriteria = new LinkedHashMap<>(this.criteria.buildOrThrow());
		}

		return this.modifiedCriteria;
	}

	@Unique
	private void pureRequirements() {
		if (this.requirements.isEmpty()) {
			return;
		}

		Map<String, Criterion<?>> criteria = mutableCriteria();
		List<List<String>> groups = new ArrayList<>();

		for (List<String> group : this.requirements.get().requirements()) {
			List<String> prunedGroup = group.stream().filter(criteria::containsKey).toList();

			if (!prunedGroup.isEmpty()) {
				groups.add(prunedGroup);
			}
		}

		this.requirements = Optional.of(new AdvancementRequirements(groups));
	}

	@Override
	public Map<String, Criterion<?>> getCriteria() {
		return Map.copyOf(this.modifiedCriteria != null ? this.modifiedCriteria : this.criteria.buildOrThrow());
	}

	@Override
	public Advancement.Builder setCriteria(Map<String, Criterion<?>> criteria) {
		Objects.requireNonNull(criteria, "criteria cannot be null");

		this.modifiedCriteria = new LinkedHashMap<>(criteria);
		pureRequirements();

		return self();
	}

	@Override
	public Advancement.Builder removeCriterion(String name) {
		Objects.requireNonNull(name, "criterion name cannot be null");

		if (mutableCriteria().remove(name) != null) {
			pureRequirements();
		}

		return self();
	}

	@Override
	public Advancement.Builder requireCriterion(String name) {
		Objects.requireNonNull(name, "criterion name cannot be null");

		return requireCriteria(List.of(name));
	}

	@Override
	public Advancement.Builder requireCriteria(List<String> names) {
		Objects.requireNonNull(names, "criteria names cannot be null");

		List<List<String>> newRequirements = new ArrayList<>();
		this.requirements.ifPresent(existing -> newRequirements.addAll(existing.requirements()));
		newRequirements.add(List.copyOf(names));

		this.requirements = Optional.of(new AdvancementRequirements(newRequirements));

		return self();
	}

	@Override
	public AdvancementRequirements getRequirements() {
		return this.requirements.orElse(AdvancementRequirements.EMPTY);
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
	public boolean isSendsTelemetryEvent() {
		return this.sendsTelemetryEvent;
	}

	@Inject(method = "addCriterion(Ljava/lang/String;Lnet/minecraft/advancements/triggers/Criterion;)Lnet/minecraft/advancements/Advancement$Builder;", at = @At("HEAD"), cancellable = true)
	private void addModifiedCriterion(String name, Criterion<?> criterion, CallbackInfoReturnable<Advancement.Builder> cir) {
		if (this.modifiedCriteria != null) {
			this.modifiedCriteria.put(name, criterion);
			cir.setReturnValue(self());
		}
	}

	@ModifyReceiver(
			method = "build(Lnet/minecraft/resources/Identifier;)Lnet/minecraft/advancements/AdvancementHolder;",
			at = @At(value = "INVOKE", target = "Lcom/google/common/collect/ImmutableMap$Builder;buildOrThrow()Lcom/google/common/collect/ImmutableMap;")
	)
	private ImmutableMap.Builder<String, Criterion<?>> useModifiedCriteria(ImmutableMap.Builder<String, Criterion<?>> original) {
		if (this.modifiedCriteria == null) {
			return original;
		}

		ImmutableMap.Builder<String, Criterion<?>> builder = ImmutableMap.builder();
		builder.putAll(this.modifiedCriteria);

		return builder;
	}
}
