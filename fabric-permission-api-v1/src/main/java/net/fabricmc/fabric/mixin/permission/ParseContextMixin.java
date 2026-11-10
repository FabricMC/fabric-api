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

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.ParseContext;
import net.minecraft.server.level.ServerPlayer;

import net.fabricmc.fabric.api.permission.v1.PermissionContext;
import net.fabricmc.fabric.api.permission.v1.PermissionContextOwner;
import net.fabricmc.fabric.impl.permission.CustomPermissionContext;
import net.fabricmc.fabric.impl.permission.ExtendedParseContext;

@Mixin(ParseContext.class)
interface ParseContextMixin extends PermissionContextOwner {
	@Override
	default PermissionContext getPermissionContext() {
		return CustomPermissionContext.FALLBACK;
	}

	@ModifyReturnValue(method = "of(Lnet/minecraft/server/level/ServerPlayer;)Lnet/minecraft/commands/ParseContext;", at = @At("RETURN"))
	private static ParseContext attachPermissionContextPlayer(ParseContext original, @Local(argsOnly = true) ServerPlayer sender) {
		return new ExtendedParseContext(original, sender.getPermissionContext());
	}

	@ModifyReturnValue(method = "of(Lnet/minecraft/commands/CommandSourceStack;)Lnet/minecraft/commands/ParseContext;", at = @At("RETURN"))
	private static ParseContext attachPermissionContextCommand(ParseContext original, @Local(argsOnly = true) CommandSourceStack sender) {
		return new ExtendedParseContext(original, sender.getPermissionContext());
	}
}
