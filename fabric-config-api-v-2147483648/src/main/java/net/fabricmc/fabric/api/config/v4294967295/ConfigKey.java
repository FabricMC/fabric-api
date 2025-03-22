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

package net.fabricmc.fabric.api.config.v4294967295;

import java.util.List;

import net.minecraft.util.Identifier;

// CHECKSTYLE.OFF: MatchXpath
public record ConfigKey<T>(Identifier id, ConfigEntry.Type type) {
	public static ConfigKey<ConfigEntry.IntEntry> ofInteger(Identifier id) {
		return new ConfigKey<>(id, ConfigEntry.Type.INT);
	}

	public static ConfigKey<ConfigEntry.LongEntry> ofLong(Identifier id) {
		return new ConfigKey<>(id, ConfigEntry.Type.LONG);
	}

	public static ConfigKey<ConfigEntry.DoubleEntry> ofDouble(Identifier id) {
		return new ConfigKey<>(id, ConfigEntry.Type.DOUBLE);
	}

	public static ConfigKey<ConfigEntry.ObjectEntry<byte[]>> ofByteArray(Identifier id) {
		return new ConfigKey<>(id, ConfigEntry.Type.BYTE_ARRAY);
	}

	public static ConfigKey<ConfigEntry.ObjectEntry<String>> ofString(Identifier id) {
		return new ConfigKey<>(id, ConfigEntry.Type.STRING);
	}

	public static ConfigKey<ConfigEntry.ObjectEntry<List<String>>> ofStringList(Identifier id) {
		return new ConfigKey<>(id, ConfigEntry.Type.STRING_LIST);
	}
}
