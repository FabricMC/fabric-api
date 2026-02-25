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

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
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
	 */
	public static final Event<ServerEntityEvents.Load> ENTITY_LOAD = EventFactory.createArrayBacked(ServerEntityEvents.Load.class, callbacks -> (entity, level) -> {
		for (Load callback : callbacks) {
			callback.onLoad(entity, level);
		}
	});

	/**
	 * Called before an Entity is added to a ServerLevel.
	 *
	 * <p>If return value is {@code false} entity will not be added to a server.</p>
	 *
	 * <p>Should be used when you want to add another entity instead of added or to block adding specific entities.</p>
	 *
	 * {@snippet :
	 * ServerEntityEvents.ALLOW_FRESH_LOAD.register((entity, level) -> {
	 * 	// Spawn with 25% chance zombie instead of creeper
	 * 	if (entity instanceof Creeper && level.getRandom().nextFloat() <= 0.25f) {
	 * 	 var zombie = EntityType.ZOMBIE.create(level, null, entity.blockPosition(), EntitySpawnReason.EVENT, true, false);
	 * 	 if (zombie != null) return !level.addFreshEntity(zombie);
	 * 	}
	 * 	return true;
	 * });
	 * }
	 */
	public static final Event<ServerEntityEvents.AllowFreshLoad> ALLOW_FRESH_LOAD = EventFactory.createArrayBacked(ServerEntityEvents.AllowFreshLoad.class, callbacks -> (entity, level) -> {
		boolean bl = true;

		for (AllowFreshLoad callback : callbacks) {
			bl = bl && callback.onFreshLoad(entity, level);
		}

		return bl;
	});

	/**
	 * Called when an Entity is added to a ServerLevel.
	 *
	 * <p>When this event is called, the entity is already in the level.</p>
	 *
	 * <p>Should be used when you need to do something after entity summon, naturally spawn or any other add reason.</p>
	 * <p>If you need to do something after entity every entity load (not the first one) use ENTITY_LOAD event.</p>
	 *
	 * {@snippet :
	 * ServerEntityEvents.AFTER_FRESH_LOAD.register((entity, level) -> {
	 * 	if (entity instanceof Creeper) {
	 * 	 level.players().forEach(player -> player.sendSystemMessage(Component.literal("Creeper was added")));
	 * 	}
	 * });
	 * }
	 */
	public static final Event<ServerEntityEvents.AfterFreshLoad> AFTER_FRESH_LOAD = EventFactory.createArrayBacked(ServerEntityEvents.AfterFreshLoad.class, callbacks -> (entity, level) -> {
		for (AfterFreshLoad callback : callbacks) {
			callback.afterFreshLoad(entity, level);
		}
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
	public interface AllowFreshLoad {
		boolean onFreshLoad(Entity entity, ServerLevel level);
	}

	@FunctionalInterface
	public interface AfterFreshLoad {
		void afterFreshLoad(Entity entity, ServerLevel level);
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
