package net.fabricmc.fabric.api.networking.v1;

import java.util.Objects;

import net.minecraft.server.network.ServerCommonNetworkHandler;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import net.minecraft.nbt.NbtElement;
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
