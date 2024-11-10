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

package net.fabricmc.fabric.mixin.tag;

import java.util.Map;
import java.util.Set;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.tag.TagKey;

import net.fabricmc.fabric.impl.tag.TagAliasEnabledRegistryWrapper;

/**
 * Adds tag alias support to {@code SimpleRegistry$2}, which is the wrapper used
 * for (TODO: only?) static registries during world creation and world/data reloading.
 */
@Mixin(targets = "net.minecraft.registry.SimpleRegistry$2")
abstract class SimpleRegistry2Mixin<T> implements TagAliasEnabledRegistryWrapper {
	// returns SimpleRegistry.this, which implements TagAliasEnabledRegistry
	@Shadow
	public abstract RegistryWrapper.Impl<T> getBase();

	@Override
	public void fabric_loadTagAliases(Map<TagKey<?>, Set<TagKey<?>>> aliasGroups) {
		((TagAliasEnabledRegistryWrapper) getBase()).fabric_loadTagAliases(aliasGroups);
	}
}
