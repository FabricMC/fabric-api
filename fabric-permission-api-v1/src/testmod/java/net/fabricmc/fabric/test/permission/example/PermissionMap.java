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

package net.fabricmc.fabric.test.permission.example;

import java.util.Collections;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Optional;

import com.mojang.serialization.Codec;
import org.jspecify.annotations.Nullable;

import net.minecraft.nbt.ByteTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.Identifier;

public record PermissionMap(Map<Identifier, PermissionValue> permissions) {
	public PermissionMap() {
		this(new HashMap<>());
	}

	public void set(Identifier identifier, Tag tag) {
		this.permissions.put(identifier, new PermissionValue(tag));
	}

	public void set(Identifier identifier, boolean val) {
		this.permissions.put(identifier, new PermissionValue(ByteTag.valueOf(val)));
	}

	public void set(Identifier identifier, int val) {
		this.permissions.put(identifier, new PermissionValue(IntTag.valueOf(val)));
	}

	public void set(Identifier identifier, String val) {
		this.permissions.put(identifier, new PermissionValue(StringTag.valueOf(val)));
	}

	@Nullable
	public <T> T get(Identifier identifier, Codec<T> codec) {
		PermissionValue value = this.permissions.get(identifier);
		return value != null ? value.get(codec) : null;
	}

	public Tag getRaw(Identifier identifier) {
		PermissionValue value = this.permissions.get(identifier);
		return value != null ? value.tag() : null;
	}

	public record PermissionValue(Tag tag, Map<Codec<?>, Optional<Object>> map) {
		public PermissionValue(Tag tag) {
			this(tag, Collections.synchronizedMap(new IdentityHashMap<>()));
		}

		@SuppressWarnings("unchecked")
		@Nullable
		public <T> T get(Codec<T> codec) {
			Optional<T> val = (Optional<T>) this.map.get(codec);

			//noinspection OptionalAssignedToNull
			if (val != null) {
				return val.orElse(null);
			}

			Optional<T> parse = codec.parse(NbtOps.INSTANCE, tag).result();
			this.map.put(codec, (Optional<Object>) parse);
			return parse.orElse(null);
		}
	}
}
