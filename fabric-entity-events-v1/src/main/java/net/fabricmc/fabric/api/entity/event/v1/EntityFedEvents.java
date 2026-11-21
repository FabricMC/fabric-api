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

package net.fabricmc.fabric.api.entity.event.v1;

import net.minecraft.world.entity.animal.equine.AbstractHorse;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;

public final class EntityFedEvents {
	/**
	 * An event that is run when a horse is fed by the player.
	 *
	 * <p>This event allows for both adding custom values for new horse food, along with modifying the values for food from either vanilla or a previous listener</p>
	 *
	 * <p>New foods must still be added to the {@link net.minecraft.tags.ItemTags#HORSE_FOOD ItemTags.HORSE_FOOD} tag.</p>
	 *
	 * <p>You can effectively cancel this feeding by setting all values to {@code 0} or {@code false}.</p>
	 *
	 * @see HorseFed.HorseFood HorseFood
	 */
	public static final Event<HorseFed> HORSE_FED = EventFactory.createArrayBacked(HorseFed.class, callbacks -> (horse, stack, player, food) -> {
		for (HorseFed callback : callbacks) {
			callback.onFed(horse, stack, player, food);
		}
	});

	private EntityFedEvents() {
	}

	@FunctionalInterface
	public interface HorseFed {
		/**
		 * <table>
		 *     <caption>The different properties of horse food.</caption>
		 *     <thead><tr><th>Property</th><th>Getter</th><th>Setter</th><th>Purpose</th></tr></thead>
		 *     <tbody>
		 *         <tr><td>Heal Amount</td><td>{@link #getHealAmount() getHealAmount}</td><td>{@link #setHealAmount(float) setHealAmount}</td><td>Heals the horse</td></tr>
		 *         <tr><td>Age Up Seconds</td><td>{@link #getAgeUpSeconds() getAgeUpSeconds}</td><td>{@link #setAgeUpSeconds(int) setAgeUpSeconds}</td><td>Ages up a baby horse</td></tr>
		 *         <tr><td>Temper Increase </td><td>{@link #getTemperIncrease() getTemperIncrease}</td><td>{@link #setTemperIncrease(int) setTemperIncrease}</td><td>Increases the temper on a horse, making it easier to tame</td></tr>
		 *         <tr><td>Allows Breeding</td><td>{@link #getAllowsBreeding() getAllowsBreeding}</td><td>{@link #setAllowsBreeding(boolean) setAllowsBreeding}</td><td>Will set the horse in love, allowing it to breed</td></tr>
		 *     </tbody>
		 * </table>
		 */
		interface HorseFood {
			/**
			 * @return The health points restored by the food.
			 */
			float getHealAmount();

			/**
			 * @param heal the health points to be restored by the food
			 */
			void setHealAmount(float heal);

			/**
			 * @return The number of seconds the food ages the horse up by.
			 */
			int getAgeUpSeconds();

			/**
			 * @param ageUpSeconds the number of seconds to age the horse up by
			 */
			void setAgeUpSeconds(int ageUpSeconds);

			/**
			 * @return The number of temper points added by the food.
			 */
			int getTemperIncrease();

			/**
			 * @param temperIncrease the number of temper points to be added by the food.
			 */
			void setTemperIncrease(int temperIncrease);

			/**
			 * @return {@code true} if the food will allow the horse to be in love, {@code false} otherwise.
			 */
			boolean getAllowsBreeding();

			/**
			 * @param allowsBreeding {@code true} to allow the horse to be in love, {@code false} otherwise.
			 */
			void setAllowsBreeding(boolean allowsBreeding);
		}

		/**
		 * Called when a horse is fed. Run on both the server and client, but properties are only applied on the server.
		 *
		 * @param horse the horse entity
		 * @param stack the food stack
		 * @param player the feeding player
		 * @param food the food properties. See {@link HorseFood HorseFood}
		 */
		void onFed(AbstractHorse horse, ItemStack stack, Player player, HorseFood food);
	}
}
