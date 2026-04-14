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

package net.fabricmc.fabric.api.event.lifecycle.v1;

import org.jspecify.annotations.Nullable;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;

/**
 * Represents extra load data for an {@link Entity}.
 */
public interface EntityLoadData {
	/**
	 * @return The {@link EntitySpawnReason}, which can be null.
	 * On the client, this is almost always {@link EntitySpawnReason#LOAD}.
	 */
	default @Nullable EntitySpawnReason spawnReason() {
		throw new UnsupportedOperationException("Implemented via mixin!");
	}

	/**
	 * @return true if the entity was loaded from disk.
	 * On the client, this is always false.
	 */
	default boolean isLoadedFromDisk() {
		throw new UnsupportedOperationException("Implemented via mixin!");
	}
}
