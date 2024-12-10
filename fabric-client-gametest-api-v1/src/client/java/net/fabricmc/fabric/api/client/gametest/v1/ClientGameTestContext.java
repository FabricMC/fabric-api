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

import org.apache.commons.lang3.function.FailableConsumer;
import org.apache.commons.lang3.function.FailableFunction;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;

/**
 * Context for a client gametest containing various helpful functions and functions to access the game.
 */
@ApiStatus.NonExtendable
public interface ClientGameTestContext {
	void waitTick();
	void waitTicks(int ticks);

	void setScreen(@Nullable Screen screen);
	void clickScreenButton(String translationKey);
	boolean tryClickScreenButton(String translationKey);

	<E extends Throwable> void runOnClient(FailableConsumer<MinecraftClient, E> action) throws E;
	<T, E extends Throwable> T computeOnClient(FailableFunction<MinecraftClient, T, E> function) throws E;
}
