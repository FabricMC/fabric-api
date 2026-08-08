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

package net.fabricmc.fabric.impl.client.gametest.context;

import java.util.function.Predicate;

import com.google.common.base.Preconditions;
import org.apache.commons.lang3.function.FailableConsumer;
import org.apache.commons.lang3.function.FailableFunction;
import org.apache.commons.lang3.mutable.MutableObject;

import net.minecraft.server.MinecraftServer;

import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestServerContext;
import net.fabricmc.fabric.impl.client.gametest.threading.ThreadingImpl;

public class TestServerContextImpl implements TestServerContext {
	protected final MinecraftServer server;

	public TestServerContextImpl(MinecraftServer server) {
		this.server = server;
	}

	@Override
	public void runCommand(String command) {
		ThreadingImpl.checkOnGametestThread("runCommand");
		Preconditions.checkNotNull(command, "command");

		runOnServer(server -> server.getCommands().performPrefixedCommand(server.createCommandSourceStack(), command));
	}

	@Override
	public <E extends Throwable> void runOnServer(FailableConsumer<MinecraftServer, E> action) throws E {
		ThreadingImpl.checkOnGametestOrServerThread("runOnServer", server);
		Preconditions.checkNotNull(action, "action");

		if (server.isSameThread()) {
			action.accept(server);
		} else {
			ThreadingImpl.runOnServer(() -> action.accept(server));
		}
	}

	@Override
	public <T, E extends Throwable> T computeOnServer(FailableFunction<MinecraftServer, T, E> function) throws E {
		ThreadingImpl.checkOnGametestOrServerThread("computeOnServer", server);
		Preconditions.checkNotNull(function, "function");

		if (server.isSameThread()) {
			return function.apply(server);
		} else {
			MutableObject<T> result = new MutableObject<>();
			ThreadingImpl.runOnServer(() -> result.setValue(function.apply(server)));
			return result.getValue();
		}
	}

	@Override
	public int waitFor(Predicate<MinecraftServer> predicate) {
		ThreadingImpl.checkOnGametestThread("waitFor");
		Preconditions.checkNotNull(predicate, "predicate");
		return waitFor(predicate, ClientGameTestContext.DEFAULT_TIMEOUT);
	}

	@Override
	public int waitFor(Predicate<MinecraftServer> predicate, int timeout) {
		ThreadingImpl.checkOnGametestThread("waitFor");
		Preconditions.checkNotNull(predicate, "predicate");

		if (timeout == ClientGameTestContext.NO_TIMEOUT) {
			int ticksWaited = 0;

			while (!computeOnServer(predicate::test)) {
				ticksWaited++;
				ThreadingImpl.runTick();
			}

			return ticksWaited;
		} else {
			Preconditions.checkArgument(timeout > 0, "timeout must be positive");

			for (int i = 0; i < timeout; i++) {
				if (computeOnServer(predicate::test)) {
					return i;
				}

				ThreadingImpl.runTick();
			}

			if (!computeOnServer(predicate::test)) {
				throw new AssertionError("Timed out waiting for predicate");
			}

			return timeout;
		}
	}
}
