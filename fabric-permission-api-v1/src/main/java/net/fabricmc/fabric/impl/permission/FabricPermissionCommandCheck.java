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

import net.fabricmc.fabric.api.permission.v1.PermissionContextOwner;

public record FabricPermissionCommandCheck<T>(Predicate<PermissionContextOwner> predicate,
											Predicate<Object> fallback) implements Predicate<T> {
	@Override
	public boolean test(T context) {
		return context instanceof PermissionContextOwner owner ? this.predicate.test(owner) : fallback.test(context);
	}

	public <S> FabricPermissionCommandCheck<S> cast() {
		//noinspection unchecked
		return (FabricPermissionCommandCheck<S>) this;
	}
}
