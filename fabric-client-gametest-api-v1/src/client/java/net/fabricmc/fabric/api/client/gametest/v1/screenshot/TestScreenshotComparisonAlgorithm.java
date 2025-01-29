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
import org.jetbrains.annotations.Nullable;
import org.joml.Vector2i;

import net.minecraft.client.texture.NativeImage;

import net.fabricmc.fabric.impl.client.gametest.screenshot.TestScreenshotComparisonAlgorithms;

/**
 * An algorithm for finding a template subimage (the "needle") in a screenshot (the "haystack"). Comparison algorithms
 * are written to find a subimage, but can also be used to compare two images of equal size (which is a special case of
 * finding). Custom algorithm implementations are allowed.
 */
public interface TestScreenshotComparisonAlgorithm {
	/**
	 * The default algorithm for this API, which is the Mean Squared Difference algorithm with a threshold of
	 * {@code 0.005}. See {@link #meanSquaredDifference(float)} for details.
	 *
	 * @return The default algorithm for this API
	 */
	static TestScreenshotComparisonAlgorithm defaultAlgorithm() {
		return TestScreenshotComparisonAlgorithms.MeanSquaredDifference.DEFAULT;
	}

	/**
	 * The Mean Squared Difference algorithm computes the mean of differences between the needle pixels and
	 * corresponding haystack pixels squared, and checks if the mean is less than a particular threshold.
	 *
	 * @param maxMeanSquaredDifference The maximum mean squared difference between the needle and the subimage in the
	 *                                 haystack for a match
	 * @return The Mean Squared Difference algorithm with the given threshold
	 */
	static TestScreenshotComparisonAlgorithm meanSquaredDifference(float maxMeanSquaredDifference) {
		Preconditions.checkArgument(maxMeanSquaredDifference >= 0 && maxMeanSquaredDifference <= 1, "maxMeanSquaredError must be between 0 and 1");
		return new TestScreenshotComparisonAlgorithms.MeanSquaredDifference(maxMeanSquaredDifference);
	}

	/**
	 * An algorithm which searches for an exact match of the needle in the haystack. This is simpler and faster than a
	 * fuzzy match, but is prone to failing on even the slightest inconsistency in the image.
	 *
	 * @return An algorithm for exact matches
	 */
	static TestScreenshotComparisonAlgorithm exact() {
		return TestScreenshotComparisonAlgorithms.Exact.INSTANCE;
	}

	/**
	 * Finds a needle in a haystack, color image version. Pixels are in RGB format (with no alpha channel).
	 *
	 * @param haystack The image in which to search for the needle
	 * @param needle The image to search for
	 * @return The location of the match, or {@code null} if no match was found. In case of multiple matches, returns an
	 *         arbitrary match
	 */
	@Nullable
	Vector2i findColor(RawImage<int[]> haystack, RawImage<int[]> needle);

	/**
	 * Finds a needle in a haystack, grayscale image version. Each pixel is a brightness value from 0-255.
	 *
	 * <p>Note for custom algorithm implementations: the default implementation converts the images to color and then
	 * uses {@link #findColor}, but it is usually possible to implement this more efficiently. You should strongly
	 * consider overriding this method.
	 *
	 * @param haystack The image in which to search for the needle
	 * @param needle The image to search for
	 * @return The location of the match, or {@code null} if no match was found. In case of multiple matches, returns an
	 *         arbitrary match
	 */
	@Nullable
	default Vector2i findGrayscale(RawImage<byte[]> haystack, RawImage<byte[]> needle) {
		RawImage<int[]> colorHaystack = TestScreenshotComparisonAlgorithms.RawImageImpl.toColor(haystack);
		RawImage<int[]> colorNeedle = TestScreenshotComparisonAlgorithms.RawImageImpl.toColor(needle);
		return findColor(colorHaystack, colorNeedle);
	}

	/**
	 * A thin wrapper around a raw image data array, so that algorithms can directly access the array which may be more
	 * efficient than going through a {@link NativeImage} each time.
	 *
	 * @param <DATA> The type of the image data array
	 */
	@ApiStatus.NonExtendable
	interface RawImage<DATA> {
		/**
		 * The width of the image.
		 *
		 * @return The width of the image
		 */
		int width();

		/**
		 * The height of the image.
		 *
		 * @return The height of the image
		 */
		int height();

		/**
		 * The raw image data. Each element of this array represents one pixel and is indexed by
		 * {@code y * this.width() + x}, where ({@code x}, {@code y}) are the coordinates of the pixel you want to
		 * index. The length of the array is {@code this.width() * this.height()}.
		 *
		 * @return The raw image data
		 */
		DATA data();
	}
}
