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

package net.fabricmc.fabric.api.client.gametest.v1.world;

import java.util.Properties;
import java.util.function.Consumer;

import org.jetbrains.annotations.ApiStatus;

import net.minecraft.client.gui.screen.world.WorldCreator;

import net.fabricmc.fabric.api.client.gametest.v1.context.TestDedicatedServerContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;

/**
 * A builder used for creating singleplayer worlds and dedicated servers.
 *
 * <p>Worlds from this builder default to being flat worlds with settings and game rules designed for consistency of
 * tests, see the package documentation for details. To disable this, use {@link #setUseConsistentSettings}. If you need
 * to re-enable a particular setting, you can override it using {@link #adjustSettings}.
 */
@ApiStatus.NonExtendable
public interface TestWorldBuilder {
	/**
	 * Sets whether to use consistent world settings. Consistent settings are designed for consistency of tests. See the
	 * package documentation for details on what the consistent settings are.
	 *
	 * <p>If disabled, the world builder will default to creating worlds with the default world preset in survival mode,
	 * as if clicking straight through the create world screen without changing any settings.
	 *
	 * @param useConsistentSettings Whether to use consistent settings
	 * @return This world builder instance
	 */
	TestWorldBuilder setUseConsistentSettings(boolean useConsistentSettings);

	/**
	 * Adjusts the world settings from the default. Can be used to adjust anything that can be changed in the create
	 * world screen, including generation settings and game rules.
	 *
	 * @param settingsAdjuster The function to adjust the world settings
	 * @return This world builder instance
	 */
	TestWorldBuilder adjustSettings(Consumer<WorldCreator> settingsAdjuster);

	/**
	 * Creates and joins a singleplayer world with the configured world settings.
	 *
	 * @return The singleplayer context of the world that was joined
	 */
	TestSingleplayerContext create();

	/**
	 * Creates and starts a dedicated server with the configured world settings.
	 *
	 * <p>The dedicated server will only run if the EULA has been accepted in {@code eula.txt}. See
	 * {@link TestDedicatedServerContext} for details.
	 *
	 * @return The dedicated server context of the server that was created
	 */
	default TestDedicatedServerContext createServer() {
		return createServer(new Properties());
	}

	/**
	 * Creates and starts a dedicated server with the configured world settings and some custom server properties.
	 *
	 * <p>The dedicated server will only run if the EULA has been accepted in {@code eula.txt}. See
	 * {@link TestDedicatedServerContext} for details.
	 *
	 * @param serverProperties The custom server properties to be written to the {@code server.properties} file.
	 * @return The dedicated server context of the server that was created.
	 */
	TestDedicatedServerContext createServer(Properties serverProperties);
}
