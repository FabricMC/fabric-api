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

package net.fabricmc.fabric.mixin.event.lifecycle;

import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;

import net.fabricmc.fabric.api.event.lifecycle.v1.EntityLoadData;
import net.fabricmc.fabric.impl.event.lifecycle.EntityLoadDataSetter;

@Mixin(Entity.class)
abstract class EntityMixin implements EntityLoadDataSetter, EntityLoadData {
	@Unique
	@Nullable
	private EntitySpawnReason fabric_spawnReason = null;

	@Unique
	private boolean fabric_isLoadedFromDisk = false;

	@Unique
	public void fabric_setSpawnReason(EntitySpawnReason spawnReason) {
		this.fabric_spawnReason = spawnReason;
	}

	@Unique
	public void fabric_setLoadedFromDisk(boolean isLoadedFromDisk) {
		this.fabric_isLoadedFromDisk = isLoadedFromDisk;
	}

	@Unique
	public @Nullable EntitySpawnReason spawnReason() {
		return fabric_spawnReason;
	}

	@Unique
	public boolean isLoadedFromDisk() {
		return fabric_isLoadedFromDisk;
	}
}
