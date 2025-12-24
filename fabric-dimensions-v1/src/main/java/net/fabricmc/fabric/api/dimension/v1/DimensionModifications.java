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

import net.minecraft.resources.Identifier;

/**
 * Provides an API to modify dimensions after they have been loaded and before they are used in the World.
 *
 * <p>Any modifications made to dimensions will not be available for use in the demo level.
 */
public final class DimensionModifications {
	private DimensionModifications() {
	}

	/**
	 * Creates a new dimension modification which will be applied whenever dimensions are loaded from data packs.
	 *
	 * @param id An identifier for the new set of dimension modifications that is returned. Is used for
	 *           guaranteeing consistent ordering between the dimension modifications added by different mods
	 *           (assuming they otherwise have the same phase).
	 */
	public static DimensionModification create(Identifier id) {
		return new DimensionModification(id);
	}
}
