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

package net.fabricmc.fabric.impl.client.gametest.context;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import com.google.common.base.Preconditions;
import com.mojang.blaze3d.platform.NativeImage;
import org.apache.commons.lang3.function.FailableConsumer;
import org.apache.commons.lang3.function.FailableFunction;
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.apache.commons.lang3.mutable.MutableObject;
import org.joml.Vector2i;
import org.jspecify.annotations.Nullable;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.minecraft.Optionull;
import net.minecraft.client.CloudStatus;
import net.minecraft.client.Minecraft;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.Options;
import net.minecraft.client.Screenshot;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.layouts.LayoutElement;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonInfo;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.client.tutorial.TutorialSteps;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;

import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.screenshot.TestScreenshotComparisonAlgorithm;
import net.fabricmc.fabric.api.client.gametest.v1.screenshot.TestScreenshotComparisonOptions;
import net.fabricmc.fabric.api.client.gametest.v1.screenshot.TestScreenshotOptions;
import net.fabricmc.fabric.api.client.gametest.v1.world.TestWorldBuilder;
import net.fabricmc.fabric.impl.client.gametest.TestInputImpl;
import net.fabricmc.fabric.impl.client.gametest.TestSystemProperties;
import net.fabricmc.fabric.impl.client.gametest.screenshot.TestScreenshotCommonOptionsImpl;
import net.fabricmc.fabric.impl.client.gametest.screenshot.TestScreenshotComparisonAlgorithms;
import net.fabricmc.fabric.impl.client.gametest.screenshot.TestScreenshotComparisonOptionsImpl;
import net.fabricmc.fabric.impl.client.gametest.screenshot.TestScreenshotOptionsImpl;
import net.fabricmc.fabric.impl.client.gametest.threading.ThreadingImpl;
import net.fabricmc.fabric.impl.client.gametest.world.TestWorldBuilderImpl;
import net.fabricmc.fabric.mixin.client.gametest.gui.CycleButtonAccessor;
import net.fabricmc.fabric.mixin.client.gametest.gui.ScreenAccessor;
import net.fabricmc.fabric.mixin.client.gametest.lifecycle.OptionsAccessor;
import net.fabricmc.fabric.mixin.client.gametest.screenshot.DeltaTrackerDefaultValueAccessor;
import net.fabricmc.loader.api.FabricLoader;

public final class ClientGameTestContextImpl implements ClientGameTestContext {
	private static final Logger LOGGER = LoggerFactory.getLogger("fabric-client-gametest-api-v1");

	private final TestInputImpl input = new TestInputImpl(this);
	private static int screenshotCounter = 0;

	private static final Map<String, Object> DEFAULT_GAME_OPTIONS = new HashMap<>();

	public static void initGameOptions(Options options) {
		// Messes with the consistency of gametests
		options.tutorialStep = TutorialSteps.NONE;
		options.cloudStatus().set(CloudStatus.OFF);

		// Messes with game tests starting
		options.onboardAccessibility = false;

		// Makes chunk rendering finish sooner
		options.renderDistance().set(5);

		// Just annoying
		options.getSoundSourceOptionInstance(SoundSource.MUSIC).set(0.0);

		// Disable Anisotropic Filtering
		options.maxAnisotropyBit().set(0);

		// Disable chunk fade
		options.chunkSectionFadeInTime().set(0D);

		((OptionsAccessor) options).invokeAccept(new Options.FieldAccess() {
			@Override
			public int process(String key, int current) {
				DEFAULT_GAME_OPTIONS.put(key, current);
				return current;
			}

			@Override
			public boolean process(String key, boolean current) {
				DEFAULT_GAME_OPTIONS.put(key, current);
				return current;
			}

			@Override
			public String process(String key, String current) {
				DEFAULT_GAME_OPTIONS.put(key, current);
				return current;
			}

			@Override
			public float process(String key, float current) {
				DEFAULT_GAME_OPTIONS.put(key, current);
				return current;
			}

			@Override
			public <T> T process(String key, T current, Function<String, T> decoder, Function<T, String> encoder) {
				DEFAULT_GAME_OPTIONS.put(key, current);
				return current;
			}

			@Override
			public <T> void process(String key, OptionInstance<T> option) {
				DEFAULT_GAME_OPTIONS.put(key, option.get());
			}
		});
	}

	@Override
	public void waitTick() {
		ThreadingImpl.checkOnGametestThread("waitTick");
		ThreadingImpl.runTick();
	}

	@Override
	public void waitTicks(int ticks) {
		ThreadingImpl.checkOnGametestThread("waitTicks");
		Preconditions.checkArgument(ticks >= 0, "ticks cannot be negative");

		for (int i = 0; i < ticks; i++) {
			ThreadingImpl.runTick();
		}
	}

