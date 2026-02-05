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

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import net.fabricmc.fabric.test.serialization.DelegateValueInput;
import net.fabricmc.fabric.test.serialization.DelegateValueOutput;

public class SerializationExtensionTest {
	private static final String BYTES_KEY = "bytes";
	private static final byte[] BYTES_DATA = new byte[]{0, 1, 2, 3, 4, 5, 6, 7, 8, Byte.MAX_VALUE};
	private static final String LONG_KEY = "longs";
	private static final long[] LONG_DATA = new long[]{0, 1, 2, 3, 4, 5, 6, 7, 8, Long.MAX_VALUE};

	@Test
	void testFallbackWriteImplementation() {
		TagValueOutput directWrite = TagValueOutput.createWithoutContext(ProblemReporter.DISCARDING);
		TagValueOutput defaultedWrite = TagValueOutput.createWithoutContext(ProblemReporter.DISCARDING);

		writeExampleData(new DelegateValueOutput(defaultedWrite));
		writeExampleData(directWrite);

		assertEquals(directWrite.buildResult(), defaultedWrite.buildResult(), () -> "Written NBT data is not equal!\n"
				+ "Direct write:\n"
				+ NbtUtils.prettyPrint(directWrite.buildResult(), true)
				+ "\nDefaulted write:\n"
				+ NbtUtils.prettyPrint(defaultedWrite.buildResult(), true));
	}

	@Test
	void testFallbackReadImplementation() {
		TagValueOutput directWrite = TagValueOutput.createWithoutContext(ProblemReporter.DISCARDING);
		writeExampleData(directWrite);

		ValueInput directRead = TagValueInput.create(ProblemReporter.DISCARDING, HolderLookup.Provider.create(Stream.empty()), directWrite.buildResult());
		ValueInput defaultedRead = new DelegateValueInput(directRead);
		readExampleData(directWrite.buildResult(), directRead, "Direct read");
		readExampleData(directWrite.buildResult(), defaultedRead, "Defaulted read");
	}

	private void writeExampleData(ValueOutput output) {
		output.putByteArray(BYTES_KEY, BYTES_DATA);
		output.putLongArray(LONG_KEY, LONG_DATA);

		for (int i = 0; i < 8; i++) {
			output.putInt("key_" + i, i);
		}
	}

	private void readExampleData(CompoundTag compoundTag, ValueInput input, String type) {
		assertArrayEquals(input.getOptionalByteArray(BYTES_KEY).orElse(new byte[0]), BYTES_DATA, () -> "Read NBT data doesn't match key " + BYTES_KEY + " for type " + type + "!");
		assertArrayEquals(input.getOptionalLongArray(LONG_KEY).orElse(new long[0]), LONG_DATA, () -> "Read NBT data doesn't match key " + LONG_KEY + " for type " + type + "!");

		assertFalse(input.contains("non_existing"), () -> "Read NBT data wrongly returns contains check for non existing entry for type " + type + "!");
		assertTrue(input.contains("key_3"), () -> "Read NBT data wrongly returns contains check for existing entry for type " + type + "!");

		assertEquals(Set.copyOf(input.keySet()), compoundTag.keySet(), () -> "Read NBT data returns wrong keys for type " + type + "!");
	}
}
