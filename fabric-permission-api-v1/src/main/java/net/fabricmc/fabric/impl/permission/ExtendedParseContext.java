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

package net.fabricmc.fabric.impl.permission;

import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.commands.ParseContext;
import net.minecraft.commands.arguments.ValidationWrapper;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.server.permissions.PermissionSet;
import net.minecraft.util.context.ContextKeySet;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.level.storage.loot.Validatable;

import net.fabricmc.fabric.api.permission.v1.PermissionContext;

public record ExtendedParseContext(ParseContext context, PermissionContext permissionContext) implements ParseContext {
	@Override
	public HolderGetter.Provider holderGetters() {
		return this.context.holderGetters();
	}

	@Override
	public FeatureFlagSet enabledFeatures() {
		return this.context.enabledFeatures();
	}

	@Override
	public <T extends Validatable> ValidationWrapper<T> getOrCreateValidationWrapper(Holder<T> element, ContextKeySet contextKeySet) throws CommandSyntaxException {
		return this.context.getOrCreateValidationWrapper(element, contextKeySet);
	}

	@Override
	public PermissionSet permissions() {
		return this.context.permissions();
	}

	@Override
	public PermissionContext getPermissionContext() {
		return this.permissionContext;
	}
}