	@Override
	public int waitFor(Predicate<Minecraft> predicate) {
		ThreadingImpl.checkOnGametestThread("waitFor");
		Preconditions.checkNotNull(predicate, "predicate");
		return waitFor(predicate, DEFAULT_TIMEOUT);
	}

	@Override
	public int waitFor(Predicate<Minecraft> predicate, int timeout) {
		ThreadingImpl.checkOnGametestThread("waitFor");
		Preconditions.checkNotNull(predicate, "predicate");

		if (timeout == NO_TIMEOUT) {
			int ticksWaited = 0;

			while (!computeOnClient(predicate::test)) {
				ticksWaited++;
				ThreadingImpl.runTick();
			}

			return ticksWaited;
		} else {
			Preconditions.checkArgument(timeout > 0, "timeout must be positive");

			for (int i = 0; i < timeout; i++) {
				if (computeOnClient(predicate::test)) {
					return i;
				}

				ThreadingImpl.runTick();
			}

			if (!computeOnClient(predicate::test)) {
				throw new AssertionError("Timed out waiting for predicate");
			}

			return timeout;
		}
	}

	@Override
	public int waitForScreen(@Nullable Class<? extends Screen> screenClass) {
		ThreadingImpl.checkOnGametestThread("waitForScreen");

		if (screenClass == null) {
			return waitFor(client -> client.screen == null);
		} else {
			return waitFor(client -> screenClass.isInstance(client.screen));
		}
	}

	@Override
	public void setScreen(Supplier<@Nullable Screen> screen) {
		ThreadingImpl.checkOnGametestThread("setScreen");
		runOnClient(client -> client.setScreen(screen.get()));
	}

	@Override
	public void clickScreenButton(String translationKey) {
		ThreadingImpl.checkOnGametestThread("clickScreenButton");
		Preconditions.checkNotNull(translationKey, "translationKey");

		runOnClient(client -> {
			if (!tryClickScreenButtonImpl(client.screen, translationKey)) {
				throw new AssertionError("Could not find button '%s' in screen '%s'".formatted(
					translationKey,
					Optionull.map(client.screen, screen -> screen.getClass().getName())
				));
			}
		});
	}

	@Override
	public boolean tryClickScreenButton(String translationKey) {
		ThreadingImpl.checkOnGametestThread("tryClickScreenButton");
		Preconditions.checkNotNull(translationKey, "translationKey");

		return computeOnClient(client -> tryClickScreenButtonImpl(client.screen, translationKey));
	}

	private static boolean tryClickScreenButtonImpl(@Nullable Screen screen, String translationKey) {
		if (screen == null) {
			return false;
		}

		final String buttonText = Component.translatable(translationKey).getString();
		final ScreenAccessor screenAccessor = (ScreenAccessor) screen;

		for (Renderable drawable : screenAccessor.getDrawables()) {
			if (drawable instanceof AbstractButton pressableWidget && pressMatchingButton(pressableWidget, buttonText)) {
				return true;
			}

			if (drawable instanceof LayoutElement widget) {
				MutableBoolean found = new MutableBoolean(false);
				widget.visitWidgets(clickableWidget -> {
					if (!found.booleanValue()) {
						found.setValue(pressMatchingButton(clickableWidget, buttonText));
					}
				});

				if (found.booleanValue()) {
					return true;
				}
			}
		}

		// Was unable to find the button to press
		return false;
	}

	private static boolean pressMatchingButton(AbstractWidget widget, String text) {
		var clickEvent = new MouseButtonInfo(GLFW.GLFW_KEY_UNKNOWN, 0);

		if (widget instanceof Button buttonWidget) {
			if (text.equals(buttonWidget.getMessage().getString())) {
				buttonWidget.onPress(clickEvent);
				return true;
			}
		}

		if (widget instanceof CycleButton<?> buttonWidget) {
			CycleButtonAccessor accessor = (CycleButtonAccessor) buttonWidget;

			if (text.equals(accessor.getOptionText().getString())) {
				buttonWidget.onPress(clickEvent);
				return true;
			}
		}

		return false;
	}

	@Override
	public Path takeScreenshot(TestScreenshotOptions options) {
		ThreadingImpl.checkOnGametestThread("takeScreenshot");
		Preconditions.checkNotNull(options, "options");

		TestScreenshotOptionsImpl optionsImpl = (TestScreenshotOptionsImpl) options;
		return doTakeScreenshot(optionsImpl, screenshot -> saveScreenshot(screenshot, optionsImpl.name, optionsImpl));
	}

