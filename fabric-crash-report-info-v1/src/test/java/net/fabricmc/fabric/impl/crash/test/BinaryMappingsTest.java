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

package net.fabricmc.fabric.impl.crash.test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.io.StringReader;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import net.fabricmc.fabric.impl.crash.build.BinaryMappingsWriter;
import net.fabricmc.fabric.impl.crash.build.MappingsPreparation;
import net.fabricmc.fabric.impl.crash.report.info.BinaryMappingReader;
import net.fabricmc.mappingio.format.tiny.Tiny2FileReader;
import net.fabricmc.mappingio.tree.MappingTreeView;
import net.fabricmc.mappingio.tree.MemoryMappingTree;

public class BinaryMappingsTest {
	@TempDir
	Path tempDir;

	@Test
	void testReadWriteClassName() throws IOException {
		Path mappingsFile = tempDir.resolve("mappings.bin");

		MappingTreeView mappings = getMapping(SIMPLE_MAPPINGS);
		BinaryMappingsWriter.write(mappingsFile, mappings);

		BinaryMappingReader reader = new BinaryMappingReader(read(mappingsFile));

		assertEquals("net/minecraft/Example", reader.getClassName(12));
		assertEquals("example", reader.getMethodName(5432));
		assertEquals("net/minecraft/util/Test", reader.getClassName(34));
		assertEquals("test", reader.getMethodName(1));
		assertEquals("net/minecraft/util/Test$Inner", reader.getClassName(56));
		assertEquals("inner", reader.getMethodName(2));
		assertEquals("net/minecraft/Test", reader.getClassName(78));
	}

	@Test
	void testReadWriteFullMappings() throws IOException {
		Path mappingsFile = tempDir.resolve("mappings.bin");
		MappingsPreparation.prepare(mappingsFile);

		BinaryMappingReader reader = new BinaryMappingReader(read(mappingsFile));

		assertEquals("net/minecraft/block/Blocks", reader.getClassName(2246));
		assertEquals("getBlockState", reader.getMethodName(48884));
		assertEquals("net/minecraft/world/BlockLocating$Rectangle", reader.getClassName(5460));
	}

	private static ByteBuffer read(Path path) throws IOException {
		return ByteBuffer.wrap(Files.readAllBytes(path));
	}

	private static MappingTreeView getMapping(String tiny) throws IOException {
		MemoryMappingTree mappings = new MemoryMappingTree();
		Tiny2FileReader.read(new StringReader(tiny), mappings);
		return mappings;
	}

	private static final String SIMPLE_MAPPINGS = """
			tiny	2	0	intermediary	named
			c	net/minecraft/class_12	net/minecraft/Example
			\tm	()V;	method_5432	example
			c	net/minecraft/class_34	net/minecraft/util/Test
			\tm	()V;	method_1	test
			c	net/minecraft/class_56	net/minecraft/util/Test$Inner
			\tm	()V;	method_2	inner
			c	net/minecraft/class_78	net/minecraft/Test
			""".stripIndent();
}
