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

import java.util.Optional;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;

import net.minecraft.registry.RegistryWrapper;
import net.minecraft.storage.ReadView;

/**
 * A delegating ReadView, used to force usage of fallback implementation of FabricReadView.
 */
public record DelegateReadView(ReadView view) implements ReadView {
	@Override
	public <T> Optional<T> read(String key, Codec<T> codec) {
		return view.read(key, codec);
	}

	@Override
	public <T> Optional<T> read(MapCodec<T> mapCodec) {
		return view.read(mapCodec);
	}

	@Override
	public Optional<ReadView> getOptionalReadView(String key) {
		return view.getOptionalReadView(key).map(DelegateReadView::new);
	}

	@Override
	public ReadView getReadView(String key) {
		return new DelegateReadView(view.getReadView(key));
	}

	@Override
	public Optional<ListReadView> getOptionalListReadView(String key) {
		return view.getOptionalListReadView(key);
	}

	@Override
	public ListReadView getListReadView(String key) {
		return view.getListReadView(key);
	}

	@Override
	public <T> Optional<TypedListReadView<T>> getOptionalTypedListView(String key, Codec<T> typeCodec) {
		return view.getOptionalTypedListView(key, typeCodec);
	}

	@Override
	public <T> TypedListReadView<T> getTypedListView(String key, Codec<T> typeCodec) {
		return view.getTypedListView(key, typeCodec);
	}

	@Override
	public boolean getBoolean(String key, boolean fallback) {
		return view.getBoolean(key, fallback);
	}

	@Override
	public byte getByte(String key, byte fallback) {
		return view.getByte(key, fallback);
	}

	@Override
	public int getShort(String key, short fallback) {
		return view.getShort(key, fallback);
	}

	@Override
	public Optional<Integer> getOptionalInt(String key) {
		return view.getOptionalInt(key);
	}

	@Override
	public int getInt(String key, int fallback) {
		return view.getInt(key, fallback);
	}

	@Override
	public long getLong(String key, long fallback) {
		return view.getLong(key, fallback);
	}

	@Override
	public Optional<Long> getOptionalLong(String key) {
		return view.getOptionalLong(key);
	}

	@Override
	public float getFloat(String key, float fallback) {
		return view.getFloat(key, fallback);
	}

	@Override
	public double getDouble(String key, double fallback) {
		return view.getDouble(key, fallback);
	}

	@Override
	public Optional<String> getOptionalString(String key) {
		return view.getOptionalString(key);
	}

	@Override
	public String getString(String key, String fallback) {
		return view.getString(key, fallback);
	}

	@Override
	public Optional<int[]> getOptionalIntArray(String key) {
		return view.getOptionalIntArray(key);
	}

	@Override
	public RegistryWrapper.WrapperLookup getRegistries() {
		return view.getRegistries();
	}
}
