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

package net.fabricmc.fabric.test.base.client;

import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;

import org.apache.commons.lang3.function.FailableConsumer;
import org.apache.commons.lang3.function.FailableFunction;
import org.apache.commons.lang3.mutable.MutableObject;

import net.minecraft.SharedConstants;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.screen.GameMenuScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.screen.ingame.CreativeInventoryScreen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.gui.screen.multiplayer.ConnectScreen;
import net.minecraft.client.gui.screen.world.LevelLoadingScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.CyclingButtonWidget;
import net.minecraft.client.gui.widget.PressableWidget;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.client.network.ServerAddress;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.client.option.Perspective;
import net.minecraft.client.util.ScreenshotRecorder;
import net.minecraft.text.Text;

import net.fabricmc.fabric.test.base.client.mixin.CyclingButtonWidgetAccessor;
import net.fabricmc.fabric.test.base.client.mixin.ScreenAccessor;
import net.fabricmc.fabric.test.base.client.mixin.TitleScreenAccessor;
import net.fabricmc.loader.api.FabricLoader;

public final class FabricClientTestHelper {
	public static void waitForLoadingComplete() {
		// client is not ticking and can't accept tasks, waitFor doesn't work so we'll do this until then
		while (!ThreadingImpl.clientCanAcceptTasks) {
			runTick();

			try {
				//noinspection BusyWait
				Thread.sleep(50);
			} catch (InterruptedException e) {
				throw new RuntimeException(e);
			}
		}

		waitFor("Loading to complete", client -> client.getOverlay() == null, 5 * SharedConstants.TICKS_PER_MINUTE);
	}

	public static void waitForScreen(Class<? extends Screen> screenClass) {
		waitFor("Screen %s".formatted(screenClass.getName()), client -> client.currentScreen != null && client.currentScreen.getClass() == screenClass);
	}

	public static void openGameMenu() {
		setScreen((client) -> new GameMenuScreen(true));
		waitForScreen(GameMenuScreen.class);
	}

	public static void openInventory() {
		setScreen((client) -> new InventoryScreen(Objects.requireNonNull(client.player)));

		boolean creative = computeOnClient(client -> Objects.requireNonNull(client.player).isCreative());
		waitForScreen(creative ? CreativeInventoryScreen.class : InventoryScreen.class);
	}

	public static void closeScreen() {
		setScreen((client) -> null);
	}

	private static void setScreen(Function<MinecraftClient, Screen> screenSupplier) {
		runOnClient(client -> client.setScreen(screenSupplier.apply(client)));
	}

	public static void takeScreenshot(String name) {
		takeScreenshot(name, 1);
	}

	public static void takeScreenshot(String name, int delayTicks) {
		// Allow time for any screens to open
		runTicks(delayTicks);

		runOnClient(client -> {
			ScreenshotRecorder.saveScreenshot(FabricLoader.getInstance().getGameDir().toFile(), name + ".png", client.getFramebuffer(), (message) -> {
			});
		});
	}

	public static void clickScreenButton(String translationKey) {
		final String buttonText = Text.translatable(translationKey).getString();

		waitFor("Click button" + buttonText, client -> {
			final Screen screen = client.currentScreen;

			if (screen == null) {
				return false;
			}

			final ScreenAccessor screenAccessor = (ScreenAccessor) screen;

			for (Drawable drawable : screenAccessor.getDrawables()) {
				if (drawable instanceof PressableWidget pressableWidget && pressMatchingButton(pressableWidget, buttonText)) {
					return true;
				}

				if (drawable instanceof Widget widget) {
					widget.forEachChild(clickableWidget -> pressMatchingButton(clickableWidget, buttonText));
				}
			}

			// Was unable to find the button to press
			return false;
		});
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

	public static void waitForWorldTicks(long ticks) {
		// Wait for the world to be loaded and get the start ticks
		waitFor("World load", client -> client.world != null && !(client.currentScreen instanceof LevelLoadingScreen), 30 * SharedConstants.TICKS_PER_MINUTE);
		final long startTicks = computeOnClient(client -> client.world.getTime());
		waitFor("World load", client -> Objects.requireNonNull(client.world).getTime() > startTicks + ticks, 10 * SharedConstants.TICKS_PER_MINUTE);
	}

	public static void enableDebugHud() {
		runOnClient(client -> client.inGameHud.getDebugHud().toggleDebugHud());
	}

	public static void setPerspective(Perspective perspective) {
		runOnClient(client -> client.options.setPerspective(perspective));
	}

	public static void connectToServer(TestDedicatedServer server) {
		runOnClient(client -> {
			final var serverInfo = new ServerInfo("localhost", server.getConnectionAddress(), ServerInfo.ServerType.OTHER);
			ConnectScreen.connect(client.currentScreen, client, ServerAddress.parse(server.getConnectionAddress()), serverInfo, false, null);
		});
	}

	public static void waitForTitleScreenFade() {
		waitFor("Title screen fade", client -> {
			if (!(client.currentScreen instanceof TitleScreen titleScreen)) {
				return false;
			}

			return !((TitleScreenAccessor) titleScreen).getDoBackgroundFade();
		});
	}

	public static void waitForServerStop() {
		waitFor("Server stop", client -> !ThreadingImpl.isServerRunning, SharedConstants.TICKS_PER_MINUTE);
	}

	private static void waitFor(String what, Predicate<MinecraftClient> predicate) {
		waitFor(what, predicate, 10 * SharedConstants.TICKS_PER_SECOND);
	}

	private static void waitFor(String what, Predicate<MinecraftClient> predicate, int timeoutTicks) {
		int tickCount;

		for (tickCount = 0; tickCount < timeoutTicks && !computeOnClient(predicate::test); tickCount++) {
			runTick();
		}

		if (tickCount == timeoutTicks && !computeOnClient(predicate::test)) {
			throw new RuntimeException("Timed out waiting for " + what);
		}
	}

	public static void runTicks(int ticks) {
		for (int i = 0; i < ticks; i++) {
			runTick();
		}
	}

	public static void runTick() {
		ThreadingImpl.runTick();
	}

	public static <E extends Throwable> void runOnClient(FailableConsumer<MinecraftClient, E> action) throws E {
		ThreadingImpl.runOnClient(() -> action.accept(MinecraftClient.getInstance()));
	}

	public static <T, E extends Throwable> T computeOnClient(FailableFunction<MinecraftClient, T, E> action) throws E {
		MutableObject<T> result = new MutableObject<>();
		runOnClient(client -> result.setValue(action.apply(client)));
		return result.getValue();
	}
}
