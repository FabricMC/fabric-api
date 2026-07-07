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

package net.fabricmc.fabric.mixin.permission;

import java.util.UUID;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.permissions.PermissionSet;
import net.minecraft.util.Util;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;

import net.fabricmc.fabric.api.permission.v1.PermissionContext;
import net.fabricmc.fabric.api.permission.v1.PermissionContextOwner;
import net.fabricmc.fabric.impl.permission.CommandPermissionContext;

@Mixin(CommandSourceStack.class)
public abstract class CommandSourceStackMixin implements PermissionContextOwner, CommandPermissionContext.Extension {
	@Unique
	private final PermissionContext context = new CommandPermissionContext((CommandSourceStack) ((Object) this));
	@Unique
	private PermissionContext.Type sourceType = PermissionContext.Type.SYSTEM;
	@Unique
	private UUID sourceUuid = Util.NIL_UUID;

	@Inject(method = "<init>(Lnet/minecraft/commands/CommandSource;Lnet/minecraft/world/phys/Vec3;Lnet/minecraft/world/phys/Vec2;Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/server/permissions/PermissionSet;Ljava/lang/String;Lnet/minecraft/network/chat/Component;Lnet/minecraft/server/MinecraftServer;Lnet/minecraft/world/entity/Entity;)V", at = @At("TAIL"))
	private void storeOriginalSource(CommandSource source, Vec3 position, Vec2 rotation, ServerLevel level, PermissionSet permissions, String textName, Component displayName, MinecraftServer server, Entity entity, CallbackInfo ci) {
		this.sourceType = switch (entity) {
		case Player _ -> PermissionContext.Type.PLAYER;
		case Entity _ -> PermissionContext.Type.ENTITY;
		case null -> PermissionContext.Type.SYSTEM;
		};
		this.sourceUuid = switch (entity) {
		case Entity _ -> entity.getUUID();
		case null -> Util.NIL_UUID;
		};
	}

	@SuppressWarnings("DataFlowIssue")
	@ModifyReturnValue(method = "/^with/ desc=/CommandSourceStack;$/", at = @At("RETURN"))
	private CommandSourceStack copyOriginalOwner(CommandSourceStack result) {
		((CommandSourceStackMixin) (Object) result).sourceUuid = this.sourceUuid;
		((CommandSourceStackMixin) (Object) result).sourceType = this.sourceType;
		return result;
	}

	@Override
	public PermissionContext.Type fabric_getType() {
		return this.sourceType;
	}

	@Override
	public UUID fabric_getUuid() {
		return this.sourceUuid;
	}

	@Override
	public PermissionContext getPermissionContext() {
		return this.context;
	}
}
