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

package net.fabricmc.fabric.api.gametest.v1;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import net.minecraft.util.BlockRotation;

/**
 * {@link GameTest} is an annotation that can be used to mark a method as a game test.
 *
 * <p>{@link GameTest} methods must be {@code public} not {@code static}, return {@code void } and take exactly one argument of type {@link net.minecraft.test.TestContext}.
 *
 * <p>The values in this class directly correspond to the values in {@link net.minecraft.test.TestData}.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Documented
public @interface GameTest {
	/**
	 * A namespaced ID of an entry within the {@link net.minecraft.registry.RegistryKeys#TEST_ENVIRONMENT} registry.
	 */
	String environment() default "minecraft:default";

	/**
	 * A namespaced ID pointing to a structure resource in the {@code modid/gametest/structure/} directory.
	 *
	 * <p>Defaults to an 8x8 structure with no blocks.
	 */
	String structure() default "fabric-gametest-api-v1:empty";

	/**
	 * The maximum number of ticks the test is allowed to run for.
	 */
	int maxTicks() default 20;

	/**
	 * The number of ticks to wait before starting the test after placing the structure.
	 */
	int setupTicks() default 0;

	/**
	 * Whether the test is required to pass for the test suite to pass.
	 */
	boolean required() default true;

	/**
	 * The rotation of the structure when placed.
	 */
	BlockRotation rotation() default BlockRotation.NONE;

	/**
	 * When set the test must be run manually.
	 */
	boolean manualOnly() default false;

	/**
	 * The number of times the test should be re attempted if it fails.
	 */
	int maxAttempts() default 1;

	/**
	 * The number of times the test should be successfully ran before it is considered a success.
	 */
	int requiredSuccesses() default 1;

	/**
	 * Whether the test should have sky access. When {@code false} the test will be enclosed by barrier blocks.
	 */
	boolean skyAccess() default false;
}
