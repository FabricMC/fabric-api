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
 * A delegating ValueInput, used to force usage of fallback implementation of FabricValueInput.
 */
public record DelegateValueInput(ValueInput input) implements ValueInput {
	@Override
	public <T> Optional<T> read(String key, Codec<T> codec) {
		return input.read(key, codec);
	}

	@Override
	public <T> Optional<T> read(MapCodec<T> mapCodec) {
		return input.read(mapCodec);
	}

	@Override
	public Optional<ValueInput> child(String key) {
		return input.child(key).map(DelegateValueInput::new);
	}

	@Override
	public ValueInput childOrEmpty(String key) {
		return new DelegateValueInput(input.childOrEmpty(key));
	}

	@Override
	public Optional<ValueInputList> childrenList(String key) {
		return input.childrenList(key);
	}

	@Override
	public ValueInputList childrenListOrEmpty(String key) {
		return input.childrenListOrEmpty(key);
	}

	@Override
	public <T> Optional<TypedInputList<T>> list(String key, Codec<T> typeCodec) {
		return input.list(key, typeCodec);
	}

	@Override
	public <T> TypedInputList<T> listOrEmpty(String key, Codec<T> typeCodec) {
		return input.listOrEmpty(key, typeCodec);
	}

	@Override
	public boolean getBooleanOr(String key, boolean fallback) {
		return input.getBooleanOr(key, fallback);
	}

	@Override
	public byte getByteOr(String key, byte fallback) {
		return input.getByteOr(key, fallback);
	}

	@Override
	public int getShortOr(String key, short fallback) {
		return input.getShortOr(key, fallback);
	}

	@Override
	public Optional<Integer> getInt(String key) {
		return input.getInt(key);
	}

	@Override
	public int getIntOr(String key, int fallback) {
		return input.getIntOr(key, fallback);
	}

	@Override
	public long getLongOr(String key, long fallback) {
		return input.getLongOr(key, fallback);
	}

	@Override
	public Optional<Long> getLong(String key) {
		return input.getLong(key);
	}

	@Override
	public float getFloatOr(String key, float fallback) {
		return input.getFloatOr(key, fallback);
	}

	@Override
	public double getDoubleOr(String key, double fallback) {
		return input.getDoubleOr(key, fallback);
	}

	@Override
	public Optional<String> getString(String key) {
		return input.getString(key);
	}

	@Override
	public String getStringOr(String key, String fallback) {
		return input.getStringOr(key, fallback);
	}

	@Override
	public Optional<int[]> getIntArray(String key) {
		return input.getIntArray(key);
	}

	@Override
	public HolderLookup.Provider lookup() {
		return input.lookup();
	}
}
