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

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;

// This test is intentionally not an entrypoint to verify the generics of the entity type builder propagate properly
final class EntityTypeBuilderGenericsTest {
	static ResourceKey<EntityType<?>> DUMMY = ResourceKey.create(Registries.ENTITY_TYPE, Identifier.fromNamespaceAndPath("test", "dummy"));
	static EntityType<Entity> ENTITY_1 = FabricEntityTypeBuilder.create().build(DUMMY);
	static EntityType<LivingEntity> LIVING_ENTITY_1 = FabricEntityTypeBuilder.createLiving().build(DUMMY);
	static EntityType<TestEntity> TEST_ENTITY_1 = FabricEntityTypeBuilder.createLiving()
			.entityFactory(TestEntity::new)
			.spawnGroup(MobCategory.CREATURE)
			.build(DUMMY);
	static EntityType<TestEntity> OLD_TEST = FabricEntityTypeBuilder.<TestEntity>createLiving()
			.entityFactory(TestEntity::new)
			.spawnGroup(MobCategory.CREATURE)
			.build(DUMMY);
	static EntityType<TestMob> OLD_MOB = FabricEntityTypeBuilder.<TestMob>createMob()
			.disableSaving()
			.entityFactory(TestMob::new)
			.build(DUMMY);
	static EntityType<TestMob> MOB_TEST = FabricEntityTypeBuilder.createMob()
			.disableSaving()
			.entityFactory(TestMob::new)
			.build(DUMMY);

	private static class TestEntity extends LivingEntity {
		protected TestEntity(EntityType<? extends LivingEntity> entityType, Level world) {
			super(entityType, world);
		}

		@Override
		public ItemStack getItemBySlot(EquipmentSlot slot) {
			return ItemStack.EMPTY;
		}

		@Override
		public void setItemSlot(EquipmentSlot slot, ItemStack stack) {
		}

		@Override
		public HumanoidArm getMainArm() {
			return HumanoidArm.RIGHT;
		}
	}

	private static class TestMob extends Mob {
		protected TestMob(EntityType<? extends Mob> entityType, Level world) {
			super(entityType, world);
		}
	}
}
