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

package net.fabricmc.fabric.api.event.player;

import org.jetbrains.annotations.Nullable;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.world.World;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;

/**
 * Callback for right-clicking ("using") an entity.
 * Is hooked in before the spectator check, so make sure to check for the player's game mode as well!
 *
 * <p>On the logical client, the return values have the following meaning:
 * <ul>
 *     <li>SUCCESS cancels further processing, causes a hand swing, and sends a packet to the server.</li>
 *     <li>CONSUME cancels further processing, and sends a packet to the server. It does NOT cause a hand swing.</li>
 *     <li>PASS falls back to further processing.</li>
 *     <li>FAIL cancels further processing and does not send a packet to the server.</li>
 * </ul>
 *
 * <p>On the logical server, the return values have the following meaning:
 * <ul>
 *     <li>PASS falls back to further processing.</li>
 *     <li>Any other value cancels further processing.</li>
 * </ul>
 *
 * <p>Note that on the server, the {@link EntityHitResult} may be {@code null} if the client successfully interacted using
 * the {@linkplain PlayerEntity#interact(Entity, Hand) position-less overload}.
 * On the client, the {@link EntityHitResult} will never be null.
 */
public interface UseEntityCallback {
	Event<UseEntityCallback> EVENT = EventFactory.createArrayBacked(UseEntityCallback.class,
			(listeners) -> (player, world, hand, entity, hitResult) -> {
				for (UseEntityCallback event : listeners) {
					ActionResult result = event.interact(player, world, hand, entity, hitResult);

					if (result != ActionResult.PASS) {
						return result;
					}
				}

				return ActionResult.PASS;
			}
	);

	ActionResult interact(PlayerEntity player, World world, Hand hand, Entity entity, @Nullable EntityHitResult hitResult);
}
