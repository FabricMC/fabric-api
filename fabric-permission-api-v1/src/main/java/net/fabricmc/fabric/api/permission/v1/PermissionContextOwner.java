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

package net.fabricmc.fabric.api.permission.v1;

import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.Nullable;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.resources.Identifier;
import net.minecraft.server.permissions.PermissionLevel;
import net.minecraft.world.entity.Entity;

import net.fabricmc.fabric.api.util.TriState;

/**
 * Utility interface allowing quick access for permission checking methods.
 * Implemented by default on {@link Entity}, {@link CommandSourceStack} and {@link PermissionContext}.
 * Other mods are allowed to implement this on their own classes as well.
 *
 * <p>See {@link PermissionContext} for creation and modification of permission contexts.
 *
 * <p>Example usage:
 * <pre>{@code
 * Identifier claimBypassPermission = Identifier.fromNamespaceAndPath("potatoclaims", "bypass_protection");
 * ServerPlayer player = ...;
 *
 * AttackEntityCallback.EVENT.register((playerEntity, _, _, entity, _) -> {
 *     if (ModChecks.isProtected(entity) && !player.checkPermission(claimBypassPermission, PermissionLevel.GAMEMASTERS)) {
 *         return InteractionResult.FAIL;
 *     }
 *     return InteractionResult.PASS;
 * });
 * }</pre>
 */
public interface PermissionContextOwner {
	/**
	 * Provides the permission context.
	 * In case of entities, this context will be dynamic.
	 *
	 * @return PermissionContext attached to this object
	 */
	default PermissionContext getPermissionContext() {
		throw new IllegalStateException("Implemented via Mixin");
	}

	/**
	 * Simple permission check. Should be used to check if something is allowed.
	 *
	 * @param permission a permission identifier to check against
	 * @return TriState returning value of the permission (DEFAULT if not changed)
	 */
	default TriState checkPermission(Identifier permission) {
		return TriState.of(this.checkPermission(PermissionNode.of(permission)));
	}

	/**
	 * Simple permission check. Should be used to check if something is allowed.
	 * Will default to {@param defaultValue} if permission value is not provided.
	 *
	 * @param permission a permission identifier to check against
	 * @param defaultValue fallback value
	 * @return a boolean representing state of the permission, returns defaultValue if not modified by other mods
	 */
	default boolean checkPermission(Identifier permission, boolean defaultValue) {
		Boolean value = this.checkPermission(PermissionNode.of(permission));
		return value != null ? value : defaultValue;
	}

	/**
	 * Simple permission check. Should be used to check if something is allowed.
	 * Will check for vanilla permission level, if permission value is not provided.
	 *
	 * @param permission a permission identifier to check against
	 * @param defaultPermissionLevel a fallback permission level to check against
	 * @return a boolean representing state of the permission
	 */
	default boolean checkPermission(Identifier permission, PermissionLevel defaultPermissionLevel) {
		PermissionLevel permissionLevel = this.getPermissionContext().permissionLevel();
		return this.checkPermission(PermissionNode.of(permission), permissionLevel.isEqualOrHigherThan(defaultPermissionLevel));
	}

	/**
	 * A dynamic, typed permission check. Should be used to check for more complex permission values,
	 * like allowed amount and alike.
	 *
	 * @param permission a permission node to check against
	 * @param <T> type of the permission
	 * @return value of the permission or null if not provided
	 */
	@Nullable
	default <T> T checkPermission(PermissionNode<T> permission) {
		return this.checkPermission(permission, null);
	}

	/**
	 * A dynamic, typed permission check. Should be used to check for more complex permission values,
	 * like allowed amount and alike.
	 *
	 * @param permission a permission node to check against
	 * @param defaultValue fallback value, if not provided
	 * @param <T> type of the permission
	 * @return  value of the permission or {@param defaultValue} if not provided
	 */
	@Contract("_, null -> _; _, !null -> !null")
	default <T> @Nullable T checkPermission(PermissionNode<T> permission, @Nullable T defaultValue) {
		T value = PermissionEvents.ON_REQUEST.invoker().handlePermissionRequest(this.getPermissionContext(), permission);

		return value != null ? value : defaultValue;
	}
}
