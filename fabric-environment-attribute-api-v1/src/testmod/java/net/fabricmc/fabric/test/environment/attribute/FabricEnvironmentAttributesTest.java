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

package net.fabricmc.fabric.test.environment.attribute;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.attribute.AttributeTypes;
import net.minecraft.world.attribute.EnvironmentAttribute;

import net.fabricmc.api.ModInitializer;

public class FabricEnvironmentAttributesTest implements ModInitializer {
	public static final EnvironmentAttribute<Integer> TEST_COLOR = EnvironmentAttribute.builder(AttributeTypes.RGB_COLOR)
			.defaultValue(0xFFFFFF)
			.syncable()
			.build();

	@Override
	public void onInitialize() {
		Registry.register(BuiltInRegistries.ENVIRONMENT_ATTRIBUTE, Identifier.fromNamespaceAndPath("fabric", "test_color"), TEST_COLOR);
	}
}
