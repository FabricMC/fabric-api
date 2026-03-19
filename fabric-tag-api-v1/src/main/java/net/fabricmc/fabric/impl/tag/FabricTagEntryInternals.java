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

package net.fabricmc.fabric.impl.tag;

import java.util.function.Supplier;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Decoder;
import com.mojang.serialization.DynamicOps;

import org.jetbrains.annotations.Nullable;

import net.minecraft.tags.TagEntry;
import net.minecraft.util.Unit;

import net.fabricmc.fabric.impl.tag.util.WrapperCodec;

public final class FabricTagEntryInternals {
	public static final Codec<TagEntry> REMOVED_ENTRY_CODEC = new WrapperCodec<>(
			TagEntry.CODEC,
			new WrapperCodec.Wrapper<>() {
				@Override
				public <T> DataResult<Pair<TagEntry, T>> decode(DynamicOps<T> ops, T input, Decoder<TagEntry> wrapped) {
					return FabricTagEntryInternals.withRemovedValue(true, () -> wrapped.decode(ops, input));
				}
			}
	);

	/**
	 * A Fake Argument to the {@link TagEntry} constructor representing whether the entry will have it's {@code removed} flag set.
	 */
	private static final ThreadLocal<Unit> REMOVED = new ThreadLocal<>();

	private FabricTagEntryInternals() {
		throw new UnsupportedOperationException();
	}

	public static <T> T withRemovedValue(boolean value, Supplier<T> action) {
		@Nullable
		Unit initialValue = REMOVED.get();

		try {
			if (value) {
				REMOVED.set(Unit.INSTANCE);
			} else {
				REMOVED.remove();
			}

			return action.get();
		} finally {
			if (initialValue == null) {
				REMOVED.remove();
			} else {
				REMOVED.set(initialValue);
			}
		}
	}

	public static boolean getCurrentRemovedValue() {
		return REMOVED.get() != null;
	}
}
