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

import java.util.function.Predicate;

import com.mojang.serialization.Codec;
import org.jspecify.annotations.Nullable;

import net.minecraft.resources.Identifier;

import net.fabricmc.fabric.api.permission.v1.PermissionNode;

public record PermissionNodeImpl<T>(Identifier key, Codec<T> codec, Predicate<Object> castPredicate) implements PermissionNode<T> {
	public static final Predicate<Object> BOOLEAN = o -> o.getClass() == Boolean.class;
	public static final Predicate<Object> INTEGER = o -> o.getClass() == Integer.class;
	public static final Predicate<Object> STRING = o -> o.getClass() == String.class;

	@Override
	@Nullable
	public T cast(@Nullable Object value) {
		if (value == null) {
			return null;
		} else if (castPredicate.test(value)) {
			//noinspection unchecked
			return (T) value;
		}

		throw new IllegalArgumentException("The provided value is not compatible with this node's type!");
	}
}
