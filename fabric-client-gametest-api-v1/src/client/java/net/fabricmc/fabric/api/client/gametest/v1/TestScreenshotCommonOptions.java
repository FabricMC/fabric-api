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

import org.jetbrains.annotations.ApiStatus;

/**
 * Base class for screenshot customization options common to taking screenshots and comparing them.
 *
 * @param <SELF> The builder class
 */
@ApiStatus.NonExtendable
public interface TestScreenshotCommonOptions<SELF extends TestScreenshotCommonOptions<SELF>> {
	/**
	 * By default, screenshot file names will be prefixed by a counter so that the screenshots appear in sequence in the
	 * screenshots directory. Use this method to disable this behavior.
	 *
	 * @return This screenshot options instance
	 */
	SELF disableCounterPrefix();

	/**
	 * Changes the tick delta to take this screenshot with. Tick delta controls interpolation between the previous tick and the
	 * current tick to make objects appear to move more smoothly when there are multiple frames in a tick. Defaults to
	 * {@code 1}, which renders all objects as their appear in the current tick.
	 *
	 * @param tickDelta The tick delta to take this screenshot with
	 * @return This screenshot options instance
	 */
	SELF withTickDelta(float tickDelta);

	/**
	 * Changes the resolution of the screenshot, which defaults to the resolution of the Minecraft window.
	 *
	 * @param width The width of the screenshot
	 * @param height The height of the screenshot
	 * @return This screenshot options instance
	 */
	SELF withSize(int width, int height);

	/**
	 * Changes the directory in which this screenshot is saved, which defaults to the {@code screenshots} directory in
	 * the game's run directory.
	 *
	 * @param destinationDir The directory in which to save the screenshot
	 * @return This screenshot options instance
	 */
	SELF withDestinationDir(Path destinationDir);
}
