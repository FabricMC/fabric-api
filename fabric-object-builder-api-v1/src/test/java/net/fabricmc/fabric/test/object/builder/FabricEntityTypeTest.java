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

package net.fabricmc.fabric.test.object.builder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import net.minecraft.SharedConstants;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.SpawnPlacementTypes;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.DefaultAttributes;
import net.minecraft.world.entity.animal.pig.Pig;
import net.minecraft.world.level.levelgen.Heightmap;

import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityType;

public class FabricEntityTypeTest {
	@BeforeAll
	static void beforeAll() {
		SharedConstants.tryDetectVersion();
		Bootstrap.bootStrap();
	}

	@Test
	void buildEntityType() {
		EntityType<Entity> type = EntityType.Builder.createNothing(MobCategory.MISC)
				.alwaysUpdateVelocity(true)
				.build(ResourceKey.create(Registries.ENTITY_TYPE, Identifier.fromNamespaceAndPath("test", "test")));

		assertNotNull(type);
		assertTrue(type.trackDeltas());
	}

	@Test
	void buildLivingEntityType() {
		EntityType<LivingEntity> type = FabricEntityType.Builder.createLiving((t, w) -> null, MobCategory.MISC, living -> living
						.defaultAttributes(FabricEntityTypeTest::createAttributes)
		).build(ResourceKey.create(Registries.ENTITY_TYPE, Identifier.fromNamespaceAndPath("test", "test2")));

		assertNotNull(type);
		assertNotNull(DefaultAttributes.getSupplier(type));
	}

	@Test
	void buildMobEntityType() {
		EntityType<Mob> type = FabricEntityType.Builder.createMob((t, w) -> null, MobCategory.MISC, mob -> mob
				.spawnRestriction(SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Pig::checkMobSpawnRules)
				.defaultAttributes(FabricEntityTypeTest::createAttributes)
		).build(ResourceKey.create(Registries.ENTITY_TYPE, Identifier.fromNamespaceAndPath("test", "test3")));

		assertNotNull(type);
		assertEquals(SpawnPlacementTypes.ON_GROUND, SpawnPlacements.getPlacementType(type));
		assertNotNull(DefaultAttributes.getSupplier(type));
	}

	private static AttributeSupplier.Builder createAttributes() {
		return Mob.createMobAttributes()
				.add(Attributes.MAX_HEALTH, 10.0)
				.add(Attributes.MOVEMENT_SPEED, 0.25);
	}
}
