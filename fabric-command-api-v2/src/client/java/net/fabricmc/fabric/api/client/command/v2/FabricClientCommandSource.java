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

package net.fabricmc.fabric.api.client.command.v2;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.command.CommandSource;
import net.minecraft.entity.Entity;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;

/**
 * Extensions to {@link CommandSource} for client-sided commands.
 */
public interface FabricClientCommandSource extends CommandSource {
	/**
	 * Sends a feedback message to the player.
	 *
	 * @param message the feedback message
	 */
	void sendFeedback(Text message);

	/**
	 * Sends an error message to the player.
	 *
	 * @param message the error message
	 */
	void sendError(Text message);

	/**
	 * Gets the client instance used to run the command.
	 *
	 * @return the client
	 */
	MinecraftClient getClient();

	/**
	 * Gets the player that used the command.
	 *
	 * @return the player
	 */
	ClientPlayerEntity getPlayer();

	/**
	 * Gets the entity that used the command.
	 *
	 * @return the entity
	 */
	default Entity getEntity() {
		return getPlayer();
	}

	/**
	 * Gets the position from where the command has been executed.
	 *
	 * @return the position
	 */
	default Vec3d getPosition() {
		return getPlayer().getEntityPos();
	}

	/**
	 * Gets the rotation of the entity that used the command.
	 *
	 * @return the rotation
	 */
	default Vec2f getRotation() {
		return getPlayer().getRotationClient();
	}

	/**
	 * Gets the world where the player used the command.
	 *
	 * @return the world
	 */
	ClientWorld getWorld();

	/**
	 * Gets the meta property under {@code key} that was assigned to this source.
	 *
	 * <p>This method should return the same result for every call with the same {@code key}.
	 *
	 * @param key the meta key
	 * @return the meta
	 */
	default Object getMeta(String key) {
		return null;
	}
}
