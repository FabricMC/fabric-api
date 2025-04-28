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

package net.fabricmc.fabric.impl.client.gametest;

import org.jetbrains.annotations.Nullable;

public final class TestSystemProperties {
	private TestSystemProperties() {
	}

	// Whether client gametests are to be run. Reference the field in the mixin config plugin rather than the other way
	// round so that this class isn't loaded before mixins are bootstrapped.
	public static final boolean ENABLED = ClientGameTestMixinConfigPlugin.ENABLED;

	// The resources path of the test mod to be written to by (e.g. screenshot) regression tests.
	@Nullable
	public static final String TEST_MOD_RESOURCES_PATH = System.getProperty("fabric.client.gametest.testModResourcesPath");

	// Disable the network (packet) synchronizer.
	public static final boolean DISABLE_NETWORK_SYNCHRONIZER = System.getProperty("fabric.client.gametest.disableNetworkSynchronizer") != null;

	// Disable the joining of async stack traces in ThreadingImpl.
	public static final boolean DISABLE_JOIN_ASYNC_STACK_TRACES = System.getProperty("fabric.client.gametest.disableJoinAsyncStackTraces") != null;

	// A comma separated list of mod ids to run tests for. When empty, all tests are run.
	public static final String MOD_ID_FILTER_PROPERTY = "fabric.client.gametest.modid";
}
