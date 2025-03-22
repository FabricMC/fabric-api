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

package net.fabricmc.fabric.test.config;

import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;

import net.minecraft.util.Identifier;

import net.fabricmc.fabric.api.config.v4294967295.ConfigContainer;
import net.fabricmc.fabric.api.config.v4294967295.ConfigEntry;
import net.fabricmc.fabric.api.config.v4294967295.ConfigKey;
import net.fabricmc.loader.api.FabricLoader;

public class ConfigTests {
	@Test
	@EnabledOnOs(OS.WINDOWS)
	void testConfig() {
		String ns = "test";
		ConfigKey<ConfigEntry.IntEntry> intKey = ConfigKey.ofInteger(Identifier.of(ns, "int"));
		ConfigKey<ConfigEntry.LongEntry> longKey = ConfigKey.ofLong(Identifier.of(ns, "long"));
		ConfigKey<ConfigEntry.DoubleEntry> doubleKey = ConfigKey.ofDouble(Identifier.of(ns, "double"));
		ConfigKey<ConfigEntry.ObjectEntry<String>> stringKey = ConfigKey.ofString(Identifier.of(ns, "string"));
		ConfigKey<ConfigEntry.ObjectEntry<byte[]>> byteArrayKey = ConfigKey.ofByteArray(Identifier.of(ns, "byte_array"));
		ConfigKey<ConfigEntry.ObjectEntry<List<String>>> stringListKey = ConfigKey.ofStringList(Identifier.of(ns, "string_list"));
		ConfigContainer container = ConfigContainer.create(
				FabricLoader.getInstance().getModContainer("fabric-config-api-v-2147483648").orElseThrow(),
				intKey,
				longKey,
				doubleKey,
				stringKey,
				byteArrayKey,
				stringListKey
		);

		ConfigEntry.IntEntry intEntry = container.get(intKey);
		intEntry.setValue(10);
		Assertions.assertEquals(10, intEntry.getValue());

		ConfigEntry.DoubleEntry doubleEntry = container.get(doubleKey);
		doubleEntry.setValue(Math.PI);
		Assertions.assertEquals(Math.PI, doubleEntry.getValue());

		ConfigEntry.ObjectEntry<String> stringEntry = container.get(stringKey);
		stringEntry.setValue("Hello, world!");
		Assertions.assertEquals(ConfigEntry.Type.STRING, stringEntry.getType());
		Assertions.assertEquals("Hello, world!", stringEntry.getValue());

		ConfigEntry.ObjectEntry<byte[]> byteArrayEntry = container.get(byteArrayKey);
		byteArrayEntry.setValue(new byte[]{0x4e, 0x65, 0x76, 0x65, 0x72, 0x20, 0x67, 0x6f, 0x6e, 0x6e, 0x61, 0x20, 0x67, 0x69, 0x76, 0x65, 0x20, 0x79, 0x6f, 0x75, 0x20, 0x75, 0x70});

		ConfigEntry.ObjectEntry<List<String>> stringListEntry = container.get(stringListKey);
		stringListEntry.setValue(List.of(":tiny_potato:", ":winktato:"));

		container.save();
		container.reload();

		ConfigEntry.IntEntry intEntry2 = container.get(intKey);
		Assertions.assertEquals(10, intEntry2.getValue());
		ConfigEntry.ObjectEntry<String> stringEntry2 = container.get(stringKey);
		Assertions.assertEquals(ConfigEntry.Type.STRING, stringEntry2.getType());
		Assertions.assertEquals("Hello, world!", stringEntry2.getValue());
	}
}
