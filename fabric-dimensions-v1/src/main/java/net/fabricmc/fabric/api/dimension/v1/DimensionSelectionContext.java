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

package net.fabricmc.fabric.api.dimension.v1;

import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.dimension.DimensionType;

/**
 * Context given to a dimension selector for deciding whether it applies to a dimension or not.
 */
public interface DimensionSelectionContext {
	RegistryAccess getDynamicRegistries();

	ResourceKey<DimensionType> getDimensionKey();

	/**
	 * Returns the dimension with modifications by dimension modifiers of higher priority already applied.
	 */
	DimensionType getDimension();

	Holder<DimensionType> getDimensionRegistryEntry();

	/**
	 * {@return true if this dimension is in the given {@link TagKey }}.
	 */
	boolean hasTag(TagKey<DimensionType> tag);
}
