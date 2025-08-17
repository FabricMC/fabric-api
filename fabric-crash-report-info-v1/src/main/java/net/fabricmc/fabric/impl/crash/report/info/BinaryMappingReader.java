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

package net.fabricmc.fabric.impl.crash.report.info;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.jetbrains.annotations.Nullable;

public class BinaryMappingReader {
	private static final byte[] MAGIC_HEADER = new byte[]{'B', 'T', 'N', 'Y'};
	private static final int NAME_ENTRY_SIZE = 8; // 4 bytes for id, 4 bytes for offset

	private final ByteBuffer buf;
	private final int classNameMapCount;
	private final int classNameMapOffset;
	private final int methodNameMapCount;
	private final int methodNameMapOffset;
	private final int namesOffset;

	public BinaryMappingReader() throws IOException {
		this(open());
	}

	public BinaryMappingReader(ByteBuffer buffer) throws IOException {
		this.buf = buffer;

		byte[] header = new byte[4];
		this.buf.get(header);

		if (!Arrays.equals(header, MAGIC_HEADER)) {
			throw new IOException("Invalid header in mappings.bin");
		}

		int version = this.buf.getInt();

		if (version != 1) {
			throw new IOException("Unsupported version in mappings.bin: " + version);
		}

		this.classNameMapCount = this.buf.getInt();
		this.methodNameMapCount = this.buf.getInt();
		// End of header

		int offset = this.buf.position();
		this.classNameMapOffset = offset;
		offset += this.classNameMapCount * NAME_ENTRY_SIZE;
		this.methodNameMapOffset = offset;
		offset += this.methodNameMapCount * NAME_ENTRY_SIZE;
		this.namesOffset = offset;
	}

	@Nullable
	public String getClassName(int id) {
		int offset = getOffset(id, classNameMapOffset, classNameMapCount);

		if (offset < 0) {
			return null;
		}

		return getName(offset);
	}

	@Nullable
	public String getMethodName(int id) {
		int offset = getOffset(id, methodNameMapOffset, methodNameMapCount);

		if (offset < 0) {
			return null;
		}

		return getName(offset);
	}

	// Returns the offset for the given id from the class or method name map, or 0 if not found.
	private int getOffset(int id, int baseOffset, int count) {
		int low = 0;
		int high = count - 1;

		while (low <= high) {
			int mid = (low + high) >>> 1;
			int offset = baseOffset + mid * NAME_ENTRY_SIZE;
			buf.position(offset);

			int key = buf.getInt();

			if (key == id) {
				return buf.getInt();
			} else if (key < id) {
				low = mid + 1; // Search in the upper half
			} else {
				high = mid - 1; // Search in the lower half
			}
		}

		return -1;
	}

	private String getName(int offset) {
		List<String> parts = new ArrayList<>();

		buf.position(namesOffset + offset);
		getNextName(parts);

		if (parts.isEmpty()) {
			throw new IllegalStateException("No name parts found for offset: " + offset);
		}

		Collections.reverse(parts);
		return String.join("/", parts);
	}

	private void getNextName(List<String> parts) {
		int length = buf.getInt();

		// String terminator
		if (length == 0) {
			return;
		}

		// Pointer to another name
		if ((length & 0xC0000000) == 0xC0000000) {
			buf.position(namesOffset + (length & 0x3FFFFFFF));
		} else {
			byte[] nameBytes = new byte[length];
			buf.get(nameBytes);
			parts.add(new String(nameBytes, StandardCharsets.UTF_8));
		}

		getNextName(parts);
	}

	private static ByteBuffer open() throws IOException {
		try (InputStream is = BinaryMappingReader.class.getResourceAsStream("/data/fabric-crash-report-info-v1/mappings.bin")) {
			if (is == null) {
				throw new IOException("Could not find mappings.bin resource");
			}

			return ByteBuffer.wrap(is.readAllBytes());
		}
	}
}
