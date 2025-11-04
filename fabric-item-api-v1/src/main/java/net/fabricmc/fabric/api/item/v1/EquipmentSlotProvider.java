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

package net.fabricmc.fabric.api.item.v1;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

/**
 * A provider for the preferred equipment slot of an item.
 * This can be used to give non-armor items, such as blocks,
 * an armor slot that they can go in.
 *
 * <p>The preferred equipment slot of an item stack can be queried using
 * {@link LivingEntity#getEquipmentSlotForItem(ItemStack)}  LivingEntity.getPreferredEquipmentSlot()}.
 *
 * <p>Equipment slot providers can be set with {@link FabricItem.Settings#equipmentSlot(EquipmentSlotProvider)}.
 *
 * <p>If the equipment slot is not entity-dependent, you can set {@link
 * net.minecraft.world.item.equipment.Equippable} on the item
 * instead of using this provider.
 */
@FunctionalInterface
public interface EquipmentSlotProvider {
	/**
	 * Gets the preferred equipment slot for an item stack.
	 *
	 * <p>If there is no preferred armor equipment slot for the stack,
	 * {@link EquipmentSlot#MAINHAND} can be returned.
	 *
	 * <p>Callers are expected to check themselves whether the slot is available for the
	 * {@code entity} using {@link LivingEntity#canUseSlot}. For example, players
	 * cannot use {@link EquipmentSlot#BODY}, which is instead used for items like horse armors.
	 *
	 * @param entity the entity
	 * @param stack the item stack
	 * @return the preferred equipment slot
	 */
	EquipmentSlot getPreferredEquipmentSlot(LivingEntity entity, ItemStack stack);
}
