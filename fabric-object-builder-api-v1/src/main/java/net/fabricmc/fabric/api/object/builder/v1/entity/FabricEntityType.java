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

package net.fabricmc.fabric.api.object.builder.v1.entity;

import java.util.function.Supplier;
import java.util.function.UnaryOperator;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.entity.SpawnLocation;
import net.minecraft.entity.SpawnRestriction;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.world.Heightmap;

import net.fabricmc.fabric.impl.object.builder.FabricEntityTypeImpl;

/**
 * General-purpose Fabric-provided extensions for {@link EntityType}.
 */
public interface FabricEntityType {
	/**
	 * General-purpose Fabric-provided extensions for {@link EntityType.Builder}.
	 *
	 * <p>Note: This interface is automatically implemented on {@link EntityType.Builder} via Mixin and interface injection.
	 */
	interface Builder<T extends Entity> {
		/**
		 * Sets whether the entity's velocity should always be updated.
		 *
		 * @param alwaysUpdateVelocity whether the entity's velocity should always be updated
		 * @return this builder
		 */
		default EntityType.Builder<T> alwaysUpdateVelocity(boolean alwaysUpdateVelocity) {
			throw new AssertionError("Implemented in Mixin");
		}

		/**
		 * Sets whether the entity is able to execute commands.
		 *
		 * @param canPotentiallyExecuteCommands whether the entity is able to execute commands
		 * @return this builder
		 */
		default EntityType.Builder<T> canPotentiallyExecuteCommands(boolean canPotentiallyExecuteCommands) {
			throw new AssertionError("Implemented in Mixin");
		}

		/**
		 * Creates an entity type builder for a living entity.
		 *
		 * <p>This entity's spawn group will automatically be set to {@link SpawnGroup#MISC}.
		 *
		 * @param <T> the type of entity
		 * @param livingBuilder a function to configure living entity specific properties
		 *
		 * @return a new living entity type builder
		 */
		static <T extends LivingEntity> EntityType.Builder<T> createLiving(EntityType.EntityFactory<T> factory, SpawnGroup spawnGroup, UnaryOperator<Living<T>> livingBuilder) {
			return FabricEntityTypeImpl.Builder.createLiving(factory, spawnGroup, livingBuilder);
		}

		/**
		 * Creates an entity type builder for a mob entity.
		 *
		 * @param <T> the type of entity
		 * @param mobBuilder a function to configure mob entity specific properties
		 *
		 * @return a new mob entity type builder
		 */
		static <T extends MobEntity> EntityType.Builder<T> createMob(EntityType.EntityFactory<T> factory, SpawnGroup spawnGroup, UnaryOperator<Mob<T>> mobBuilder) {
			return FabricEntityTypeImpl.Builder.createMob(factory, spawnGroup, mobBuilder);
		}

		/**
		 * A builder for additional properties of a living entity, use via {@link #createLiving(EntityType.EntityFactory, SpawnGroup, UnaryOperator)}.
		 * @param <T> the type of living entity
		 */
		interface Living<T extends LivingEntity> {
			/**
			 * Sets the default attributes for a type of living entity.
			 *
			 * @param defaultAttributeBuilder a function to generate the default attribute builder from the entity type
			 * @return this builder for chaining
			 */
			Living<T> defaultAttributes(Supplier<DefaultAttributeContainer.Builder> defaultAttributeBuilder);
		}

		/**
		 * A builder for additional properties of a mob entity, use via {@link #createMob(EntityType.EntityFactory, SpawnGroup, UnaryOperator)}.
		 * @param <T> the type of mob entity
		 */
		interface Mob<T extends MobEntity> extends Living<T> {
			/**
			 * Registers a spawn restriction for this entity.
			 *
			 * <p>This is used by mobs to determine whether Minecraft should spawn an entity within a certain context.
			 *
			 * @return this builder for chaining.
			 */
			Mob<T> spawnRestriction(SpawnLocation location, Heightmap.Type heightmap, SpawnRestriction.SpawnPredicate<T> spawnPredicate);

			/**
			 * Sets the default attributes for a type of mob entity.
			 *
			 * @param defaultAttributeBuilder a function to generate the default attribute builder from the entity type
			 * @return this builder for chaining
			 */
			@Override
			Mob<T> defaultAttributes(Supplier<DefaultAttributeContainer.Builder> defaultAttributeBuilder);
		}
	}
}
