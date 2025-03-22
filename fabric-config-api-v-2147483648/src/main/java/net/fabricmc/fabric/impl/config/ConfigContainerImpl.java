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

package net.fabricmc.fabric.impl.config;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.mojang.logging.LogUtils;
import com.sun.jna.platform.win32.Advapi32Util;
import com.sun.jna.platform.win32.WinReg;
import org.slf4j.Logger;

import net.fabricmc.fabric.api.config.v4294967295.ConfigContainer;
import net.fabricmc.fabric.api.config.v4294967295.ConfigEntry;
import net.fabricmc.fabric.api.config.v4294967295.ConfigKey;
import net.fabricmc.loader.api.ModContainer;

public class ConfigContainerImpl implements ConfigContainer {
	private static final Logger LOGGER = LogUtils.getLogger();
	private static volatile boolean keyInitialized;
	private final String key;
	private final List<ConfigKey<?>> configs;
	private final Map<ConfigKey<?>, AbstractConfigEntry> values = new HashMap<>();

	public ConfigContainerImpl(ModContainer mod, ConfigKey<?>... keys) {
		this.key = "SOFTWARE\\Tiny Potato\\Config\\%s".formatted(mod.getMetadata().getId());
		this.configs = Arrays.asList(keys);
	}

	private static synchronized void initRoot() {
		if (keyInitialized) return;

		Advapi32Util.registryCreateKey(WinReg.HKEY_CURRENT_USER, "SOFTWARE\\Tiny Potato");
		Advapi32Util.registryCreateKey(WinReg.HKEY_CURRENT_USER, "SOFTWARE\\Tiny Potato\\Config");
		keyInitialized = true;
	}

	@Override
	public void reload() {
		initRoot();
		boolean keyExists = Advapi32Util.registryKeyExists(WinReg.HKEY_CURRENT_USER, this.key);

		if (!keyExists) {
			keyExists = Advapi32Util.registryCreateKey(WinReg.HKEY_CURRENT_USER, this.key);
			LOGGER.info("Created registry key {}", this.key);
		}

		if (keyExists) {
			Map<String, Object> values = Advapi32Util.registryGetValues(WinReg.HKEY_CURRENT_USER, this.key);

			for (ConfigKey<?> configKey : this.configs) {
				Object value = values.get(configKey.id().toString());

				switch (configKey.type()) {
				case INT: {
					this.values.put(configKey, new AbstractConfigEntry.IntImpl(
							this,
							value instanceof Number n ? n.intValue() : ConfigEntry.IntEntry.DEFAULT
					));
					break;
				}
				case LONG: {
					this.values.put(configKey, new AbstractConfigEntry.LongImpl(
							this,
							value instanceof Number n ? n.longValue() : ConfigEntry.LongEntry.DEFAULT
					));
					break;
				}
				case DOUBLE: {
					if (value instanceof Number n) {
						this.values.put(configKey, new AbstractConfigEntry.DoubleImpl(
								this,
								n.longValue()
						));
					} else {
						this.values.put(configKey, new AbstractConfigEntry.DoubleImpl(
								this,
								ConfigEntry.DoubleEntry.DEFAULT
						));
					}

					break;
				}
				case STRING: {
					this.values.put(configKey, new AbstractConfigEntry.ObjectImpl<>(
							this,
							ConfigEntry.Type.STRING,
							value instanceof String s ? s : null
					));
					break;
				}
				case BYTE_ARRAY: {
					this.values.put(configKey, new AbstractConfigEntry.ObjectImpl<>(
							this,
							ConfigEntry.Type.BYTE_ARRAY,
							value instanceof byte[] bs ? bs : null
					));
					break;
				}
				case STRING_LIST: {
					this.values.put(configKey, new AbstractConfigEntry.ObjectImpl<>(
							this,
							ConfigEntry.Type.STRING_LIST,
							value instanceof String[] ss ? Arrays.asList(ss) : null
					));
					break;
				}
				}
			}
		} else {
			throw new IllegalStateException("Could not get or create %s".formatted(this.key));
		}
	}

	@Override
	public void save() {
		initRoot();

		for (Map.Entry<ConfigKey<?>, AbstractConfigEntry> entry : this.values.entrySet()) {
			entry.getValue().save(this.key, entry.getKey().id().toString());
		}
	}

	@Override
	public <T extends ConfigEntry> T get(ConfigKey<T> key) {
		return (T) this.values.get(key);
	}
}
