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
 * Allows an item to run custom logic when {@link ItemStack#hurtAndBreak(int, LivingEntity, EquipmentSlot)} is called.
 * This is useful for items that, for example, may drain durability from some other source before damaging
 * the stack itself.
 *
 * <p>Custom damage handlers can be set with {@link FabricItem.Settings#customDamage}.
 */
@FunctionalInterface
public interface CustomDamageHandler {
	/**
	 * Called to apply damage to the given stack.
	 * This can be used to e.g. drain from a battery before actually damaging the item.
	 * Note that this does not get called if non-entities, such as dispensers, are damaging the item,
	 * or for thrown tridents.
	 * Calling {@code breakCallback} breaks the item, bypassing the vanilla logic. The return value is
	 * ignored in this case.
	 * @param amount the amount of damage originally requested
	 * @return The amount of damage to pass to vanilla's logic
	 */
	int damage(ItemStack stack, int amount, LivingEntity entity, EquipmentSlot slot, Runnable breakCallback);
}
