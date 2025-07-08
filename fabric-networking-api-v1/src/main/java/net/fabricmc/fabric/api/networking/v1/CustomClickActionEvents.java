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

package net.fabricmc.fabric.api.networking.v1;

import java.util.Objects;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import net.minecraft.nbt.NbtElement;
import net.minecraft.server.network.ServerCommonNetworkHandler;
import net.minecraft.server.network.ServerConfigurationNetworkHandler;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.util.Identifier;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.impl.networking.CustomClickActionsRegistry;

public final class CustomClickActionEvents {
	public static Event<ClickActionReceived<PlayContext>> playClickActionEvent(Identifier id) {
		Objects.requireNonNull(id, "ID cannot be null");
		return CustomClickActionsRegistry.PLAY_REGISTRY.getOrCreateActionEvent(id);
	}

	public static Event<ClickActionReceived<ConfigContext>> configClickActionEvent(Identifier id) {
		Objects.requireNonNull(id, "ID cannot be null");
		return CustomClickActionsRegistry.CONFIG_REGISTRY.getOrCreateActionEvent(id);
	}

	@ApiStatus.NonExtendable
	public interface CommonContext {
		ServerCommonNetworkHandler handler();

		@Nullable
		NbtElement payload();
	}

	@ApiStatus.NonExtendable
	public interface PlayContext extends CommonContext {
		ServerPlayNetworkHandler handler();
	}

	@ApiStatus.NonExtendable
	public interface ConfigContext extends CommonContext {
		ServerConfigurationNetworkHandler handler();
	}

	@FunctionalInterface
	public interface ClickActionReceived<T extends CommonContext> {
		void handleCustomClickAction(T context);
	}

	private CustomClickActionEvents() {
	}
}
