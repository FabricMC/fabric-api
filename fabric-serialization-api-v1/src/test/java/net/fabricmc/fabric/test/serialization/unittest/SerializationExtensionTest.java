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

package net.fabricmc.fabric.test.serialization.unittest;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.storage.NbtReadView;
import net.minecraft.storage.NbtWriteView;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.util.ErrorReporter;

import net.fabricmc.fabric.test.serialization.DelegateReadView;
import net.fabricmc.fabric.test.serialization.DelegateWriteView;

public class SerializationExtensionTest {
	private static final String BYTES_KEY = "bytes";
	private static final byte[] BYTES_DATA = new byte[]{0, 1, 2, 3, 4, 5, 6, 7, 8, Byte.MAX_VALUE};
	private static final String LONG_KEY = "longs";
	private static final long[] LONG_DATA = new long[]{0, 1, 2, 3, 4, 5, 6, 7, 8, Long.MAX_VALUE};

	@Test
	void testFallbackWriteImplementation() {
		NbtWriteView directWrite = NbtWriteView.create(ErrorReporter.EMPTY);
		NbtWriteView defaultedWrite = NbtWriteView.create(ErrorReporter.EMPTY);

		writeExampleData(new DelegateWriteView(defaultedWrite));
		writeExampleData(directWrite);

		assertEquals(directWrite.getNbt(), defaultedWrite.getNbt(), () -> "Written NBT data is not equal!\n"
				+ "Direct write:\n"
				+ NbtHelper.toFormattedString(directWrite.getNbt(), true)
				+ "\nDefaulted write:\n"
				+ NbtHelper.toFormattedString(defaultedWrite.getNbt(), true));
	}

	@Test
	void testFallbackReadImplementation() {
		NbtWriteView directWrite = NbtWriteView.create(ErrorReporter.EMPTY);
		writeExampleData(directWrite);

		ReadView directRead = NbtReadView.create(ErrorReporter.EMPTY, RegistryWrapper.WrapperLookup.of(Stream.empty()), directWrite.getNbt());
		ReadView defaultedRead = new DelegateReadView(directRead);
		readExampleData(directWrite.getNbt(), directRead, "Direct read");
		readExampleData(directWrite.getNbt(), defaultedRead, "Defaulted read");
	}

	private void writeExampleData(WriteView view) {
		view.putByteArray(BYTES_KEY, BYTES_DATA);
		view.putLongArray(LONG_KEY, LONG_DATA);

		for (int i = 0; i < 8; i++) {
			view.putInt("key_" + i, i);
		}
	}

	private void readExampleData(NbtCompound compound, ReadView view, String type) {
		assertArrayEquals(view.getOptionalByteArray(BYTES_KEY).orElse(new byte[0]), BYTES_DATA, () -> "Read NBT data doesn't match key " + BYTES_KEY + " for type " + type + "!");
		assertArrayEquals(view.getOptionalLongArray(LONG_KEY).orElse(new long[0]), LONG_DATA, () -> "Read NBT data doesn't match key " + LONG_KEY + " for type " + type + "!");

		assertFalse(view.contains("non_existing"), () -> "Read NBT data wrongly returns contains check for non existing entry for type " + type + "!");
		assertTrue(view.contains("key_3"), () -> "Read NBT data wrongly returns contains check for existing entry for type " + type + "!");

		assertEquals(Set.copyOf(view.keys()), compound.getKeys(), () -> "Read NBT data returns wrong keys for type " + type + "!");
	}
}
