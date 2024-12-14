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

package net.fabricmc.fabric.impl.tag;

import java.util.Map;
import java.util.Set;

import net.minecraft.registry.tag.TagKey;

/**
 * Implemented on {@code RegistryWrapper.Impl} instances used during data loading
 * to give access to the underlying registry.
 */
public interface TagAliasEnabledRegistryWrapper {
	void fabric_loadTagAliases(Map<TagKey<?>, Set<TagKey<?>>> aliasGroups);
}
