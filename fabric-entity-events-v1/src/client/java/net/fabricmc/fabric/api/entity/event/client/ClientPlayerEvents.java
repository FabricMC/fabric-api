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

package net.fabricmc.fabric.api.entity.event.client;

import org.jetbrains.annotations.Nullable;

import net.minecraft.client.network.ClientPlayerEntity;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;

public final class ClientPlayerEvents {
	/**
	 * An event that is called when a player is moving during using an item.
	 */
	public static final Event<ModifyItemUseSpeed> MODIFY_ITEM_USE_SPEED = EventFactory.createArrayBacked(ModifyItemUseSpeed.class, callbacks -> player -> {
		Float maxSpeedPercentage = null;

		for (ModifyItemUseSpeed callback : callbacks) {
			Float speedPercentage = callback.modifyItemUseSpeed(player);

			if (speedPercentage != null) {
				maxSpeedPercentage = maxSpeedPercentage == null ? speedPercentage : Math.max(speedPercentage, maxSpeedPercentage);
			}
		}

		return maxSpeedPercentage;
	});

	@FunctionalInterface
	public interface ModifyItemUseSpeed {
		/**
		 * Called when a player is moving during using an item.
		 *
		 * @param player the player is moving during using an item.
		 * @return a Float representing the speed modifyment as a percentage (e.g., 0.8 for 80% speed),
		 * or {@code null} indicates that no modifyment should be applied.
		 */
		@Nullable
		Float modifyItemUseSpeed(ClientPlayerEntity player);
	}
}
