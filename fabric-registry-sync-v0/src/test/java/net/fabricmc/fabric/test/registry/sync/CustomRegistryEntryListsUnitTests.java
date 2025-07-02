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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.Stream;

import com.google.gson.JsonElement;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.invocation.InvocationOnMock;

import net.minecraft.Bootstrap;
import net.minecraft.SharedConstants;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryOps;
import net.minecraft.registry.entry.RegistryEntryList;
import net.minecraft.registry.entry.RegistryEntryListCodec;

import net.fabricmc.fabric.impl.registry.sync.entrylists.CustomRegistryEntryListSerializerImpl;
import net.fabricmc.fabric.impl.registry.sync.entrylists.defaults.DefaultCustomRegistryEntryListsImpl;
import net.fabricmc.fabric.impl.registry.sync.entrylists.defaults.InverseRegistryEntryList;

public class CustomRegistryEntryListsUnitTests {
	@BeforeAll
	static void beforeAll() {
		SharedConstants.createGameVersion();
		Bootstrap.initialize();
		DefaultCustomRegistryEntryListsImpl.register();
	}

	@Test
	void testSerialization() {
		DynamicRegistryManager drm = mockDRM();
		Registry<Block> reg = drm.getOrThrow(RegistryKeys.BLOCK);
		RegistryOps<JsonElement> ops = drm.getOps(JsonOps.INSTANCE);
		RegistryEntryList<Block> entryList = RegistryEntryList.of(reg.getEntry(Blocks.ACACIA_LEAVES), reg.getEntry(Blocks.BRAIN_CORAL_BLOCK));
		InverseRegistryEntryList<Block> customEntryList = new InverseRegistryEntryList<>(reg, entryList);
		Codec<InverseRegistryEntryList<Block>> codec = swapType(RegistryEntryListCodec.create(reg.getKey(), reg.getEntryCodec(), false));
		JsonElement serialized = Assertions.assertDoesNotThrow(() -> codec.encodeStart(ops, customEntryList).getOrThrow());
		Assertions.assertTrue(CustomRegistryEntryListSerializerImpl.isSerializedCustomRegistryEntryList(ops, serialized));
		RegistryEntryList<Block> deserialized = Assertions.assertDoesNotThrow(() -> codec.parse(ops, serialized).getOrThrow());
		Assertions.assertInstanceOf(InverseRegistryEntryList.class, deserialized);
	}

	@Test
	@SuppressWarnings("unchecked")
	void testInvalidation() {
		RegistryEntryList<Block> parent = mockParentList();
		RegistryEntryList<Block> child1 = spy(RegistryEntryList.class);
		RegistryEntryList<Block> child2 = spy(RegistryEntryList.class);
		parent.registerDependency(child1);
		parent.registerDependency(child2);
		parent.invalidate();
		verify(child1, times(1)).invalidate();
		verify(child2, times(1)).invalidate();
	}

	@SuppressWarnings("unchecked")
	private static DynamicRegistryManager mockDRM() {
		DynamicRegistryManager drm = mock(DynamicRegistryManager.class);
		when(drm.getOps(any())).thenReturn((RegistryOps<Object>) (Object) RegistryOps.of(JsonOps.INSTANCE, drm));
		when(drm.getOptional(any())).thenAnswer(invocation -> {
			RegistryKey<?> key = invocation.getArgument(0);
			return Optional.ofNullable(Registries.REGISTRIES.get(key.getValue()));
		});
		doAnswer(invocation -> drm.getOptional(invocation.getArgument(0)).orElseThrow(
				() -> new NoSuchElementException("No registry found for key: " + invocation.getArgument(0))
		)).when(drm).getOrThrow(any());
		return drm;
	}

	@SuppressWarnings("unchecked")
	private static <T> RegistryEntryList<T> mockParentList() {
		RegistryEntryList<T> parentList = mock(RegistryEntryList.class);
		when(parentList.stream()).thenReturn(Stream.empty());
		doAnswer(InvocationOnMock::callRealMethod)
				.when(parentList)
				.invalidate();

		doAnswer(InvocationOnMock::callRealMethod)
				.when(parentList)
				.registerDependency(any());
		return parentList;
	}

	@SuppressWarnings("unchecked")
	private static <T1, T2> Codec<T1> swapType(Codec<T2> codec) {
		return (Codec<T1>) codec;
	}
}
