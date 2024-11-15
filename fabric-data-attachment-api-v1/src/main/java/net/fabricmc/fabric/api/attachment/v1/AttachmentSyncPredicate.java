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

package net.fabricmc.fabric.api.attachment.v1;

import java.util.function.BiPredicate;

import org.jetbrains.annotations.ApiStatus;

import net.minecraft.server.network.ServerPlayerEntity;

/**
 * A predicate that determines, for a specific attachment type, whether the data should be synchronized with a
 * player's client, given the player's {@link ServerPlayerEntity} and the {@linkplain AttachmentTarget} the data is linked to.
 *
 * <p>The class extends {@link BiPredicate} to allow for custom predicates, outside the ones provided by methods.</p>
 */
@ApiStatus.NonExtendable
@FunctionalInterface
public interface AttachmentSyncPredicate extends BiPredicate<AttachmentTarget, ServerPlayerEntity> {
	/**
	 * @return a predicate that syncs an attachment with all clients
	 */
	static AttachmentSyncPredicate all() {
		return (t, p) -> true;
	}

	/**
	 * @return a predicate that syncs an attachment only with the target it is attached to, when that is a player. If the
	 * target isn't a player, the attachment will be synced with no clients.
	 */
	static AttachmentSyncPredicate targetOnly() {
		return (target, player) -> target == player;
	}

	/**
	 * @return a predicate that syncs an attachment with every client except the target it is attached to, when that is a player.
	 * When the target isn't a player, the attachment will be synced with all clients.
	 */
	static AttachmentSyncPredicate allButTarget() {
		return (target, player) -> target != player;
	}
}
