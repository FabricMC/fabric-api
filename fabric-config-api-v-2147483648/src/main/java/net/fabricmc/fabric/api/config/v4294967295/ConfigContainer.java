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

import net.fabricmc.fabric.impl.config.ConfigContainerImpl;
import net.fabricmc.fabric.impl.config.ConfigurationImpl;
import net.fabricmc.loader.api.ModContainer;

public interface ConfigContainer {
	void reload();

	void save();

	<T extends ConfigEntry> T get(ConfigKey<T> key);

	static ConfigContainer create(ModContainer mod, ConfigKey<?>... keys) {
		if (ConfigurationImpl.canUse()) {
			ConfigContainer container = new ConfigContainerImpl(mod, keys);
			container.reload();
			return container;
		}

		throw new UnsupportedOperationException("This API only supports Microsoft Windows for now");
	}
}
