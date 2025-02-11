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

package net.fabricmc.fabric.api.client.gametest.v1.screenshot;

import com.google.common.base.Preconditions;
import org.jetbrains.annotations.ApiStatus;

import net.minecraft.client.texture.NativeImage;

import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.impl.client.gametest.screenshot.TestScreenshotComparisonOptionsImpl;

/**
 * Options for comparing screenshots.
 *
 * <p>By default, screenshots are compared with a fuzzy comparison which allows screenshots to be 0.5% different from
 * the template image. See {@link TestScreenshotComparisonAlgorithm#defaultAlgorithm()} for details on how this is
 * implemented. To change the threshold for how different the screenshot can be, set the algorithm to
 * {@link TestScreenshotComparisonAlgorithm#meanSquaredDifference(float)}. To force the screenshot to be exactly the
 * same as your template, use {@link TestScreenshotComparisonAlgorithm#exact()}. It is recommended to stick with fuzzy
 * matching unless you specifically require an exact match, because the exact screenshot can vary slightly on different
 * frames and in rare cases other factors such as the GPU/driver version.
 *
 * <p>Template images must be fully opaque, this API will throw if the template image has any transparent pixels.
 *
 * <p>Template images loaded from a path are expected to be in the {@code templates} directory inside the resources
 * directory of the mod which registered the gametest. If the template image is not found then the screenshot will be
 * saved to the {@code templates} directory so that it can be used next time the gametest is run. Your test mod's
 * resources directory is in the {@code fabric.client.gametest.testModResourcesPath} system property which is set by
 * Loom by default. You may set this system property to override the location of the test mod resources directory. If
 * this system property is unset and the template image is not found, then the test will fail.
 */
@ApiStatus.NonExtendable
public interface TestScreenshotComparisonOptions extends TestScreenshotCommonOptions<TestScreenshotComparisonOptions> {
	/**
	 * Load the template image from a path. The template image is expected to be in the {@code templates} directory
	 * inside the resources directory of the mod which registered the gametest.
	 *
	 * @param templateImage The template image path
	 * @return The new screenshot comparison options instance
	 */
	static TestScreenshotComparisonOptions of(String templateImage) {
		Preconditions.checkNotNull(templateImage, "templateImage");
		return new TestScreenshotComparisonOptionsImpl(templateImage);
	}

	/**
	 * Use the given image as the template image.
	 *
	 * @param templateImage The template image
	 * @return The new screenshot comparison options instance
	 */
	static TestScreenshotComparisonOptions of(NativeImage templateImage) {
		Preconditions.checkNotNull(templateImage, "templateImage");
		return new TestScreenshotComparisonOptionsImpl(templateImage);
	}

	/**
	 * Additionally save the screenshot which was compared against with the template image name.
	 * This method only works when a template image name instead of a {@link NativeImage} is used.
	 * This method works as if by calling {@link ClientGameTestContext#takeScreenshot(TestScreenshotOptions)}
	 * with these screenshot options, except that the screenshot saved is from the same render of the game
	 * as the one that is compared against in this screenshot comparison.
	 *
	 * @return This screenshot comparison options instance
	 * @throws java.util.NoSuchElementException if template image is not provided by path
	 */
	TestScreenshotComparisonOptions save();

	/**
	 * Additionally save the screenshot which was compared against. This method works as if by calling
	 * {@link ClientGameTestContext#takeScreenshot(TestScreenshotOptions)} with these screenshot options, except that
	 * the screenshot saved is from the same render of the game as the one that is compared against in this screenshot
	 * comparison.
	 *
	 * @param fileName The name of the screenshot to save
	 * @return This screenshot comparison options instance
	 */
	TestScreenshotComparisonOptions saveWithFileName(String fileName);

	/**
	 * Changes the algorithm used to compare the template image with the screenshot. See class documentation for
	 * details.
	 *
	 * @param algorithm The new algorithm
	 * @return This screenshot comparison options instance
	 */
	TestScreenshotComparisonOptions withAlgorithm(TestScreenshotComparisonAlgorithm algorithm);

	/**
	 * Compares images in grayscale rather than in color. Comparing in grayscale is typically faster but has the obvious
	 * disadvantage of two colors with different hues but the same brightness being treated as the same.
	 *
	 * @return This screenshot comparison options instance
	 */
	TestScreenshotComparisonOptions withGrayscale();

	/**
	 * Only considers a certain region of the screenshot for comparison. When used with
	 * {@link ClientGameTestContext#assertScreenshotEquals(TestScreenshotComparisonOptions) assertScreenshotEquals},
	 * asserts that the template image is equal to the given region of the screenshot. When used with
	 * {@link ClientGameTestContext#assertScreenshotContains(TestScreenshotComparisonOptions) assertScreenshotContains},
	 * asserts that the template image is contained within the given region of the screenshot.
	 *
	 * @param x The minimum X position in the region
	 * @param y The minimum Y position in the region
	 * @param width The width of the region
	 * @param height The height of the region
	 * @return This screenshot comparison options instance
	 */
	TestScreenshotComparisonOptions withRegion(int x, int y, int width, int height);
}
