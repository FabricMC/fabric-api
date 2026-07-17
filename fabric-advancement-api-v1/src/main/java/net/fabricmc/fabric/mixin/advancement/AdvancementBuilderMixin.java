package net.fabricmc.fabric.mixin.advancement;

import com.google.common.collect.ImmutableMap;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.fabricmc.fabric.api.advancement.event.v1.FabricAdvancementBuilder;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.triggers.Criterion;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Mixin(Advancement.Builder.class)
public abstract class AdvancementBuilderMixin implements FabricAdvancementBuilder, AdvancementBuilderAccessor {
	@Unique
	private final Set<String> fabric_removedCriteria = new HashSet<>();

	@Override
	public void fabric_removeCriterion(String name) {
		this.fabric_removedCriteria.add(name);
	}

	@Override
	public Map<String, Criterion<?>> fabric_getCriteria() {
		// Create a snapshot of the current criteria
		Map<String, Criterion<?>> map = new HashMap<>(this.fabric_getCriteriaBuilder().build());
		// Remove from the map the criteria that were removed
		this.fabric_removedCriteria.forEach(map::remove);
		return map;
	}

	// Intercept the build process to filter the criteria map before it becomes immutable
	@ModifyExpressionValue(
			method = "build(Lnet/minecraft/resources/Identifier;)Lnet/minecraft/advancements/AdvancementHolder;",
			at = @At(value = "INVOKE", target = "Lcom/google/common/collect/ImmutableMap$Builder;buildOrThrow()Lcom/google/common/collect/ImmutableMap;")
	)
	private ImmutableMap<String, Criterion<?>> applyFabricRemovals(ImmutableMap<String, Criterion<?>> original) {
		if (this.fabric_removedCriteria.isEmpty()) {
			return original;
		}

		ImmutableMap.Builder<String, Criterion<?>> newBuilder = ImmutableMap.builder();
		original.forEach((key, value) -> {
			if (!this.fabric_removedCriteria.contains(key)) {
				newBuilder.put(key, value);
			}
		});

		return newBuilder.buildOrThrow();
	}
}
