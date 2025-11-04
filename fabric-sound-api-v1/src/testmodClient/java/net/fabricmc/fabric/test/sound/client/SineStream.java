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

package net.fabricmc.fabric.test.sound.client;

import java.nio.ByteBuffer;

import javax.sound.sampled.AudioFormat;

import org.lwjgl.BufferUtils;

import net.minecraft.client.sounds.AudioStream;

/**
 * An audio stream which plays a sine wave.
 */
class SineStream implements AudioStream {
	private static final AudioFormat FORMAT = new AudioFormat(44100, 8, 1, false, false);
	private static final double DT = 2 * Math.PI * 220 / 44100;

	private static double value = 0;

	@Override
	public AudioFormat getFormat() {
		return FORMAT;
	}

	@Override
	public ByteBuffer read(int capacity) {
		ByteBuffer buffer = BufferUtils.createByteBuffer(capacity);

		for (int i = 0; i < capacity; i++) {
			buffer.put(i, (byte) (Math.sin(value) * 127));
			value = (value + DT) % Math.PI;
		}

		return buffer;
	}

	@Override
	public void close() {
	}
}
