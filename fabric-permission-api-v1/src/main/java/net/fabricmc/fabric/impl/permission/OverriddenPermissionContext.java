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

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import com.google.common.collect.Sets;
import org.jspecify.annotations.Nullable;

import net.minecraft.server.permissions.PermissionLevel;

import net.fabricmc.fabric.api.permission.v1.MutablePermissionContext;
import net.fabricmc.fabric.api.permission.v1.PermissionContext;

public record OverriddenPermissionContext(PermissionContext context, Map<Key<?>, Object> overrides) implements MutablePermissionContext {
	public OverriddenPermissionContext(PermissionContext context) {
		this(context, new HashMap<>());
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
	public UUID uuid() {
		return this.context.uuid();
	}

	@Override
	public Type type() {
		return this.context.type();
	}

	@Override
	public @Nullable <T> T get(Key<T> key) {
		if (this.overrides.containsKey(key)) {
			//noinspection unchecked
			return (T) this.overrides.get(key);
		}

		return this.context.get(key);
	}

	@Override
	public PermissionLevel permissionLevel() {
		return this.context.permissionLevel();
	}

	@Override
	public Set<Key<?>> keys() {
		return Sets.union(this.overrides.keySet(), this.context.keys());
	}
}
