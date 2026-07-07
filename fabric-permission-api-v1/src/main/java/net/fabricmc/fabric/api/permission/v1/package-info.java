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

/**
 * Permission API is an api allowing mods to easily interact with each
 * other in order to know what is and isn't allowed (aka the titular permissions).
 * This applies to actions done by players, entities and anything else that
 * might do actions you would want other mods to easily prevent.
 *
 * <p>By default, this api provides direct support for checking permissions
 * for {@link net.minecraft.world.entity.player.Player}, {@link net.minecraft.world.entity.Entity}
 * and {@link net.minecraft.commands.CommandSourceStack}, but also creation of custom contexts.
 * To simplify access, these classes now extend the {@link net.fabricmc.fabric.api.permission.v1.PermissionContextOwner}
 * interface allowing for access to the attached {@link net.fabricmc.fabric.api.permission.v1.PermissionContext} (which also extends that interface)
 * as well as providing additional methods for sync and async permission checks.
 * Custom implementations of {@link net.fabricmc.fabric.api.permission.v1.PermissionContextOwner}
 * and {@link net.fabricmc.fabric.api.permission.v1.PermissionContext} are encouraged in places that require
 * more flexibility or lazy evaluation.
 *
 * <p>Permission themselves are typed, which allows to support more types (aside of most common boolean permissions)
 * allowing for more flexibility of the interactions.
 * To create a typed permission node you can use one of the provided static factory methods from
 * the {@link net.fabricmc.fabric.api.permission.v1.PermissionNode} interface. Permission nodes can be
 * created at any point in time, either dynamically or statically. You can then use this object
 * directly as an argument of {@link net.fabricmc.fabric.api.permission.v1.PermissionContextOwner} permission
 * checking methods.
 * Boolean permissions can also use {@link net.minecraft.resources.Identifier} directly,
 * with addition for some extra utility methods on the owner object.
 *
 * <p>To define a provider, you need to register callbacks for events defined in {@link net.fabricmc.fabric.api.permission.v1.PermissionEvents}.
 * By default, you only need to implement the
 * {@link net.fabricmc.fabric.api.permission.v1.PermissionEvents#ON_REQUEST} event, but other ones might still be good to look into to allow better handling of them.
 *
 * <p>Example cases where you might want to use this api:
 * - Commands that might not make sense to give to all players, but might be required for helpers/moderators/admins,
 * - Dynamic limits for things based on external factors (for example warps, max protected area size),
 * - Checking if non-regular in world interaction is allowed within protected area (for example transmuting blocks with a special items, summoning mounts),
 *
 * <p>Example code - Checking for command permission:
 * <pre>{@code
 * CommandRegistrationCallback.EVENT.register((dispatcher, _, _) -> {
 *     dispatcher.register(literal("modcommand")
 *     	   // By using direct Identitier
 *         .requires(PermissionPredicates.require(Identifier.fromNamespaceAndPath("mymod", "command/main"), true))
 *         .executes(ModCommands::executeMainCommand)
 *         .then(literal("admin")
 *             // By using boolean permission node
 *             .requires(PermissionPredicates.require(PermissionNode.of("mymod", "command/admin"), PermissionLevel.ADMINS))
 *             .executes(ModCommands::executeMainCommand)
 *         )
 * });
 * }</pre>
 *
 * <p>Example code - Validating if special interaction works in claim at select position.
 * <pre>{@code
 * // Check side (...)
 * var canSummonMountPermission = PermissionNode.of("mymod", "can_summon_mount");
 * public boolean trySummoningMount(Player player, Vec3 pos) {
 *     var context = player.getPermissionContext().mutable()
 *     				.set(PermissionContext.BLOCK_POSITION, BlockPos.containing(pos))
 *     				.set(PermissionContext.POSITION, pos);
 *
 *     	if (!context.checkPermission(canSummonMountPermission, true)) {
 *     	    player.sendSystemMessage(Component.literal("You can't summon your mount here!"));
 *     	    return false;
 *     	}
 *
 *     	// Mount summoning logic goes here (...)
 *     	return true;
 * }
 *
 * // Protection mod / validation side (...)
 * var checkedPermissions = Set.of(Identifier.fromNamespaceAndPath("mymod", "can_summon_mount"), ...);
 *
 * PermissionEvents.register((context, permission) -> {
 * 		if (context.type() != PermissionContext.Type.PLAYER && context.type() != PermissionContext.Type.ENTITY) return null;
 *
 * 		var pos = context.get(PermissionContext.BLOCK_POSITION);
 * 		if (pos == null) return null;
 *
 * 		var claim = ClaimMod.getClaimAt(pos);
 * 		if (claim == null) return null;
 *
 *      if (checkedPermissions.contains(permission.key()) && permission.codec() == Codec.BOOL) {
 *          return (T) Boolean.valueOf(claim.canModifyClaim(context.uuid()));
 *      }
 *      // Any other logic...
 *      return null;
 * });
 * }</pre>
 */
@ApiStatus.Experimental
@NullMarked
package net.fabricmc.fabric.api.permission.v1;

import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.NullMarked;