	@Override
	public void assertScreenshotEquals(TestScreenshotComparisonOptions options) {
		ThreadingImpl.checkOnGametestThread("assertScreenshotEquals");
		Preconditions.checkNotNull(options, "options");
		doAssertScreenshotContains(options, (haystackImage, needleImage) -> haystackImage.width() == needleImage.width() && haystackImage.height() == needleImage.height());
	}

	@Override
	public Vector2i assertScreenshotContains(TestScreenshotComparisonOptions options) {
		ThreadingImpl.checkOnGametestThread("assertScreenshotContains");
		Preconditions.checkNotNull(options, "options");
		return doAssertScreenshotContains(options, (haystackImage, needleImage) -> true);
	}

	private Vector2i doAssertScreenshotContains(
			TestScreenshotComparisonOptions options,
			BiPredicate<TestScreenshotComparisonAlgorithm.RawImage<?>, TestScreenshotComparisonAlgorithm.RawImage<?>> preCheck
	) {
		TestScreenshotComparisonOptionsImpl optionsImpl = (TestScreenshotComparisonOptionsImpl) options;
		return doTakeScreenshot(optionsImpl, screenshot -> {
			Rect2i region = optionsImpl.region == null ? new Rect2i(0, 0, screenshot.getWidth(), screenshot.getHeight()) : optionsImpl.region;
			Preconditions.checkState(region.getX() + region.getWidth() <= screenshot.getWidth() && region.getY() + region.getHeight() <= screenshot.getHeight(), "Screenshot comparison region extends outside the screenshot");

			try (NativeImage subScreenshot = new NativeImage(region.getWidth(), region.getHeight(), false)) {
				screenshot.resizeSubRectTo(region.getX(), region.getY(), region.getWidth(), region.getHeight(), subScreenshot);

				if (optionsImpl.savedFileName != null) {
					saveScreenshot(subScreenshot, optionsImpl.savedFileName, optionsImpl);
				}

				Vector2i result;

				if (optionsImpl.grayscale) {
					TestScreenshotComparisonAlgorithm.RawImage<byte[]> templateImage = optionsImpl.getGrayscaleTemplateImage();

					if (templateImage == null) {
						onTemplateImageDoesntExist(subScreenshot, optionsImpl);
						return new Vector2i(region.getX(), region.getY());
					}

					TestScreenshotComparisonAlgorithm.RawImage<byte[]> haystackImage = TestScreenshotComparisonAlgorithms.RawImageImpl.fromGrayscaleNativeImage(subScreenshot);

					if (preCheck.test(haystackImage, templateImage)) {
						result = optionsImpl.algorithm.findGrayscale(haystackImage, templateImage);
					} else {
						result = null;
					}
				} else {
					TestScreenshotComparisonAlgorithm.RawImage<int[]> templateImage = optionsImpl.getColorTemplateImage();

					if (templateImage == null) {
						onTemplateImageDoesntExist(subScreenshot, optionsImpl);
						return new Vector2i(region.getX(), region.getY());
					}

					TestScreenshotComparisonAlgorithm.RawImage<int[]> haystackImage = TestScreenshotComparisonAlgorithms.RawImageImpl.fromColorNativeImage(subScreenshot);

					if (preCheck.test(haystackImage, templateImage)) {
						result = optionsImpl.algorithm.findColor(haystackImage, templateImage);
					} else {
						result = null;
					}
				}

				if (result == null) {
					throw new AssertionError("Screenshot does not contain template" + optionsImpl.getTemplateImagePath().map(" '%s'"::formatted).orElse(""));
				}

				return result.add(region.getX(), region.getY());
			}
		});
	}

	private <T> T doTakeScreenshot(TestScreenshotCommonOptionsImpl<?> options, Function<NativeImage, T> screenshotConsumer) {
		ThreadingImpl.checkOnGametestThread("doTakeScreenshot");

		Vector2i prevSize = computeOnClient(client -> {
			int prevWidth = client.getWindow().getWidth();
			int prevHeight = client.getWindow().getHeight();

			if (options.size != null) {
				client.getWindow().setWidth(options.size.x);
				client.getWindow().setHeight(options.size.y);
				client.getMainRenderTarget().resize(options.size.x, options.size.y);
			}

			return new Vector2i(prevWidth, prevHeight);
		});

		try {
			CompletableFuture<T> future = computeOnClient(client -> {
				client.gameRenderer.render(DeltaTrackerDefaultValueAccessor.create(options.tickDelta), true);
				CompletableFuture<T> resultFuture = new CompletableFuture<>();

				Screenshot.takeScreenshot(client.getMainRenderTarget(), screenshot -> {
					try {
						resultFuture.complete(screenshotConsumer.apply(screenshot));
					} catch (Throwable e) {
						resultFuture.completeExceptionally(e);
					}
				});

				return resultFuture;
			});

			// Keep ticking until the screenshot is done
			while (!future.isDone()) {
				waitTick();
			}

			return future.get();
		} catch (ExecutionException | InterruptedException e) {
			throw new RuntimeException(e);
		} finally {
			if (options.size != null) {
				computeOnClient(client -> {
					client.getWindow().setWidth(prevSize.x);
					client.getWindow().setHeight(prevSize.y);
					client.getMainRenderTarget().resize(prevSize.x, prevSize.y);
					return null;
				});
			}
		}
	}

