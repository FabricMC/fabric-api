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

import java.util.Objects;
import java.util.stream.IntStream;

import com.google.common.base.Preconditions;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector2i;

import net.minecraft.client.texture.NativeImage;
import net.minecraft.util.math.ColorHelper;
import net.minecraft.util.math.MathHelper;

import net.fabricmc.fabric.api.client.gametest.v1.TestScreenshotComparisonAlgorithm;

public class TestScreenshotComparisonAlgorithms {
	public record MeanSquaredDifference(float maxMeanSquaredDifference) implements TestScreenshotComparisonAlgorithm {
		public static final MeanSquaredDifference DEFAULT = new MeanSquaredDifference(0.005f);

		@Override
		@Nullable
		public Vector2i findColor(RawImage<int[]> haystack, RawImage<int[]> needle) {
			Preconditions.checkNotNull(haystack, "haystack");
			Preconditions.checkNotNull(needle, "needle");

			int[] haystackData = haystack.data();
			int[] needleData = needle.data();
			int haystackWidth = haystack.width();
			int needleWidth = needle.width();
			int needleHeight = needle.height();

			// We want mean < maxMeanSquaredError
			// sum / (numPixels * 3) < maxMeanSquaredError
			// sum < maxMeanSquaredError * numPixels * 3
			// But the pixels are stored as integers, not floats.
			// We can avoid floating point conversions in the inner loop by multiplying both sides by 255^2:
			// sumSquaredDifference < maxMeanSquaredError * numPixels * 3 * 255^2
			long threshold = (long) ((double) maxMeanSquaredDifference * needleWidth * needleHeight * 3 * 255 * 255);

			return find(haystack, needle, (needleX, needleY) -> {
				long sumSquaredDifference = 0;

				for (int y = 0; y < needleHeight; y++) {
					for (int x = 0; x < needleWidth; x++) {
						int haystackColor = haystackData[(needleY + y) * haystackWidth + needleX + x];
						int haystackRed = ColorHelper.getRed(haystackColor);
						int haystackGreen = ColorHelper.getGreen(haystackColor);
						int haystackBlue = ColorHelper.getBlue(haystackColor);
						int needleColor = needleData[y * needleWidth + x];
						int needleRed = ColorHelper.getRed(needleColor);
						int needleGreen = ColorHelper.getGreen(needleColor);
						int needleBlue = ColorHelper.getBlue(needleColor);
						sumSquaredDifference += MathHelper.square(haystackRed - needleRed) + MathHelper.square(haystackGreen - needleGreen) + MathHelper.square(haystackBlue - needleBlue);

						if (sumSquaredDifference >= threshold) {
							return false;
						}
					}
				}

				return true;
			});
		}

		@Override
		@Nullable
		public Vector2i findGrayscale(RawImage<byte[]> haystack, RawImage<byte[]> needle) {
			Preconditions.checkNotNull(haystack, "haystack");
			Preconditions.checkNotNull(needle, "needle");

			byte[] haystackData = haystack.data();
			byte[] needleData = needle.data();
			int haystackWidth = haystack.width();
			int needleWidth = needle.width();
			int needleHeight = needle.height();

			// We want mean < maxMeanSquaredError
			// sum / numPixels < maxMeanSquaredError
			// sum < maxMeanSquaredError * numPixels
			// But the pixels are stored as integers, not floats.
			// We can avoid floating point conversions in the inner loop by multiplying both sides by 255^2:
			// sumSquaredDifference < maxMeanSquaredError * numPixels * 255^2
			long threshold = (long) ((double) maxMeanSquaredDifference * needleWidth * needleHeight * 255 * 255);

			return find(haystack, needle, (needleX, needleY) -> {
				long sumSquaredDifference = 0;

				for (int y = 0; y < needleHeight; y++) {
					for (int x = 0; x < needleWidth; x++) {
						int haystackLuminance = haystackData[(needleY + y) * haystackWidth + needleX + x] & 0xff;
						int needleLuminance = needleData[y * needleWidth + x] & 0xff;
						sumSquaredDifference += MathHelper.square(haystackLuminance - needleLuminance);

						if (sumSquaredDifference >= threshold) {
							return false;
						}
					}
				}

				return true;
			});
		}
	}

