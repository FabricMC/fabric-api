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

package net.fabricmc.fabric.api.networking.v1.context;

import java.util.Objects;
import java.util.function.Supplier;

import com.mojang.authlib.GameProfile;
import com.mojang.serialization.Codec;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import net.minecraft.core.RegistryAccess;
import net.minecraft.network.Connection;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerLoginPacketListenerImpl;

import net.fabricmc.fabric.impl.networking.context.PacketContextImpl;

/**
 * This class allow to easily pass context between multiple packet listeners and packet serialization.
 * All connections get their own unique context object.
 *
 * <p>When using outside of packet serialization, you can retrieve instance of PacketContext
 * by calling the {@link PacketContextProvider#getPacketContext()} method on vanilla packet listeners.
 *
 * <p>When inside of packet serialization, whatever it's within {@link StreamCodec} or networking-used {@link Codec}
 * you can retrievie the instance with {@link PacketContext#get()} or {@link PacketContext#orElseThrow()}.
 *
 * <p>Example usage:
 * <pre>{@code
 * PacketContext.Key<TriState> TATER_MESSAGE = PacketContext.key(Identifier.fromNamespaceAndPath("mod", "tater_message"));
 *
 * ServerConfigurationNetworking.registerGlobalReceiver(ServerboundModConfig.TYPE, (packet, context) -> {
 *      context.packetContext().set(TATER_MESSAGE, packet.taterMessage());
 * });
 *
 * ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
 *      if (handler.getPacketContext().orElse(TATER_MESSAGE, ModConfig.taterMessage)) {
 *            handler.getPlayer().sendSystemMessage(Component.literal("I am a Tiny Potato and I believe in you!"), false);
 *      }
 * });
 * }</pre>
 */
@ApiStatus.NonExtendable
public interface PacketContext {
	/**
	 * The server instance that handles this connection. Only present on clientbound connections.
	 * This value is set once the {@link ServerLoginPacketListenerImpl} is constructed.
	 */
	ReadKey<MinecraftServer> SERVER_INSTANCE = PacketContextImpl.SERVER_INSTANCE;
	/**
	 * The instance of registry access.
	 * This value is set once the {@link ServerLoginPacketListenerImpl} is constructed (on serve)
	 * and once client side configuration is finished.
	 */
	ReadKey<RegistryAccess> REGISTRY_ACCESS = PacketContextImpl.REGISTRY_ACCESS;
	/**
	 * The Game Profile attached to this connection.
	 * This value is set on both server and client, once the login process succeeds.
	 */
	ReadKey<GameProfile> GAME_PROFILE = PacketContextImpl.GAME_PROFILE;
	/**
	 * The connection that owns this packet context.
	 * This value is always present.
	 */
	ReadKey<@NonNull Connection> CONNECTION = PacketContextImpl.CONNECTION;

	/**
	 * Returns currently stored value.
	 *
	 * @param key unique key under which value is stored
	 * @return stored value or null if not set.
	 */
	@Nullable
	<T> T get(ReadKey<T> key);

	/**
	 * Returns currently stored value.
	 * In case of it not being stored earlier, this method will throw.
	 *
	 * @param key unique key under which value is stored
	 * @return stored value
	 * @throws NullPointerException if not set
	 */
	default <T> T orElseThrow(ReadKey<T> key) {
		return Objects.requireNonNull(get(key), () -> "Packet Context is missing the '" + ((PacketContextImpl.KeyImpl<T>) key).key() + "' value!");
	}

	/**
	 * Returns currently stored value.
	 * In case of it not being stored earlier, this method will return provided default value.
	 *
	 * @param key unique key under which value is stored
	 * @param defaultValue value to return if no value is set
	 * @return stored value if present, defaultValue otherwise
	 */
	default <T> T orElse(ReadKey<T> key, T defaultValue) {
		return Objects.requireNonNullElse(get(key), defaultValue);
	}

	/**
	 * Stores the value.
	 *
	 * @param key unique key under which value is stored
	 * @param value value to store, if null it will remove it instead
	 */
	<T> void set(Key<T> key, @Nullable T value);

	/**
	 * Returns currently set packet context.
	 *
	 * @return current context or null
	 */
	@Nullable
	static PacketContext get() {
		if (PacketContextImpl.VALUE.isBound()) {
			return PacketContextImpl.VALUE.get();
		}

		return null;
	}

	/**
	 * Returns currently set packet context.
	 * In case of context missing, this method will throw.
	 *
	 * @return current context or null
	 */
	static PacketContext orElseThrow() {
		PacketContext ctx = PacketContextImpl.VALUE.orElseThrow(() -> new RuntimeException("PacketContext is required, but it wasn't set up!"));

		if (ctx == null) {
			throw new RuntimeException("PacketContext is required, but it was disabled!");
		}

		return ctx;
	}

	/**
	 * Runs specified runnable under a packet context.
	 *
	 * @param provider provider of the context
	 * @param runnable runnable to execute
	 */
	static void runWithContext(PacketContextProvider provider, Runnable runnable) {
		ScopedValue.where(PacketContextImpl.VALUE, provider.getPacketContext()).run(runnable);
	}

	/**
	 * Runs specified runnable under a packet context, returning a value.
	 *
	 * @param provider provider of the context
	 * @param supplier supplier to execute
	 * @return result of supplier
	 */
	static <T> T supplyWithContext(PacketContextProvider provider, Supplier<T> supplier) {
		return ScopedValue.where(PacketContextImpl.VALUE, provider.getPacketContext()).call(supplier::get);
	}

	/**
	 * Runs specified runnable without a packet context.
	 *
	 * @param runnable runnable to execute
	 */
	static void runWithoutContext(Runnable runnable) {
		if (PacketContextImpl.VALUE.isBound()) {
			ScopedValue.where(PacketContextImpl.VALUE, null).run(runnable);
			return;
		}

		runnable.run();
	}

	/**
	 * Runs specified runnable without a packet context, returning a value.
	 *
	 * @param supplier supplier to execute
	 * @return result of supplier
	 */
	static <T> T supplyWithoutContext(Supplier<T> supplier) {
		if (PacketContextImpl.VALUE.isBound()) {
			return ScopedValue.where(PacketContextImpl.VALUE, null).call(supplier::get);
		}

		return supplier.get();
	}

	/**
	 * Creates a new key to be used with the packet context.
	 *
	 * @param key identifier for this key
	 * @return a unique key
	 */
	static <T> Key<T> key(Identifier key) {
		return new PacketContextImpl.KeyImpl<>(key);
	}

	@ApiStatus.NonExtendable
	interface ReadKey<T> { }

	@ApiStatus.NonExtendable
	interface Key<T> extends ReadKey<T> { }
}