	private static Path saveScreenshot(NativeImage screenshot, String fileName, TestScreenshotCommonOptionsImpl<?> options) {
		Path destinationDir = Objects.requireNonNullElseGet(options.destinationDir, () -> FabricLoader.getInstance().getGameDir().resolve("screenshots"));

		try {
			Files.createDirectories(destinationDir);
		} catch (IOException e) {
			throw new AssertionError("Failed to create screenshots directory", e);
		}

		String counterPrefix = options.counterPrefix ? String.format(Locale.ROOT, "%04d_", screenshotCounter++) : "";
		Path screenshotFile = destinationDir.resolve(counterPrefix + fileName + ".png");

		try {
			screenshot.writeToFile(screenshotFile);
		} catch (IOException e) {
			throw new AssertionError("Failed to write screenshot file", e);
		}

		return screenshotFile;
	}

	private static void onTemplateImageDoesntExist(NativeImage subScreenshot, TestScreenshotComparisonOptionsImpl options) {
		if (TestSystemProperties.TEST_MOD_RESOURCES_PATH != null) {
			Path savePath = Path.of(TestSystemProperties.TEST_MOD_RESOURCES_PATH).resolve("templates").resolve(options.getTemplateImagePathOrThrow() + ".png");

			try {
				Files.createDirectories(savePath.getParent());
				subScreenshot.writeToFile(savePath);
			} catch (IOException e) {
				throw new AssertionError("Failed to write screenshot file", e);
			}

			LOGGER.info("Written absent screenshot template to {}", savePath);
		} else {
			LOGGER.error("The template image does not exist. Set the fabric.client.gametest.testModResourcesPath system property to your test mod resources file path to automatically save it");
			throw new AssertionError("Template image does not exist");
		}
	}

	@Override
	public TestInputImpl getInput() {
		return input;
	}

	@Override
	public TestWorldBuilder worldBuilder() {
		return new TestWorldBuilderImpl(this);
	}

	@Override
	public void restoreDefaultGameOptions() {
		ThreadingImpl.checkOnGametestThread("restoreDefaultGameOptions");

		runOnClient(client -> {
			((OptionsAccessor) Minecraft.getInstance().options).invokeAccept(new Options.FieldAccess() {
				@Override
				public int process(String key, int current) {
					return (Integer) DEFAULT_GAME_OPTIONS.get(key);
				}

				@Override
				public boolean process(String key, boolean current) {
					return (Boolean) DEFAULT_GAME_OPTIONS.get(key);
				}

				@Override
				public String process(String key, String current) {
					return (String) DEFAULT_GAME_OPTIONS.get(key);
				}

				@Override
				public float process(String key, float current) {
					return (Float) DEFAULT_GAME_OPTIONS.get(key);
				}

				@SuppressWarnings("unchecked")
				@Override
				public <T> T process(String key, T current, Function<String, T> decoder, Function<T, String> encoder) {
					return (T) DEFAULT_GAME_OPTIONS.get(key);
				}

				@SuppressWarnings("unchecked")
				@Override
				public <T> void process(String key, OptionInstance<T> option) {
					option.set((T) DEFAULT_GAME_OPTIONS.get(key));
				}
			});
		});
	}

	@Override
	public <E extends Throwable> void runOnClient(FailableConsumer<Minecraft, E> action) throws E {
		ThreadingImpl.checkOnGametestThread("runOnClient");
		Preconditions.checkNotNull(action, "action");

		ThreadingImpl.runOnClient(() -> action.accept(Minecraft.getInstance()));
	}

	@Override
	public <T, E extends Throwable> T computeOnClient(FailableFunction<Minecraft, T, E> function) throws E {
		ThreadingImpl.checkOnGametestThread("computeOnClient");
		Preconditions.checkNotNull(function, "function");

		MutableObject<T> result = new MutableObject<>();
		ThreadingImpl.runOnClient(() -> result.setValue(function.apply(Minecraft.getInstance())));
		return result.getValue();
	}
}
