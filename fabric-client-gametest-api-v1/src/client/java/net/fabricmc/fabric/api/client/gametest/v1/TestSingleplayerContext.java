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

package net.fabricmc.fabric.api.client.gametest.v1;

import org.jetbrains.annotations.ApiStatus;

/**
 * Context for a client gametest containing various helpful functions while a singleplayer game is open.
 *
 * <p>Functions in this class can only be called on the client gametest thread.
 */
@ApiStatus.NonExtendable
public interface TestSingleplayerContext extends AutoCloseable {
	/**
	 * Gets the handle for the world save.
	 *
	 * @return The handle for the world save
	 */
	TestWorldSave getWorldSave();

	/**
	 * Gets the handle for the client world.
	 *
	 * @return The handle for the client world
	 */
	TestClientWorldContext getClientWorld();

	/**
	 * Gets the handle for the integrated server.
	 *
	 * @return The handle for the integrated server
	 */
	TestServerContext getServer();

	/**
	 * Closes the singleplayer world.
	 */
	@Override
	void close();
}
