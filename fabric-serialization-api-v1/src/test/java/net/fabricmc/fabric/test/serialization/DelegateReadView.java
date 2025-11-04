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

import net.minecraft.core.HolderLookup;
import net.minecraft.world.level.storage.ValueInput;

/**
 * A delegating ReadView, used to force usage of fallback implementation of FabricReadView.
 */
public record DelegateReadView(ValueInput view) implements ValueInput {
	@Override
	public <T> Optional<T> read(String key, Codec<T> codec) {
		return view.read(key, codec);
	}

	@Override
	public <T> Optional<T> read(MapCodec<T> mapCodec) {
		return view.read(mapCodec);
	}

	@Override
	public Optional<ValueInput> child(String key) {
		return view.child(key).map(DelegateReadView::new);
	}

	@Override
	public ValueInput childOrEmpty(String key) {
		return new DelegateReadView(view.childOrEmpty(key));
	}

	@Override
	public Optional<ValueInputList> childrenList(String key) {
		return view.childrenList(key);
	}

	@Override
	public ValueInputList childrenListOrEmpty(String key) {
		return view.childrenListOrEmpty(key);
	}

	@Override
	public <T> Optional<TypedInputList<T>> list(String key, Codec<T> typeCodec) {
		return view.list(key, typeCodec);
	}

	@Override
	public <T> TypedInputList<T> listOrEmpty(String key, Codec<T> typeCodec) {
		return view.listOrEmpty(key, typeCodec);
	}

	@Override
	public boolean getBooleanOr(String key, boolean fallback) {
		return view.getBooleanOr(key, fallback);
	}

	@Override
	public byte getByteOr(String key, byte fallback) {
		return view.getByteOr(key, fallback);
	}

	@Override
	public int getShortOr(String key, short fallback) {
		return view.getShortOr(key, fallback);
	}

	@Override
	public Optional<Integer> getInt(String key) {
		return view.getInt(key);
	}

	@Override
	public int getIntOr(String key, int fallback) {
		return view.getIntOr(key, fallback);
	}

	@Override
	public long getLongOr(String key, long fallback) {
		return view.getLongOr(key, fallback);
	}

	@Override
	public Optional<Long> getLong(String key) {
		return view.getLong(key);
	}

	@Override
	public float getFloatOr(String key, float fallback) {
		return view.getFloatOr(key, fallback);
	}

	@Override
	public double getDoubleOr(String key, double fallback) {
		return view.getDoubleOr(key, fallback);
	}

	@Override
	public Optional<String> getString(String key) {
		return view.getString(key);
	}

	@Override
	public String getStringOr(String key, String fallback) {
		return view.getStringOr(key, fallback);
	}

	@Override
	public Optional<int[]> getIntArray(String key) {
		return view.getIntArray(key);
	}

	@Override
	public HolderLookup.Provider lookup() {
		return view.lookup();
	}
}
