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

package net.fabricmc.fabric.api.debug.v1;

/// A factory that creates a debug value of type [T] using data of type [D].
///
/// @param <D> the data passed for construction
/// (e.g. [net.minecraft.world.entity.Entity]).
/// @param <T> the debug value being constructed.
@FunctionalInterface
public interface DebugValueFactory<D, T> {
	T create(D data);
}
