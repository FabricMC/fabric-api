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

import java.util.List;

import com.mojang.serialization.Codec;

import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.tag.TagKey;

/**
 * A wrapper record for tag alias groups.
 *
 * @param tags the tags in the group, must be from the same registry
 * @param <T> the type of registry entries in the tags
 */
public record TagAliasGroup<T>(List<TagKey<T>> tags) {
	/**
	 * Creates a codec for tag alias groups in the specified registry.
	 *
	 * @param registryKey the key of the registry where the tags are from
	 * @param <T>         the entry type
	 * @return the codec
	 */
	public static <T> Codec<TagAliasGroup<T>> codec(RegistryKey<? extends Registry<T>> registryKey) {
		return TagKey.unprefixedCodec(registryKey)
				.listOf()
				.fieldOf("tags")
				.xmap(TagAliasGroup::new, TagAliasGroup::tags)
				.codec();
	}
}
