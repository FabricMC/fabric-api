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
import java.util.Map;
import java.util.Objects;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import com.google.common.base.Preconditions;
import org.apache.commons.lang3.function.FailableConsumer;
import org.apache.commons.lang3.function.FailableFunction;
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.apache.commons.lang3.mutable.MutableObject;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector2i;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.CyclingButtonWidget;
import net.minecraft.client.gui.widget.PressableWidget;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.client.option.CloudRenderMode;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.option.SimpleOption;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.tutorial.TutorialStep;
import net.minecraft.client.util.ScreenshotRecorder;
import net.minecraft.client.util.math.Rect2i;
import net.minecraft.sound.SoundCategory;
import net.minecraft.text.Text;
import net.minecraft.util.Nullables;

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
import net.fabricmc.fabric.mixin.client.gametest.CyclingButtonWidgetAccessor;
import net.fabricmc.fabric.mixin.client.gametest.ScreenAccessor;
import net.fabricmc.fabric.mixin.client.gametest.lifecycle.GameOptionsAccessor;
import net.fabricmc.fabric.mixin.client.gametest.screenshot.RenderTickCounterConstantAccessor;
import net.fabricmc.loader.api.FabricLoader;

public final class ClientGameTestContextImpl implements ClientGameTestContext {
	private static final Logger LOGGER = LoggerFactory.getLogger("fabric-client-gametest-api-v1");

	private final TestInputImpl input = new TestInputImpl(this);
	private static int screenshotCounter = 0;

	private static final Map<String, Object> DEFAULT_GAME_OPTIONS = new HashMap<>();

