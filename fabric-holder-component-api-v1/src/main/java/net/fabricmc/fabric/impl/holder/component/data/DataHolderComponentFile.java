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

package net.fabricmc.fabric.impl.holder.component.data;

import java.util.Map;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.resources.ResourceKey;

// TODO: Make public api and use for datagen
public record DataHolderComponentFile<T, C>(
		boolean replace,
		Map<ResourceKey<T>, C> components
) {
	public static <T, C> Codec<DataHolderComponentFile<T, C>> codec(ResourceKey<? extends Registry<T>> registryKey, DataComponentType<C> componentType) {
		return RecordCodecBuilder.create(instance -> instance.group(
				Codec.BOOL.optionalFieldOf("replace", false).forGetter(DataHolderComponentFile::replace),
				Codec.unboundedMap(
						ResourceKey.codec(registryKey),
						componentType.codecOrThrow()
				).fieldOf("components").forGetter(DataHolderComponentFile::components)
		).apply(instance, DataHolderComponentFile::new));
	}
}
