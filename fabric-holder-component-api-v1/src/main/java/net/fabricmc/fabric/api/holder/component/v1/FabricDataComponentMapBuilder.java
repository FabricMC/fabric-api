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

package net.fabricmc.fabric.api.holder.component.v1;

import java.util.List;

import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.TypedDataComponent;

/// Extensions for [DataComponentMap.Builder]. Implemented via interface injection, do not implement yourself!
/// @see DataComponentMap.Builder
public interface FabricDataComponentMapBuilder {
	default DataComponentMap.Builder apply(DataComponentPatch patch) {
		throw new UnsupportedOperationException("Implemented via mixin.");
	}

	default <T> DataComponentMap.Builder set(TypedDataComponent<T> typedDataComponent) {
		throw new UnsupportedOperationException("Implemented via mixin.");
	}

	default DataComponentMap.Builder setAll(List<TypedDataComponent<?>> typedDataComponents) {
		throw new UnsupportedOperationException("Implemented via mixin.");
	}

	default DataComponentMap.Builder clear() {
		throw new UnsupportedOperationException("Implemented via mixin.");
	}
}
