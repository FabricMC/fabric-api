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

package net.fabricmc.fabric.test.serialization;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import org.jspecify.annotations.Nullable;

import net.minecraft.world.level.storage.ValueOutput;

/**
 * A delegating ValueOutput, used to force usage of fallback implementation of FabricValueOutput.
 */
public record DelegateValueOutput(ValueOutput output) implements ValueOutput {
	@Override
	public <T> void store(String key, Codec<T> codec, T value) {
		output.store(key, codec, value);
	}

	@Override
	public <T> void storeNullable(String key, Codec<T> codec, @Nullable T value) {
		output.storeNullable(key, codec, value);
	}

	@Override
	public <T> void store(MapCodec<T> codec, T value) {
		output.store(codec, value);
	}

	@Override
	public void putBoolean(String key, boolean value) {
		output.putBoolean(key, value);
	}

	@Override
	public void putByte(String key, byte value) {
		output.putByte(key, value);
	}

	@Override
	public void putShort(String key, short value) {
		output.putShort(key, value);
	}

	@Override
	public void putInt(String key, int value) {
		output.putInt(key, value);
	}

	@Override
	public void putLong(String key, long value) {
		output.putLong(key, value);
	}

	@Override
	public void putFloat(String key, float value) {
		output.putFloat(key, value);
	}

	@Override
	public void putDouble(String key, double value) {
		output.putDouble(key, value);
	}

	@Override
	public void putString(String key, String value) {
		output.putString(key, value);
	}

	@Override
	public void putIntArray(String key, int[] value) {
		output.putIntArray(key, value);
	}

	@Override
	public ValueOutput child(String key) {
		return new DelegateValueOutput(output.child(key));
	}

	@Override
	public ValueOutputList childrenList(String key) {
		return output.childrenList(key);
	}

	@Override
	public <T> TypedOutputList<T> list(String key, Codec<T> codec) {
		return output.list(key, codec);
	}

	@Override
	public void discard(String key) {
		output.discard(key);
	}

	@Override
	public boolean isEmpty() {
		return output.isEmpty();
	}
}
