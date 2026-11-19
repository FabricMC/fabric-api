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

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;

public final class ServerEntityEvents {
	private ServerEntityEvents() {
	}

	/**
	 * Called when an Entity is loaded into a ServerLevel.
	 *
	 * <p>When this event is called, the entity is already in the level.
	 *
	 * @see Entity#spawnReason()
	 * @see Entity#isLoadedFromDisk()
	 */
	public static final Event<ServerEntityEvents.Load> ENTITY_LOAD = EventFactory.createArrayBacked(ServerEntityEvents.Load.class, callbacks -> (entity, level) -> {
		for (Load callback : callbacks) {
			callback.onLoad(entity, level);
		}
	});

	/**
	 * Called right before an {@link Entity} is loaded into a {@link ServerLevel}. Mods can cancel this to prevent the entity from loading in.
	 */
	public static final Event<AllowLoad> ALLOW_LOAD = EventFactory.createArrayBacked(AllowLoad.class, callbacks -> (entity, level, spawnReason, isLoadedFromDisk) -> {
		for (AllowLoad callback : callbacks) {
			if (!callback.onAllowLoad(entity, level, spawnReason, isLoadedFromDisk)) {
				return false;
			}
		}

		return true;
	});

	/**
	 * Called when an Entity is unloaded from a ServerLevel.
	 *
	 * <p>This event is called before the entity is removed from the level.
	 */
	public static final Event<ServerEntityEvents.Unload> ENTITY_UNLOAD = EventFactory.createArrayBacked(ServerEntityEvents.Unload.class, callbacks -> (entity, level) -> {
		for (Unload callback : callbacks) {
			callback.onUnload(entity, level);
		}
	});

	/**
	 * Called during {@link LivingEntity#tick()} if the Entity's equipment has been changed or mutated.
	 *
	 * <p>This event is also called when the entity joins the level.
	 * A change in equipment is determined by {@link ItemStack#matches(ItemStack, ItemStack)}.
	 */
	public static final Event<EquipmentChange> EQUIPMENT_CHANGE = EventFactory.createArrayBacked(ServerEntityEvents.EquipmentChange.class, callbacks -> (livingEntity, equipmentSlot, previous, next) -> {
		for (EquipmentChange callback : callbacks) {
			callback.onChange(livingEntity, equipmentSlot, previous, next);
		}
	});

	@FunctionalInterface
	public interface Load {
		void onLoad(Entity entity, ServerLevel level);
	}

	@FunctionalInterface
	public interface AllowLoad {
		/**
		 * Called right before an {@link Entity} is loaded into a {@link ServerLevel}.
		 *
		 * @return true to allow the load, false to cancel the load.
		 */
		boolean onAllowLoad(Entity entity, ServerLevel level, @Nullable EntitySpawnReason spawnReason, boolean isLoadedFromDisk);
	}

	@FunctionalInterface
	public interface Unload {
		void onUnload(Entity entity, ServerLevel level);
	}

	@FunctionalInterface
	public interface EquipmentChange {
		void onChange(LivingEntity livingEntity, EquipmentSlot equipmentSlot, ItemStack previousStack, ItemStack currentStack);
	}
}
