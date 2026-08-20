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

import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;

public class FabricDefaultAttributeRegistryTest implements ModInitializer {
	public static final Holder<Attribute> TEST_ATTRIBUTE = Registry.registerForHolder(BuiltInRegistries.ATTRIBUTE, ObjectBuilderTestConstants.id("test_attribute"), new RangedAttribute("attribute.name.%s.test_attribute".formatted(ObjectBuilderTestConstants.MOD_ID), 0.0, 0.0, 100.0));
	public static final Holder<Attribute> TEST_CHICKEN_ONLY_ATTRIBUTE = Registry.registerForHolder(BuiltInRegistries.ATTRIBUTE, ObjectBuilderTestConstants.id("test_chicken_only_attribute"), new RangedAttribute("attribute.name.%s.test_chicken_only_attribute".formatted(ObjectBuilderTestConstants.MOD_ID), 0.0, 0.0, 100.0));
	public static final double PIG_TEST_ATTRIBUTE_BASE_VALUE = 10.0;

	@Override
	public void onInitialize() {
		FabricDefaultAttributeRegistry.MODIFY.register(context -> {
			context.modifyAll((_, builder) -> {
				builder.add(TEST_ATTRIBUTE);
			});
			context.modify(EntityTypes.PIG, (_, builder) -> {
				builder.add(TEST_ATTRIBUTE, PIG_TEST_ATTRIBUTE_BASE_VALUE);
			});
			context.modify(EntityTypes.CHICKEN, (_, builder) -> {
				builder.add(TEST_CHICKEN_ONLY_ATTRIBUTE);
			});
		});
	}
}
