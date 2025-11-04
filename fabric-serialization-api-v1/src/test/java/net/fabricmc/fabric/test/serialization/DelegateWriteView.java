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
 * A delegating WriteView, used to force usage of fallback implementation of FabricWriteView.
 */
public record DelegateWriteView(ValueOutput view) implements ValueOutput {
	@Override
	public <T> void store(String key, Codec<T> codec, T value) {
		view.store(key, codec, value);
	}

	@Override
	public <T> void storeNullable(String key, Codec<T> codec, @Nullable T value) {
		view.storeNullable(key, codec, value);
	}

	@Override
	public <T> void store(MapCodec<T> codec, T value) {
		view.store(codec, value);
	}

	@Override
	public void putBoolean(String key, boolean value) {
		view.putBoolean(key, value);
	}

	@Override
	public void putByte(String key, byte value) {
		view.putByte(key, value);
	}

	@Override
	public void putShort(String key, short value) {
		view.putShort(key, value);
	}

	@Override
	public void putInt(String key, int value) {
		view.putInt(key, value);
	}

	@Override
	public void putLong(String key, long value) {
		view.putLong(key, value);
	}

	@Override
	public void putFloat(String key, float value) {
		view.putFloat(key, value);
	}

	@Override
	public void putDouble(String key, double value) {
		view.putDouble(key, value);
	}

	@Override
	public void putString(String key, String value) {
		view.putString(key, value);
	}

	@Override
	public void putIntArray(String key, int[] value) {
		view.putIntArray(key, value);
	}

	@Override
	public ValueOutput child(String key) {
		return new DelegateWriteView(view.child(key));
	}

	@Override
	public ValueOutputList childrenList(String key) {
		return view.childrenList(key);
	}

	@Override
	public <T> TypedOutputList<T> list(String key, Codec<T> codec) {
		return view.list(key, codec);
	}

	@Override
	public void discard(String key) {
		view.discard(key);
	}

	@Override
	public boolean isEmpty() {
		return view.isEmpty();
	}
}
