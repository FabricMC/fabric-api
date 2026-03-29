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

package net.fabricmc.fabric.test.networking.unit;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import net.minecraft.network.Connection;
import net.minecraft.resources.Identifier;

import net.fabricmc.fabric.api.networking.v1.context.PacketContext;
import net.fabricmc.fabric.impl.networking.context.PacketContextImpl;

public class PacketContextTests {
	private static final PacketContext.Key<Object> OBJECT_KEY = PacketContext.key(Identifier.fromNamespaceAndPath("test", "object"));
	private Connection connection;
	private PacketContext context;

	@BeforeEach
	void setUp() {
		connection = mock(Connection.class);
		context = new PacketContextImpl(connection);
		when(connection.getPacketContext()).thenReturn(context);
	}

	// Test storing and retrieving values.
	@Test
	void storeAndRetrieve() {
		var object = new Object();

		assertNull(context.get(OBJECT_KEY));

		context.set(OBJECT_KEY, object);
		assertEquals(context.get(OBJECT_KEY), object);

		context.set(OBJECT_KEY, null);
		assertNull(context.get(OBJECT_KEY));

		assertEquals(context.get(PacketContext.CONNECTION), connection);
	}

	// Test orElse fallback methods.
	@Test
	void orElse() {
		var a = new Object();
		var b = new Object();

		assertEquals(context.orElse(OBJECT_KEY, b), b);

		context.set(OBJECT_KEY, a);
		assertEquals(context.orElse(OBJECT_KEY, b), a);

		assertThrows(RuntimeException.class, () -> context.orElseThrow(PacketContext.GAME_PROFILE));
		assertDoesNotThrow(() -> context.orElseThrow(OBJECT_KEY));
	}

	// Test context retrieving and context running methods.
	@Test
	void runWithContext() {
		assertNull(assertDoesNotThrow(() -> PacketContext.get()));
		assertThrows(RuntimeException.class, () -> PacketContext.orElseThrow());

		PacketContext.runWithContext(connection, () -> {
			assertEquals(PacketContext.get(), context);
			assertDoesNotThrow(() -> PacketContext.orElseThrow());

			PacketContext.runWithoutContext(() -> {
				assertNull(assertDoesNotThrow(() -> PacketContext.get()));
				assertThrows(RuntimeException.class, () -> PacketContext.orElseThrow());
			});

			assertEquals(PacketContext.get(), context);
			assertDoesNotThrow(() -> PacketContext.orElseThrow());
		});

		assertNull(assertDoesNotThrow(() -> PacketContext.get()));
		assertThrows(RuntimeException.class, () -> PacketContext.orElseThrow());

		PacketContext.supplyWithContext(connection, () -> {
			assertEquals(PacketContext.get(), context);
			assertDoesNotThrow(() -> PacketContext.orElseThrow());
			return null;
		});

		assertNull(assertDoesNotThrow(() -> PacketContext.get()));
		assertThrows(RuntimeException.class, () -> PacketContext.orElseThrow());
	}
}
