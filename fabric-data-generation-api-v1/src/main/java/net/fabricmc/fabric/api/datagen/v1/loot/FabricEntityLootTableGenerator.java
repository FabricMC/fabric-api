package net.fabricmc.fabric.api.datagen.v1.loot;

import com.google.common.base.Preconditions;

import net.fabricmc.fabric.api.resource.conditions.v1.ResourceCondition;

import net.fabricmc.fabric.impl.datagen.loot.ConditionEntityLootTableGenerator;

import net.minecraft.data.loottable.EntityLootTableGenerator;

public interface FabricEntityLootTableGenerator {
	/**
	 * {@return a new generator that applies the specified conditions to any loot table it receives, and then forwards
	 * the loot tables to this generator}
	 */
	default EntityLootTableGenerator withConditions(ResourceCondition... conditions) {
		Preconditions.checkArgument(conditions.length > 0, "Must add at least one condition.");

		return new ConditionEntityLootTableGenerator((EntityLootTableGenerator) this, conditions);
	}
}
