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

package net.fabricmc.fabric.impl.object.builder;

import java.util.Objects;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

import org.jspecify.annotations.Nullable;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.SpawnPlacementType;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.level.levelgen.Heightmap;

import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityType;

public interface FabricEntityTypeImpl {
	void fabric_setAlwaysUpdateVelocity(@Nullable Boolean alwaysUpdateVelocity);

	void fabric_setCanPotentiallyExecuteCommands(@Nullable Boolean canPotentiallyExecuteCommands);

	interface Builder {
		void fabric_setLivingEntityBuilder(Living<? extends LivingEntity> livingBuilder);

		void fabric_setMobEntityBuilder(Mob<? extends net.minecraft.world.entity.Mob> mobBuilder);

		static <T extends LivingEntity> EntityType.Builder<T> createLiving(EntityType.EntityFactory<T> factory, MobCategory spawnGroup, UnaryOperator<FabricEntityType.Builder.Living<T>> livingBuilder) {
			EntityType.Builder<T> builder = EntityType.Builder.of(factory, spawnGroup);
			Living<T> builderImpl = new Living<>();
			livingBuilder.apply(builderImpl);
			((Builder) builder).fabric_setLivingEntityBuilder(builderImpl);
			return builder;
		}

		static <T extends net.minecraft.world.entity.Mob> EntityType.Builder<T> createMob(EntityType.EntityFactory<T> factory, MobCategory spawnGroup, UnaryOperator<FabricEntityType.Builder.Mob<T>> mobBuilder) {
			EntityType.Builder<T> builder = EntityType.Builder.of(factory, spawnGroup);
			Mob<T> builderImpl = new Mob<>();
			mobBuilder.apply(builderImpl);
			((Builder) builder).fabric_setMobEntityBuilder(builderImpl);
			return builder;
		}

		sealed class Living<T extends LivingEntity> implements FabricEntityType.Builder.Living<T> permits Mob {
			@Nullable
			private Supplier<AttributeSupplier.Builder> defaultAttributeBuilder;

			@Override
			public FabricEntityType.Builder.Living<T> defaultAttributes(Supplier<AttributeSupplier.Builder> defaultAttributeBuilder) {
				Objects.requireNonNull(defaultAttributeBuilder, "Cannot set null attribute builder");
				this.defaultAttributeBuilder = defaultAttributeBuilder;
				return this;
			}

			public void onBuild(EntityType<T> type) {
				if (this.defaultAttributeBuilder != null) {
					FabricDefaultAttributeRegistry.register(type, this.defaultAttributeBuilder.get());
				}
			}
		}

		final class Mob<T extends net.minecraft.world.entity.Mob> extends Living<T> implements FabricEntityType.Builder.Mob<T> {
			private SpawnPlacementType restrictionLocation;
			private Heightmap.Types restrictionHeightmap;
			private SpawnPlacements.SpawnPredicate<T> spawnPredicate;

			@Override
			public FabricEntityType.Builder.Mob<T> spawnRestriction(SpawnPlacementType location, Heightmap.Types heightmap, SpawnPlacements.SpawnPredicate<T> spawnPredicate) {
				this.restrictionLocation = Objects.requireNonNull(location, "Location cannot be null.");
				this.restrictionHeightmap = Objects.requireNonNull(heightmap, "Heightmap type cannot be null.");
				this.spawnPredicate = Objects.requireNonNull(spawnPredicate, "Spawn predicate cannot be null.");
				return this;
			}

			@Override
			public FabricEntityType.Builder.Mob<T> defaultAttributes(Supplier<AttributeSupplier.Builder> defaultAttributeBuilder) {
				super.defaultAttributes(defaultAttributeBuilder);
				return this;
			}

			public void onBuild(EntityType<T> type) {
				super.onBuild(type);

				if (this.spawnPredicate != null) {
					SpawnPlacements.register(type, this.restrictionLocation, this.restrictionHeightmap, this.spawnPredicate);
				}
			}
		}
	}
}
