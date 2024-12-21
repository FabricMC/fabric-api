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

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import com.google.common.base.Preconditions;
import org.apache.commons.lang3.function.FailableConsumer;
import org.apache.commons.lang3.function.FailableFunction;
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.apache.commons.lang3.mutable.MutableObject;
import org.jetbrains.annotations.Nullable;

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
import net.minecraft.client.tutorial.TutorialStep;
import net.minecraft.client.util.ScreenshotRecorder;
import net.minecraft.sound.SoundCategory;
import net.minecraft.text.Text;
import net.minecraft.util.Nullables;

import net.fabricmc.fabric.api.client.gametest.v1.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.TestWorldBuilder;
import net.fabricmc.fabric.mixin.client.gametest.CyclingButtonWidgetAccessor;
import net.fabricmc.fabric.mixin.client.gametest.GameOptionsAccessor;
import net.fabricmc.fabric.mixin.client.gametest.ScreenAccessor;
import net.fabricmc.loader.api.FabricLoader;

public final class ClientGameTestContextImpl implements ClientGameTestContext {
	private final TestInputImpl input = new TestInputImpl(this);

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
	public Path takeScreenshot(String name) {
		ThreadingImpl.checkOnGametestThread("takeScreenshot");
		Preconditions.checkNotNull(name, "name");
		return takeScreenshot(name, 1);
	}

	@Override
	public Path takeScreenshot(String name, int delay) {
		ThreadingImpl.checkOnGametestThread("takeScreenshot");
		Preconditions.checkNotNull(name, "name");
		Preconditions.checkArgument(delay >= 0, "delay cannot be negative");

		waitTicks(delay);
		runOnClient(client -> {
			ScreenshotRecorder.saveScreenshot(FabricLoader.getInstance().getGameDir().toFile(), name + ".png", client.getFramebuffer(), (message) -> {
			});
		});

		return FabricLoader.getInstance().getGameDir().resolve("screenshots").resolve(name + ".png");
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
