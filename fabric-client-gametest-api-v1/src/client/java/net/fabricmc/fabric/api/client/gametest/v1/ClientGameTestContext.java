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

import java.nio.file.Path;
import java.util.function.Predicate;
import java.util.function.Supplier;

import org.apache.commons.lang3.function.FailableConsumer;
import org.apache.commons.lang3.function.FailableFunction;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import net.minecraft.SharedConstants;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;

/**
 * Context for a client gametest containing various helpful functions and functions to access the game.
 *
 * <p>Functions in this class can only be called on the client gametest thread.
 */
@ApiStatus.NonExtendable
public interface ClientGameTestContext {
	/**
	 * Used to specify that a wait task should have no timeout.
	 */
	int NO_TIMEOUT = -1;

	/**
	 * The default timeout in ticks for wait tasks (10 seconds).
	 */
	int DEFAULT_TIMEOUT = 10 * SharedConstants.TICKS_PER_SECOND;

	/**
	 * Runs a single tick and waits for it to complete.
	 */
	void waitTick();

	/**
	 * Runs {@code ticks} ticks and waits for them to complete.
	 *
	 * @param ticks The amount of ticks to run
	 */
	void waitTicks(int ticks);

	/**
	 * Waits for a predicate to be true. Fails if the predicate is not satisfied after {@link #DEFAULT_TIMEOUT} ticks.
	 *
	 * @param predicate The predicate to check
	 * @return The number of ticks waited
	 */
	int waitFor(Predicate<MinecraftClient> predicate);

	/**
	 * Waits for a predicate to be true. Fails if the predicate is not satisfied after {@code timeout} ticks. If
	 * {@code timeout} is {@link #NO_TIMEOUT}, there is no timeout.
	 *
	 * @param predicate The predicate to check
	 * @param timeout The number of ticks before timing out
	 * @return The number of ticks waited
	 */
	int waitFor(Predicate<MinecraftClient> predicate, int timeout);

	/**
	 * Waits for the given screen class to be shown. If {@code screenClass} is {@code null}, waits for the current
	 * screen to be {@code null}. Fails if the screen does not open after {@link #DEFAULT_TIMEOUT} ticks.
	 *
	 * @param screenClass The screen class to wait to open
	 * @return The number of ticks waited
	 */
	int waitForScreen(@Nullable Class<? extends Screen> screenClass);

	/**
	 * Opens a {@link Screen} on the client.
	 *
	 * @param screen The screen to open
	 * @see MinecraftClient#setScreen(Screen)
	 */
	void setScreen(Supplier<@Nullable Screen> screen);

	/**
	 * Presses the button in the current screen whose label is the given translation key. Fails if the button couldn't
	 * be found.
	 *
	 * @param translationKey The translation key of the label of the button to press
	 */
	void clickScreenButton(String translationKey);

	/**
	 * Presses the button in the current screen whose label is the given translation key, if the button exists. Returns
	 * whether the button was found.
	 *
	 * @param translationKey The translation key of the label of the button to press
	 * @return Whether the button was found
	 */
	boolean tryClickScreenButton(String translationKey);

	/**
	 * Takes a screenshot after waiting 1 tick (for a frame to render) and saves it in the screenshots directory.
	 *
	 * @param name The name of the screenshot
	 */
	Path takeScreenshot(String name);

	/**
	 * Takes a screnshot after waiting {@code delay} ticks and saves it in the screenshots directory.
	 *
	 * @param name The name of the screenshot
	 * @param delay The delay in ticks before taking the screenshot
	 */
	Path takeScreenshot(String name, int delay);

	/**
	 * Gets the input handler used to simulate inputs to the client.
	 *
	 * @return The client gametest input handler
	 */
	TestInput getInput();

	/**
	 * Creates a world builder for creating singleplayer worlds and dedicated servers.
	 *
	 * @return A new world builder
	 */
	TestWorldBuilder worldBuilder();

	/**
	 * Restores all game options in {@link MinecraftClient#options} to their default values for client gametests. This
	 * is called automatically before each gametest is run, so you only need to call this explicitly if you want to do
	 * it in the middle of the test.
	 */
	void restoreDefaultGameOptions();

	/**
	 * Runs the given action on the render thread (client thread), and waits for it to complete.
	 *
	 * @param action The action to run on the render thread
	 * @param <E> The type of checked exception that the action throws
	 * @throws E When the action throws an exception
	 */
	<E extends Throwable> void runOnClient(FailableConsumer<MinecraftClient, E> action) throws E;

	/**
	 * Runs the given function on the render thread (client thread), and returns the result.
	 *
	 * @param function The function to run on the render thread
	 * @return The result of the function
	 * @param <T> The type of the value to return
	 * @param <E> The type of the checked exception that the function throws
	 * @throws E When the function throws an exception
	 */
	<T, E extends Throwable> T computeOnClient(FailableFunction<MinecraftClient, T, E> function) throws E;
}