	public enum Exact implements TestScreenshotComparisonAlgorithm {
		INSTANCE;

		@Override
		@Nullable
		public Vector2i findColor(RawImage<int[]> haystack, RawImage<int[]> needle) {
			Preconditions.checkNotNull(haystack, "haystack");
			Preconditions.checkNotNull(needle, "needle");

			int[] haystackData = haystack.data();
			int[] needleData = needle.data();
			int haystackWidth = haystack.width();
			int needleWidth = needle.width();
			int needleHeight = needle.height();

			return find(haystack, needle, (needleX, needleY) -> {
				for (int y = 0; y < needleHeight; y++) {
					for (int x = 0; x < needleWidth; x++) {
						int haystackColor = haystackData[(needleY + y) * haystackWidth + needleX + x];
						int needleColor = needleData[y * needleWidth + x];

						if (haystackColor != needleColor) {
							return false;
						}
					}
				}

				return true;
			});
		}

		@Override
		@Nullable
		public Vector2i findGrayscale(RawImage<byte[]> haystack, RawImage<byte[]> needle) {
			Preconditions.checkNotNull(haystack, "haystack");
			Preconditions.checkNotNull(needle, "needle");

			byte[] haystackData = haystack.data();
			byte[] needleData = needle.data();
			int haystackWidth = haystack.width();
			int needleWidth = needle.width();
			int needleHeight = needle.height();

			return find(haystack, needle, (needleX, needleY) -> {
				for (int y = 0; y < needleHeight; y++) {
					for (int x = 0; x < needleWidth; x++) {
						byte haystackLuminance = haystackData[(needleY + y) * haystackWidth + needleX + x];
						byte needleLuminance = needleData[y * needleWidth + x];

						if (haystackLuminance != needleLuminance) {
							return false;
						}
					}
				}

				return true;
			});
		}
	}

	@Nullable
	private static Vector2i find(TestScreenshotComparisonAlgorithm.RawImage<?> haystack, TestScreenshotComparisonAlgorithm.RawImage<?> needle, PositionPredicate predicate) {
		if (needle.width() > haystack.width() || needle.height() > haystack.height()) {
			return null;
		}

		return IntStream.rangeClosed(0, haystack.height() - needle.height())
				.parallel()
				.mapToObj(needleY -> {
					int maxNeedleX = haystack.width() - needle.width();

					for (int needleX = 0; needleX <= maxNeedleX; needleX++) {
						if (predicate.isAt(needleX, needleY)) {
							return new Vector2i(needleX, needleY);
						}
					}

					return null;
				})
				.filter(Objects::nonNull)
				.findAny()
				.orElse(null);
	}

	@FunctionalInterface
	private interface PositionPredicate {
		boolean isAt(int needleX, int needleY);
	}

	public record RawImageImpl<DATA>(int width, int height, DATA data) implements TestScreenshotComparisonAlgorithm.RawImage<DATA> {
		public static TestScreenshotComparisonAlgorithm.RawImage<int[]> toColor(TestScreenshotComparisonAlgorithm.RawImage<byte[]> grayscaleImage) {
			byte[] grayscale = grayscaleImage.data();
			int[] color = new int[grayscale.length];

			for (int i = 0; i < grayscale.length; i++) {
				int luminance = grayscale[i] & 0xFF;
				color[i] = luminance << 16 | luminance << 8 | luminance;
			}

			return new RawImageImpl<>(grayscaleImage.width(), grayscaleImage.height(), color);
		}

		public static TestScreenshotComparisonAlgorithm.RawImage<byte[]> fromGrayscaleNativeImage(NativeImage image) {
			return new RawImageImpl<>(image.getWidth(), image.getHeight(), ((NativeImageHooks) (Object) image).fabric_copyPixelsLuminance());
		}

		public static TestScreenshotComparisonAlgorithm.RawImage<int[]> fromColorNativeImage(NativeImage image) {
			return new RawImageImpl<>(image.getWidth(), image.getHeight(), ((NativeImageHooks) (Object) image).fabric_copyPixelsRgb());
		}
	}
}
