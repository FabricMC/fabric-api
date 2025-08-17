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

package net.fabricmc.fabric.impl.crash.build;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Stream;

import net.fabricmc.mappingio.tree.MappingTreeView;

/*
[byte] magic header
[int] version
[int] size of classes map
[int] size of methods map
: for each class
[int] intermediary class id
[int] relative offset to class name
: for each method
[int] intermediary method id
[int] relative offset to method name
-- start of name section, name offsets are relative to this
: for every name
[int] -- TODO could be a var int
	if top 2 bits set (0xC0) this is a pointer to another name, as a relative offset from the start of the name section
	if 0 end of string
	else read the number of bytes specified and then move onto the next name segment
 */
public class BinaryMappingsWriter {
	private static final int BUFFER_SIZE = 1024 * 1024 * 10; // 10 MiB should be enough
	private static final byte[] MAGIC_HEADER = new byte[]{'B', 'T', 'N', 'Y'};

	public static void write(Path out, MappingTreeView mappings) throws IOException {
		List<Name> names = getNames(mappings);
		NameSection nameSection = writeNameSection(names);

		// Intermediary id to the offset in the name section
		Map<Integer, Integer> classes = new TreeMap<>();
		Map<Integer, Integer> methods = new TreeMap<>();

		for (MappingTreeView.ClassMappingView classMapping : mappings.getClasses()) {
			int classId = getIntermediaryId(classMapping);

			if (classId > 0) {
				Name className = new Name(classMapping.getName(mappings.getNamespaceId("named")));
				int classOffset = nameSection.offsets.get(className);
				classes.put(classId, classOffset);
			}

			for (MappingTreeView.MethodMappingView methodMapping : classMapping.getMethods()) {
				int methodId = getIntermediaryId(methodMapping);

				if (methodId > 0) {
					Name methodName = new Name(methodMapping.getName(mappings.getNamespaceId("named")));
					int methodOffset = nameSection.offsets.get(methodName);
					methods.put(methodId, methodOffset);
				}
			}
		}

		ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE);
		buffer.put(MAGIC_HEADER);
		buffer.putInt(1); // Version 1
		buffer.putInt(classes.size());
		buffer.putInt(methods.size());
		writeOffsets(classes, buffer);
		writeOffsets(methods, buffer);
		buffer.put(nameSection.data);

		Files.write(out, getData(buffer));
	}

	private static int getIntermediaryId(MappingTreeView.ElementMappingView element) {
		String name = element.getSrcName();

		int index = name.lastIndexOf('_');

		if (index < 0) {
			// Not an intermediary name
			return -1;
		}

		String subStr = name.substring(index + 1);

		if (subStr.contains("$")) {
			// Unmapped anonymous inner class
			return -1;
		}

		return Integer.parseInt(subStr);
	}

	private static List<Name> getNames(MappingTreeView mappings) {
		List<Name> names = new ArrayList<>();

		int namedNs = mappings.getNamespaceId("named");

		for (MappingTreeView.ClassMappingView classMapping : mappings.getClasses()) {
			if (getIntermediaryId(classMapping) > 0) {
				names.add(new Name(classMapping.getName(namedNs)));
			}

			for (MappingTreeView.MethodMappingView methodMapping : classMapping.getMethods()) {
				if (getIntermediaryId(methodMapping) > 0) {
					names.add(new Name(methodMapping.getName(namedNs)));
				}
			}
		}

		return names;
	}

	private static void writeOffsets(Map<Integer, Integer> map, ByteBuffer buffer) {
		for (Map.Entry<Integer, Integer> entry : map.entrySet()) {
			buffer.putInt(entry.getKey());
			buffer.putInt(entry.getValue());
		}
	}

	private static NameSection writeNameSection(List<Name> names) {
		ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE);

		// The offsets to the start of each full name
		Map<Name, Integer> offsets = new HashMap<>();
		// The offsets to start of each part of a name
		Map<List<String>, Integer> partOffsets = new HashMap<>();

		for (Name name : names) {
			if (offsets.containsKey(name)) {
				// Duplicate name that has already been written
				continue;
			}

			List<String> parts = name.getReversedParts();

			// We know for sure that we need to write the first part, so this becomes to offset to this name
			int offset = buffer.position();
			writeParts(parts, buffer, partOffsets);

			offsets.put(name, offset);
		}

		return new NameSection(getData(buffer), Collections.unmodifiableMap(offsets));
	}

	private static void writeParts(List<String> parts, ByteBuffer buffer, Map<List<String>, Integer> partOffsets) {
		if (parts.isEmpty()) {
			// We have reached the fnd of the string, terminate with a null character
			buffer.putInt(0);
			return;
		}

		if (partOffsets.containsKey(parts)) {
			int offset = partOffsets.get(parts);
			buffer.putInt(offset | 0xC0000000); // Set the top two bits to indicate this is a pointer
			return;
		}

		String part = parts.getFirst();
		List<String> next = parts.subList(1, parts.size());

		// As we use the top two bits to indicate a pointer, we need to ensure that the part length does not exceed the new limit
		if (part.length() > 0x3FFFFFFF) {
			throw new IllegalArgumentException("Part too long: " + part);
		}

		// Write the size and data of the current part
		int offset = buffer.position();
		buffer.putInt(part.length());
		buffer.put(part.getBytes(StandardCharsets.UTF_8));

		// Store the offset for this part, so it can be referenced later
		partOffsets.put(parts, offset);

		// Keep writing until we reach the end of the name
		writeParts(next, buffer, partOffsets);
	}

	// Return the written data as a byte array
	private static byte[] getData(ByteBuffer buffer) {
		int length = buffer.position();
		byte[] data = new byte[length];
		buffer.rewind();
		buffer.get(data, 0, length);
		return data;
	}

	private record NameSection(byte[] data, Map<Name, Integer> offsets) { }

	/**
	 * A class that represents the full name of a given class.
	 * @param raw e.g "net/example/package/MyClass$Inner"
	 */
	private record Name(String raw) {
		/**
		 * Return a reversed list of the parts of the string to write.
		 * E.g given "net/example/package/MyClass$Inner" return "MyClass$Inner", "package", "example", "net"
		 */
		public List<String> getReversedParts() {
			String[] parts = raw.split("/");
			return Stream.of(parts)
					.map(String::intern)
					.toList()
					.reversed();
		}
	}
}
