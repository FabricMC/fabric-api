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

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;

import com.google.common.base.Preconditions;
import com.mojang.datafixers.util.Either;
import org.jetbrains.annotations.Nullable;

import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.util.math.Rect2i;

import net.fabricmc.fabric.api.client.gametest.v1.TestScreenshotComparisonAlgorithm;
import net.fabricmc.fabric.api.client.gametest.v1.TestScreenshotComparisonOptions;

public final class TestScreenshotComparisonOptionsImpl extends TestScreenshotCommonOptionsImpl<TestScreenshotComparisonOptions> implements TestScreenshotComparisonOptions {
	private final Either<String, NativeImage> templateImage;
	@Nullable
	public String savedFileName;
	public TestScreenshotComparisonAlgorithm algorithm = TestScreenshotComparisonAlgorithm.defaultAlgorithm();
	public boolean grayscale = false;
	@Nullable
	public Rect2i region;

	public TestScreenshotComparisonOptionsImpl(String templateImage) {
		this.templateImage = Either.left(templateImage);
	}

	public TestScreenshotComparisonOptionsImpl(NativeImage templateImage) {
		this.templateImage = Either.right(templateImage);
	}

	@Override
	public TestScreenshotComparisonOptions saveWithFileName(String fileName) {
		Preconditions.checkNotNull(fileName, "fileName");

		this.savedFileName = fileName;
		return this;
	}

	@Override
	public TestScreenshotComparisonOptions withAlgorithm(TestScreenshotComparisonAlgorithm algorithm) {
		Preconditions.checkNotNull(algorithm, "algorithm");

		this.algorithm = algorithm;
		return this;
	}

	@Override
	public TestScreenshotComparisonOptions withGrayscale() {
		this.grayscale = true;

		return this;
	}

	@Override
	public TestScreenshotComparisonOptions withRegion(int x, int y, int width, int height) {
		Preconditions.checkArgument(x >= 0, "x cannot be negative");
		Preconditions.checkArgument(y >= 0, "y cannot be negative");
		Preconditions.checkArgument(width > 0, "width must be positive");
		Preconditions.checkArgument(height > 0, "height must be positive");

		this.region = new Rect2i(x, y, width, height);
		return this;
	}

	public String getTemplateImagePath() {
		return this.templateImage.left().orElseThrow();
	}

	@Nullable
	public TestScreenshotComparisonAlgorithm.RawImage<byte[]> getGrayscaleTemplateImage() {
		return this.templateImage.map(fileName -> {
			try (NativeImage image = loadNativeImage(fileName)) {
				if (image == null) {
					return null;
				}

				return new TestScreenshotComparisonAlgorithms.RawImageImpl<>(
						image.getWidth(),
						image.getHeight(),
						((NativeImageHooks) (Object) image).fabric_copyPixelsLuminance()
				);
			}
		}, image -> {
			assertNoTransparency(image);
			return TestScreenshotComparisonAlgorithms.RawImageImpl.fromGrayscaleNativeImage(image);
		});
	}

	@Nullable
	public TestScreenshotComparisonAlgorithm.RawImage<int[]> getColorTemplateImage() {
		return this.templateImage.map(fileName -> {
			try (NativeImage image = loadNativeImage(fileName)) {
				if (image == null) {
					return null;
				}

				return new TestScreenshotComparisonAlgorithms.RawImageImpl<>(
						image.getWidth(),
						image.getHeight(),
						((NativeImageHooks) (Object) image).fabric_copyPixelsRgb()
				);
			}
		}, image -> {
			assertNoTransparency(image);
			return TestScreenshotComparisonAlgorithms.RawImageImpl.fromColorNativeImage(image);
		});
	}

	@Nullable
	private static NativeImage loadNativeImage(String templateImagePath) {
		Path filePath = FabricClientGameTestRunner.currentlyRunningGameTest.getProvider()
				.findPath("templates/" + templateImagePath + ".png")
				.orElse(null);

		if (filePath == null) {
			return null;
		}

		try (InputStream stream = Files.newInputStream(filePath)) {
			NativeImage image = NativeImage.read(stream);
			assertNoTransparency(image);
			return image;
		} catch (IOException e) {
			throw new UncheckedIOException("Failed to load template image", e);
		}
	}

	private static void assertNoTransparency(NativeImage image) {
		if (!((NativeImageHooks) (Object) image).fabric_isFullyOpaque()) {
			throw new AssertionError("Template image is partially transparent which is not supported");
		}
	}
}
