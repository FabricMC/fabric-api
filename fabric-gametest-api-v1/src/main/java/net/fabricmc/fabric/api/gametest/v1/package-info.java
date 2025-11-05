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

/**
 * Provides support for GameTest framework.
 *
 * <h2>What is GameTest?</h2>
 *
 * <p>GameTest is a framework, bundled in the game and originally designed for unit-testing
 * of game code. This can be used by mod developers to test their code.
 *
 * <p>GameTest runs as a special dedicated server that automatically calls the defined
 * "test methods". The test result can then be written as an XML file.
 *
 * <h2>Creating a test</h2>
 *
 * <p>First, make an empty class and register it under the {@code fabric-gametest} entrypoint in the
 * {@code fabric.mod.json} file.
 *
 * <p>Each "test method" represents a set of code that sets up the testing site and checks the
 * behavior of the code - for example, it could check that using a flint and steel on a creeper
 * causes explosion, or that hoppers can insert items into barrels. A test method is always annotated
 * with {@link net.fabricmc.fabric.api.gametest.v1.GameTest}. By default the test will run with
 * an empty structure, you can specify a structure using {@link net.fabricmc.fabric.api.gametest.v1.GameTest#structure()}
 * For complex tests, you can also save a structure as an SNBT file under {@code modid/gametest/structure/}
 * in the test mod's data pack and reference that structure. It will then be loaded before the test.
 *
 * <p>Test methods are instance methods (i.e. not static) and take exactly one argument - {@link
 * net.minecraft.gametest.framework.GameTestHelper}. This provides access to the world and additionally provides
 * dozens of assertions, utility methods, and more.
 * Test methods should end with {@link net.minecraft.gametest.framework.GameTestHelper#succeed()}.
 *
 * <p>Example of a test method:
 * <pre>{@code
 * public class MyTest {
 * 	@FabricGameTest
 * 	public void testSomething(TestContext context) {
 * 		context.assertTrue(MyMod.getSomeValue(context.getWorld()) > 0, "SomeValue should be positive.");
 * 	    context.complete(); // do not forget!
 * 	}
 * }
 * }</pre>
 *
 * <h2>Running GameTest</h2>
 *
 * <p>To run the server with GameTest enabled, add {@code -Dfabric-api.gametest} to the
 * JVM arguments. The server works like the usual dedicated server, except that all
 * experimental features are turned on by default.
 *
 * <p>To export the test result, set {@code fabric-api.gametest.report-file}
 * property to the output file path.
 *
 * <p>Example of a Gradle run config to launch GameTest:
 * <pre>{@code
 * loom {
 * 	runs {
 * 		gametest {
 * 			inherit testmodServer
 * 			name "Game Test"
 * 			vmArg "-Dfabric-api.gametest"
 * 			vmArg "-Dfabric-api.gametest.report-file=${project.buildDir}/junit.xml"
 * 			runDir "build/gametest"
 * 		}
 * 	}
 * }
 * }</pre>
 *
 * @see net.fabricmc.fabric.api.gametest.v1.GameTest
 */

@NullMarked
package net.fabricmc.fabric.api.gametest.v1;

import org.jspecify.annotations.NullMarked;
