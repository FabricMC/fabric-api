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

import org.apache.commons.lang3.function.FailableConsumer;
import org.apache.commons.lang3.function.FailableFunction;
import org.jetbrains.annotations.ApiStatus;

import net.minecraft.server.MinecraftServer;

/**
 * Context for a client gametest containing various helpful functions while a server (integrated or dedicated) is
 * running.
 *
 * <p>Functions in this class can only be called on the client gametest thread.
 */
@ApiStatus.NonExtendable
public interface TestServerContext {
	/**
	 * Runs a command on the server.
	 *
	 * @param command The command to run
	 */
	void runCommand(String command);

	/**
	 * Runs the given action on the server thread, and waits for it to complete.
	 *
	 * @param action The action to run on the server thread
	 * @param <E> The type of the checked exception that the action throws
	 * @throws E When the action throws an exception
	 */
	<E extends Throwable> void runOnServer(FailableConsumer<MinecraftServer, E> action) throws E;

	/**
	 * Runs the given function on the server thread, and returns the result.
	 *
	 * @param function The function to run on the server thread
	 * @return The result of the function
	 * @param <T> The type of the value to return
	 * @param <E> The type of the checked exception that the function throws
	 * @throws E When the function throws an exception
	 */
	<T, E extends Throwable> T computeOnServer(FailableFunction<MinecraftServer, T, E> function) throws E;
}
