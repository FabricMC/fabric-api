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

package net.fabricmc.fabric.impl.gametest;

public final class GameTestSystemProperties {
	/// Enable the game test system.
	public static final String ENABLED = "fabric-api.gametest";

	/// A JUnit XML report file to write the test results to.
	public static final String REPORT_FILE = "fabric-api.gametest.report-file";

	/// Filter the tests to run by the test name.
	public static final String FILTER = "fabric-api.gametest.filter";

	/// Run the enabled tests 100 times for each 90 degree rotation.
	public static final String VERIFY = "fabric-api.gametest.verify";

	private GameTestSystemProperties() {
	}
}
