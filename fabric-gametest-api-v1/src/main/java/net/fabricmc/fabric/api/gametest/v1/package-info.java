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

/// Provides support for GameTest framework.
/// ## What is GameTest?
///
/// GameTest is a framework, bundled in the game and originally designed for unit-testing
/// of game code. This can be used by mod developers to test their code.
///
/// GameTest runs as a special dedicated server that automatically calls the defined
/// "test methods". The test result can then be written as an XML file.
/// ## Creating a test
///
/// First, make an empty class and register it under the `fabric-gametest` entrypoint in the
/// `fabric.mod.json` file.
///
/// Each "test method" represents a set of code that sets up the testing site and checks the
/// behavior of the code - for example, it could check that using a flint and steel on a creeper
/// causes explosion, or that hoppers can insert items into barrels. A test method is always annotated
/// with [net.fabricmc.fabric.api.gametest.v1.GameTest]. By default, the test will run with
/// an empty structure; you can specify a structure using [net.fabricmc.fabric.api.gametest.v1.GameTest#structure()]
/// For complex tests, you can also save a structure as an SNBT file under `modid/gametest/structure/`
/// in the test mod's data pack and reference that structure. It will then be loaded before the test.
///
/// Test methods are instance methods (i.e. not static) and take exactly one argument -
/// [net.minecraft.gametest.framework.GameTestHelper]. This provides access to the level and additionally provides
/// dozens of assertions, utility methods, and more.
/// Test methods should end with [net.minecraft.gametest.framework.GameTestHelper#succeed()].
///
/// Example of a test method:
/// <pre>
/// `public class MyTest{void testSomething(GameTestHelper helper){helper.assertTrue(MyMod.getSomeValue(helper.getLevel()) > 0, "SomeValue should be positive.");helper.succeed(); // do not forget!}}`</pre>
/// ## Running GameTest
///
/// To run the server with GameTest enabled, add `-Dfabric-api.gametest` to the
/// JVM arguments. The server works like the usual dedicated server, except that all
/// experimental features are turned on by default.
///
/// To export the test result, set `fabric-api.gametest.report-file`
/// property to the output file path.
///
/// Example of a Gradle run config to launch GameTest:
/// <pre>
/// `loom{runs{gametest{inherit testmodServername "Game Test"vmArg "-Dfabric-api.gametest"vmArg "-Dfabric-api.gametest.report-file=${project.buildDir}/junit.xml"runDir "build/gametest"}}}`</pre>
///
/// @see net.fabricmc.fabric.api.gametest.v1.GameTest
@NullMarked
package net.fabricmc.fabric.api.gametest.v1;

import org.jspecify.annotations.NullMarked;