	public static void initGameOptions(GameOptions options) {
		// Messes with the consistency of gametests
		options.tutorialStep = TutorialStep.NONE;
		options.getCloudRenderMode().setValue(CloudRenderMode.OFF);

		// Messes with game tests starting
		options.onboardAccessibility = false;

		// Makes chunk rendering finish sooner
		options.getViewDistance().setValue(5);

		// Just annoying
		options.getSoundVolumeOption(SoundCategory.MUSIC).setValue(0.0);

		((GameOptionsAccessor) options).invokeAccept(new GameOptions.Visitor() {
			@Override
			public int visitInt(String key, int current) {
				DEFAULT_GAME_OPTIONS.put(key, current);
				return current;
			}

			@Override
			public boolean visitBoolean(String key, boolean current) {
				DEFAULT_GAME_OPTIONS.put(key, current);
				return current;
			}

			@Override
			public String visitString(String key, String current) {
				DEFAULT_GAME_OPTIONS.put(key, current);
				return current;
			}

			@Override
			public float visitFloat(String key, float current) {
				DEFAULT_GAME_OPTIONS.put(key, current);
				return current;
			}

			@Override
			public <T> T visitObject(String key, T current, Function<String, T> decoder, Function<T, String> encoder) {
				DEFAULT_GAME_OPTIONS.put(key, current);
				return current;
			}

			@Override
			public <T> void accept(String key, SimpleOption<T> option) {
				DEFAULT_GAME_OPTIONS.put(key, option.getValue());
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
	public int waitFor(Predicate<MinecraftClient> predicate) {
		ThreadingImpl.checkOnGametestThread("waitFor");
		Preconditions.checkNotNull(predicate, "predicate");
		return waitFor(predicate, DEFAULT_TIMEOUT);
	}

	@Override
	public int waitFor(Predicate<MinecraftClient> predicate, int timeout) {
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
			return waitFor(client -> client.currentScreen == null);
		} else {
			return waitFor(client -> screenClass.isInstance(client.currentScreen));
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
			if (!tryClickScreenButtonImpl(client.currentScreen, translationKey)) {
				throw new AssertionError("Could not find button '%s' in screen '%s'".formatted(
					translationKey,
					Nullables.map(client.currentScreen, screen -> screen.getClass().getName())
				));
			}
		});
	}

	@Override
	public boolean tryClickScreenButton(String translationKey) {
		ThreadingImpl.checkOnGametestThread("tryClickScreenButton");
		Preconditions.checkNotNull(translationKey, "translationKey");

		return computeOnClient(client -> tryClickScreenButtonImpl(client.currentScreen, translationKey));
	}

	private static boolean tryClickScreenButtonImpl(@Nullable Screen screen, String translationKey) {
		if (screen == null) {
			return false;
		}

		final String buttonText = Text.translatable(translationKey).getString();
		final ScreenAccessor screenAccessor = (ScreenAccessor) screen;

		for (Drawable drawable : screenAccessor.getDrawables()) {
			if (drawable instanceof PressableWidget pressableWidget && pressMatchingButton(pressableWidget, buttonText)) {
				return true;
			}

			if (drawable instanceof Widget widget) {
				MutableBoolean found = new MutableBoolean(false);
				widget.forEachChild(clickableWidget -> {
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

	private static boolean pressMatchingButton(ClickableWidget widget, String text) {
		if (widget instanceof ButtonWidget buttonWidget) {
			if (text.equals(buttonWidget.getMessage().getString())) {
				buttonWidget.onPress();
				return true;
			}
		}

		if (widget instanceof CyclingButtonWidget<?> buttonWidget) {
			CyclingButtonWidgetAccessor accessor = (CyclingButtonWidgetAccessor) buttonWidget;

			if (text.equals(accessor.getOptionText().getString())) {
				buttonWidget.onPress();
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
		return computeOnClient(client -> {
			try (NativeImage screenshot = doTakeScreenshot(client, optionsImpl)) {
				return saveScreenshot(screenshot, optionsImpl.name, optionsImpl);
			}
		});
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
		return this.computeOnClient(client -> {
			try (NativeImage screenshot = doTakeScreenshot(client, optionsImpl)) {
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
						throw new AssertionError("Screenshot does not contain template");
					}

					return result.add(region.getX(), region.getY());
				}
			}
		});
	}

	private static NativeImage doTakeScreenshot(MinecraftClient client, TestScreenshotCommonOptionsImpl<?> options) {
		int prevWidth = client.getWindow().getFramebufferWidth();
		int prevHeight = client.getWindow().getFramebufferHeight();

		if (options.size != null) {
			client.getWindow().setFramebufferWidth(options.size.x);
			client.getWindow().setFramebufferHeight(options.size.y);
			client.getFramebuffer().resize(options.size.x, options.size.y);
		}

		try {
			client.gameRenderer.render(RenderTickCounterConstantAccessor.create(options.tickDelta), true);

			// The vanilla panorama screenshot code has a Thread.sleep(10) here, is this needed?

			return ScreenshotRecorder.takeScreenshot(client.getFramebuffer());
		} finally {
			if (options.size != null) {
				client.getWindow().setFramebufferWidth(prevWidth);
				client.getWindow().setFramebufferHeight(prevHeight);
				client.getFramebuffer().resize(prevWidth, prevHeight);
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

		String counterPrefix = options.counterPrefix ? "%04d_".formatted(screenshotCounter++) : "";
		Path screenshotFile = destinationDir.resolve(counterPrefix + fileName + ".png");

		try {
			screenshot.writeTo(screenshotFile);
		} catch (IOException e) {
			throw new AssertionError("Failed to write screenshot file", e);
		}

		return screenshotFile;
	}

	private static void onTemplateImageDoesntExist(NativeImage subScreenshot, TestScreenshotComparisonOptionsImpl options) {
		if (TestSystemProperties.TEST_MOD_RESOURCES_PATH != null) {
			Path savePath = Path.of(TestSystemProperties.TEST_MOD_RESOURCES_PATH).resolve("templates").resolve(options.getTemplateImagePath() + ".png");

			try {
				Files.createDirectories(savePath.getParent());
				subScreenshot.writeTo(savePath);
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
			((GameOptionsAccessor) MinecraftClient.getInstance().options).invokeAccept(new GameOptions.Visitor() {
				@Override
				public int visitInt(String key, int current) {
					return (Integer) DEFAULT_GAME_OPTIONS.get(key);
				}

				@Override
				public boolean visitBoolean(String key, boolean current) {
					return (Boolean) DEFAULT_GAME_OPTIONS.get(key);
				}

				@Override
				public String visitString(String key, String current) {
					return (String) DEFAULT_GAME_OPTIONS.get(key);
				}

				@Override
				public float visitFloat(String key, float current) {
					return (Float) DEFAULT_GAME_OPTIONS.get(key);
				}

				@SuppressWarnings("unchecked")
				@Override
				public <T> T visitObject(String key, T current, Function<String, T> decoder, Function<T, String> encoder) {
					return (T) DEFAULT_GAME_OPTIONS.get(key);
				}

				@SuppressWarnings("unchecked")
				@Override
				public <T> void accept(String key, SimpleOption<T> option) {
					option.setValue((T) DEFAULT_GAME_OPTIONS.get(key));
				}
			});
		});
	}

	@Override
	public <E extends Throwable> void runOnClient(FailableConsumer<MinecraftClient, E> action) throws E {
		ThreadingImpl.checkOnGametestThread("runOnClient");
		Preconditions.checkNotNull(action, "action");

		ThreadingImpl.runOnClient(() -> action.accept(MinecraftClient.getInstance()));
	}

	@Override
	public <T, E extends Throwable> T computeOnClient(FailableFunction<MinecraftClient, T, E> function) throws E {
		ThreadingImpl.checkOnGametestThread("computeOnClient");
		Preconditions.checkNotNull(function, "function");

		MutableObject<T> result = new MutableObject<>();
		ThreadingImpl.runOnClient(() -> result.setValue(function.apply(MinecraftClient.getInstance())));
		return result.getValue();
	}
}
