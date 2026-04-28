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

package net.fabricmc.fabric.test.lookup.entity;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.animal.pig.Pig;
import net.minecraft.world.entity.monster.Creeper;

import net.fabricmc.fabric.api.lookup.v1.entity.EntityApiLookup;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.fabricmc.fabric.test.lookup.FabricApiLookupTest;
import net.fabricmc.fabric.test.lookup.api.Inspectable;

public class FabricEntityApiLookupTest {
	public static final ResourceKey<EntityType<?>> INSPECTABLE_PIG_KEY = ResourceKey.create(Registries.ENTITY_TYPE, Identifier.fromNamespaceAndPath(FabricApiLookupTest.MOD_ID, "inspectable_pig"));
	public static final EntityApiLookup<Inspectable, Void> INSPECTABLE =
			EntityApiLookup.get(Identifier.fromNamespaceAndPath(FabricApiLookupTest.MOD_ID, "inspectable"), Inspectable.class, Void.class);

	public static final EntityType<InspectablePig> INSPECTABLE_PIG = EntityType.Builder.of(InspectablePig::new, MobCategory.CREATURE)
			.sized(0.9F, 0.9F)
			.clientTrackingRange(10)
			.build(INSPECTABLE_PIG_KEY);

	public static void onInitialize() {
		Registry.register(BuiltInRegistries.ENTITY_TYPE, INSPECTABLE_PIG_KEY, INSPECTABLE_PIG);
		FabricDefaultAttributeRegistry.register(INSPECTABLE_PIG, Pig.createAttributes());

		INSPECTABLE.registerSelf(INSPECTABLE_PIG);
		INSPECTABLE.registerForTypes(
				(entity, context) -> () -> Component.literal("registerForTypes: " + entity.getClass().getName()),
				EntityTypes.PLAYER,
				EntityTypes.COW);
		INSPECTABLE.registerFallback((entity, context) -> {
			if (entity instanceof Creeper) {
				return () -> Component.literal("registerFallback: Creeper");
			} else {
				return null;
			}
		});
	}
}
