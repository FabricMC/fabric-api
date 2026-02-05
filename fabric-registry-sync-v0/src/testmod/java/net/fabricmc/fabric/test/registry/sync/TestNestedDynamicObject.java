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

package net.fabricmc.fabric.test.registry.sync;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.Holder;
import net.minecraft.resources.RegistryFileCodec;

public record TestNestedDynamicObject(Holder<TestDynamicObject> nested) {
	public static final Codec<TestNestedDynamicObject> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			RegistryFileCodec.create(CustomDynamicRegistryTest.TEST_SYNCED_1_DYNAMIC_REGISTRY_KEY, TestDynamicObject.CODEC)
					.fieldOf("nested")
					.forGetter(TestNestedDynamicObject::nested)
	).apply(instance, TestNestedDynamicObject::new));
}
