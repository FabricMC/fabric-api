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

package net.fabricmc.fabric.api.datagen.v1.smithing;

import net.minecraft.component.ComponentChanges;

/**
 * Fabric-provided extensions for {@link net.minecraft.data.recipe.SmithingTransformRecipeJsonBuilder}.
 *
 * <p>Allows specification of the transform recipe result item count and component changes.
 */
public interface FabricSmithingTransformRecipeJsonBuilder {
	default ComponentChanges getComponentChanges() {
		throw new UnsupportedOperationException("Implemented via mixin");
	}

	default int getCount() {
		throw new UnsupportedOperationException("Implemented via mixin");
	}

	default void setComponentChanges(ComponentChanges componentChanges) {
		throw new UnsupportedOperationException("Implemented via mixin");
	}

	default void setCount(int count) {
		throw new UnsupportedOperationException("Implemented via mixin");
	}
}
