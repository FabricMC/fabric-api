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

package net.fabricmc.fabric.test.registry.sync;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import net.minecraft.Bootstrap;
import net.minecraft.SharedConstants;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.SimpleRegistry;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;

import net.fabricmc.fabric.api.event.registry.FabricRegistryBuilder;
import net.fabricmc.fabric.api.event.registry.RegistryEntryAddedCallback;

public class RegistryEntryAddedCallbackTest {
	@Mock
	private Consumer<RegistryEntry.Reference<String>> mockConsumer;

	@Captor
	private ArgumentCaptor<RegistryEntry.Reference<String>> captor;

	@BeforeAll
	static void beforeAll() {
		SharedConstants.createGameVersion();
		Bootstrap.initialize();
	}

	@BeforeEach
	void beforeEach() {
		MockitoAnnotations.openMocks(this);
	}

	@Test
	void testEntryAddedCallback() {
		RegistryKey<Registry<String>> testRegistryKey = RegistryKey.ofRegistry(id(UUID.randomUUID().toString()));
		SimpleRegistry<String> testRegistry = FabricRegistryBuilder.createSimple(testRegistryKey)
				.buildAndRegister();

		Registry.register(testRegistry, id("before"), "before");
		RegistryEntryAddedCallback.allEntries(testRegistry, mockConsumer);

		// Test that the callback can register new entries.
		RegistryEntryAddedCallback.allEntries(testRegistry, s -> {
			if (s.value().equals("before")) {
				Registry.register(testRegistry, id("during"), "during");
			}
		});

		Registry.register(testRegistry, id("after"), "after");

		verify(mockConsumer, times(3)).accept(captor.capture());

		List<String> values = captor.getAllValues()
				.stream()
				.map(RegistryEntry.Reference::value)
				.toList();

		assertEquals(3, values.size());
		assertEquals("before", values.getFirst());
		assertEquals("during", values.get(1));
		assertEquals("after", values.get(2));
	}

	private static Identifier id(String path) {
		return Identifier.of("registry_sync_test_entry_added_test", path);
	}
}
