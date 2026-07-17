package net.fabricmc.fabric.mixin.advancement;

import com.google.common.collect.ImmutableMap;
import net.fabricmc.fabric.api.advancement.event.v1.FabricAdvancementBuilder;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.triggers.Criterion;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Mixin(Advancement.Builder.class)
public abstract class AdvancementBuilderMixin implements FabricAdvancementBuilder {

	@Shadow @Final private ImmutableMap.Builder<String, Criterion<?>> criteria;

	@Unique
	private final Set<String> fabric_removedCriteria = new HashSet<>();

	@Override
	public void fabric_removeCriterion(String name) {
		this.fabric_removedCriteria.add(name);
	}

	@Override
	public Map<String, Criterion<?>> fabric_getCriteria() {
		Map<String, Criterion<?>> map = new HashMap<>(this.criteria.build());
		this.fabric_removedCriteria.forEach(map::remove);
		return map;
	}

	// Intercept the build process to filter the criteria map before it becomes immutable
	@Redirect(
			method = "build(Lnet/minecraft/resources/Identifier;)Lnet/minecraft/advancements/AdvancementHolder;",
			at = @At(value = "INVOKE", target = "Lcom/google/common/collect/ImmutableMap$Builder;buildOrThrow()Lcom/google/common/collect/ImmutableMap;")
	)
	private ImmutableMap<String, Criterion<?>> applyFabricRemovals(ImmutableMap.Builder<String, Criterion<?>> instance) {
		ImmutableMap<String, Criterion<?>> original = instance.buildOrThrow();

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
