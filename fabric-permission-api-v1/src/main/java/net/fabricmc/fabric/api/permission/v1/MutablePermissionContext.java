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

package net.fabricmc.fabric.api.permission.v1;

import org.jspecify.annotations.Nullable;

/**
 * Mutable version of {@link PermissionContext}, intended for creation of custom context conditions.
 */
public interface MutablePermissionContext extends PermissionContext {
	<T> MutablePermissionContext set(PermissionContext.Key<T> key, @Nullable T value);
}
