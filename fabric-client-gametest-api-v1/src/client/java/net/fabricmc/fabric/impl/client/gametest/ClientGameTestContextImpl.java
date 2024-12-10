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

import com.google.common.base.Preconditions;
import org.apache.commons.lang3.function.FailableConsumer;
import org.apache.commons.lang3.function.FailableFunction;
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
import net.minecraft.text.Text;
import net.minecraft.util.Nullables;

import net.fabricmc.fabric.api.client.gametest.v1.ClientGameTestContext;
import net.fabricmc.fabric.mixin.client.gametest.CyclingButtonWidgetAccessor;
import net.fabricmc.fabric.mixin.client.gametest.ScreenAccessor;

public final class ClientGameTestContextImpl implements ClientGameTestContext {
	@Override
	public void waitTick() {
		ThreadingImpl.runTick();
	}

	@Override
	public void waitTicks(int ticks) {
		Preconditions.checkArgument(ticks >= 0, "ticks cannot be negative");

		for (int i = 0; i < ticks; i++) {
			ThreadingImpl.runTick();
		}
	}

	@Override
	public void setScreen(@Nullable Screen screen) {
		runOnClient(client -> client.setScreen(screen));
	}

	@Override
	public void clickScreenButton(String translationKey) {
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
				widget.forEachChild(clickableWidget -> pressMatchingButton(clickableWidget, buttonText));
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
	public <E extends Throwable> void runOnClient(FailableConsumer<MinecraftClient, E> action) throws E {
		Preconditions.checkNotNull(action, "action");

		ThreadingImpl.runOnClient(() -> action.accept(MinecraftClient.getInstance()));
	}

	@Override
	public <T, E extends Throwable> T computeOnClient(FailableFunction<MinecraftClient, T, E> function) throws E {
		Preconditions.checkNotNull(function, "function");

		MutableObject<T> result = new MutableObject<>();
		ThreadingImpl.runOnClient(() -> result.setValue(function.apply(MinecraftClient.getInstance())));
		return result.getValue();
	}
}
