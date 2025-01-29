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

package net.fabricmc.fabric.api.client.gametest.v1.context;

import org.jetbrains.annotations.ApiStatus;

/**
 * Context for a client gametest containing various helpful functions while an in-process dedicated server is running.
 * This class implements {@link AutoCloseable} and is intended to be used in a try-with-resources statement. When
 * closed, the dedicated server will be stopped.
 *
 * <p>Dedicated servers will only run if the EULA has been accepted in {@code eula.txt}. If you have read and accepted
 * the <a href="https://aka.ms/MinecraftEULA">Minecraft EULA</a>, you can write the file at build-time by setting
 * {@code fabricApi.configureTests { eula = true }} in your {@code build.gradle}.
 *
 * <p>Functions in this class can only be called on the client gametest thread.
 */
@ApiStatus.NonExtendable
public interface TestDedicatedServerContext extends TestServerContext, AutoCloseable {
	/**
	 * Connects the client to the dedicated server. The resulting connection is intended to be used in a
	 * try-with-resources statement.
	 *
	 * @return The connection handle to the dedicated server
	 */
	TestServerConnection connect();

	/**
	 * Stops the dedicated server.
	 */
	@Override
	void close();
}
