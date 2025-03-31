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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.test.FunctionTestInstance;
import net.minecraft.test.TestContext;
import net.minecraft.test.TestData;
import net.minecraft.test.TestEnvironmentDefinition;
import net.minecraft.test.TestInstance;
import net.minecraft.util.Identifier;

import net.fabricmc.fabric.api.gametest.v1.CustomTestMethodInvoker;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.entrypoint.EntrypointContainer;

final class TestAnnotationLocator {
	private static final String ENTRYPOINT_KEY = "fabric-gametest";
	private static final Logger LOGGER = LoggerFactory.getLogger(TestAnnotationLocator.class);

	private final FabricLoader fabricLoader;

	private List<TestMethod> testMethods = null;

	TestAnnotationLocator(FabricLoader fabricLoader) {
		this.fabricLoader = fabricLoader;
	}

	public List<TestMethod> getTestMethods() {
		if (testMethods != null) {
			return testMethods;
		}

		List<EntrypointContainer<Object>> entrypointContainers = fabricLoader
				.getEntrypointContainers(ENTRYPOINT_KEY, Object.class);

		return testMethods = entrypointContainers.stream()
				.flatMap(entrypoint -> findMagicMethods(entrypoint).stream())
				.toList();
	}

	private List<TestMethod> findMagicMethods(EntrypointContainer<Object> entrypoint) {
		Class<?> testClass = entrypoint.getEntrypoint().getClass();
		List<TestMethod> methods = new ArrayList<>();
		findMagicMethods(entrypoint, testClass, methods);

		if (methods.isEmpty()) {
			LOGGER.warn("No methods with the GameTest annotation were found in {}", testClass.getName());
		}

		return methods;
	}

	// Recursively find all methods with the GameTest annotation
	private void findMagicMethods(EntrypointContainer<Object> entrypoint, Class<?> testClass, List<TestMethod> methods) {
		for (Method method : testClass.getDeclaredMethods()) {
			if (method.isAnnotationPresent(GameTest.class)) {
				if (!CustomTestMethodInvoker.class.isAssignableFrom(testClass)) {
					// Only validate the test method when using the default reflection invoker
					validateMethod(method);
				}

				methods.add(new TestMethod(method, method.getAnnotation(GameTest.class), entrypoint));
			}
		}

		if (testClass.getSuperclass() != null) {
			findMagicMethods(entrypoint, testClass.getSuperclass(), methods);
		}
	}

	private void validateMethod(Method method) {
		List<String> issues = new ArrayList<>();

		if (method.getParameterCount() != 1 || method.getParameterTypes()[0] != TestContext.class) {
			issues.add("must have a single parameter of type TestContext");
		}

		if (!Modifier.isPublic(method.getModifiers())) {
			issues.add("must be public");
		}

		if (Modifier.isStatic(method.getModifiers())) {
			issues.add("must not be static");
		}

		if (method.getReturnType() != void.class) {
			issues.add("must return void");
		}

		if (issues.isEmpty()) {
			return;
		}

		String methodName = method.getDeclaringClass().getName() + "#" + method.getName();
		throw new UnsupportedOperationException("Test method (%s) has the following issues: %s".formatted(methodName, String.join(", ", issues)));
	}

	public record TestMethod(Method method, GameTest gameTest, EntrypointContainer<Object> entrypoint) {
		Identifier identifier() {
			String name = camelToSnake(entrypoint.getEntrypoint().getClass().getSimpleName() + "_" + method.getName());
			return Identifier.of(entrypoint.getProvider().getMetadata().getId(), name);
		}

		Consumer<TestContext> testFunction() {
			return context -> {
				Object instance = entrypoint.getEntrypoint();

				try {
					if (instance instanceof CustomTestMethodInvoker customTestMethodInvoker) {
						customTestMethodInvoker.invokeTestMethod(context, method);
						return;
					}

					method.invoke(instance, context);
				} catch (InvocationTargetException e) {
					// Ensure that any GameTestException are propagated without wrapping
					if (e.getTargetException() instanceof RuntimeException runtimeException) {
						throw runtimeException;
					}

					throw new RuntimeException("Failed to invoke test method", e);
				} catch (ReflectiveOperationException e) {
					throw new RuntimeException("Failed to invoke test method", e);
				}
			};
		}

		TestData<RegistryEntry<TestEnvironmentDefinition>> testData(Registry<TestEnvironmentDefinition> testEnvironmentDefinitionRegistry) {
			RegistryEntry<TestEnvironmentDefinition> testEnvironment = testEnvironmentDefinitionRegistry.getOrThrow(RegistryKey.of(RegistryKeys.TEST_ENVIRONMENT, Identifier.of(gameTest.environment())));

			return new TestData<>(
					testEnvironment,
					Identifier.of(gameTest.structure()),
					gameTest.maxTicks(),
					gameTest.setupTicks(),
					gameTest.required(),
					gameTest.rotation(),
					gameTest.manualOnly(),
					gameTest.maxAttempts(),
					gameTest.requiredSuccesses(),
					gameTest.skyAccess()
			);
		}

		TestInstance testInstance(Registry<TestEnvironmentDefinition> testEnvironmentDefinitionRegistry) {
			return new FunctionTestInstance(
					RegistryKey.of(RegistryKeys.TEST_FUNCTION, identifier()),
					testData(testEnvironmentDefinitionRegistry)
			);
		}

		private static String camelToSnake(String input) {
			return input.replaceAll("([a-z])([A-Z])", "$1_$2").toLowerCase(Locale.ROOT);
		}
	}
}
