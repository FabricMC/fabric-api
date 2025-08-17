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
import org.jetbrains.annotations.Nullable;

import net.minecraft.storage.WriteView;

/**
 * A delegating WriteView, used to force usage of fallback implementation of FabricWriteView.
 */
public record DelegateWriteView(WriteView view) implements WriteView {
	@Override
	public <T> void put(String key, Codec<T> codec, T value) {
		view.put(key, codec, value);
	}

	@Override
	public <T> void putNullable(String key, Codec<T> codec, @Nullable T value) {
		view.putNullable(key, codec, value);
	}

	@Override
	public <T> void put(MapCodec<T> codec, T value) {
		view.put(codec, value);
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
	public WriteView get(String key) {
		return new DelegateWriteView(view.get(key));
	}

	@Override
	public ListView getList(String key) {
		return view.getList(key);
	}

	@Override
	public <T> ListAppender<T> getListAppender(String key, Codec<T> codec) {
		return view.getListAppender(key, codec);
	}

	@Override
	public void remove(String key) {
		view.remove(key);
	}

	@Override
	public boolean isEmpty() {
		return view.isEmpty();
	}
}
