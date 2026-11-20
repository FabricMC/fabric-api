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

package net.fabricmc.fabric.impl.permission;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.jspecify.annotations.Nullable;

import net.minecraft.server.permissions.PermissionLevel;

import net.fabricmc.fabric.api.permission.v1.MutablePermissionContext;

public record CustomPermissionContext(UUID uuid, Type type, PermissionLevel permissionLevel, Map<Key<?>, Object> overrides) implements MutablePermissionContext {
	public CustomPermissionContext(UUID uuid, Type type, PermissionLevel permissionLevel) {
		this(uuid, type, permissionLevel, new HashMap<>());
	}

	@Override
	public <T> MutablePermissionContext set(Key<T> key, @Nullable T value) {
		if (value != null) {
			this.overrides.put(key, value);
		} else {
			this.overrides.remove(key);
		}

		return this;
	}

	@Override
	public @Nullable <T> T get(Key<T> key) {
		//noinspection unchecked
		return (T) this.overrides.get(key);
	}

	@Override
	public Set<Key<?>> keys() {
		return Collections.unmodifiableSet(this.overrides.keySet());
	}
}
