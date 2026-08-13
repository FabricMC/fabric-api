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

package net.fabricmc.fabric.impl.resource.client;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.channels.Channels;
import java.nio.channels.WritableByteChannel;

import com.mojang.blaze3d.platform.NativeImage;
import org.lwjgl.stb.STBImage;

public final class NativeImageUtils {
	public static byte[] toBytes(NativeImage image) throws IOException {
		if (!image.format().supportedByStb()) {
			throw new UnsupportedOperationException("Don't know how to write format " + image.format());
		} else {
			image.checkAllocated();
		}

		try (
				var stream = new ByteArrayOutputStream();
				WritableByteChannel channel = Channels.newChannel(stream)
		) {
			if (!image.writeToChannel(channel)) {
				throw new IOException("Could not write image to byte array: " + STBImage.stbi_failure_reason());
			}

			return stream.toByteArray();
		}
	}
}
