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

package net.fabricmc.fabric.api.event.registry.entrylists;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;

import net.fabricmc.fabric.impl.registry.sync.entrylists.CustomRegistryEntryListSerializerImpl;

public interface CustomRegistryEntryListSerializer {
	/**
	 * registers a {@link CustomRegistryEntryListSerializer}. this must be done before the list is used
	 * @param serializer the serializer to register
	 */
	static void registerSerializer(@NotNull CustomRegistryEntryListSerializer serializer) {
		CustomRegistryEntryListSerializerImpl.registerSerializer(serializer);
	}

	/**
	 * gets a registered serializer.
	 * @param id the identifier of the serializer
	 * @return the serializer, or {@code null} if no serializer is registered with the given id
	 */
	static @Nullable CustomRegistryEntryListSerializer getSerializer(Identifier id) {
		return CustomRegistryEntryListSerializerImpl.getSerializer(id);
	}

	/**
	 * used to encode the entry list over the network, or in json.
	 * @return the registry id of this serializer
	 */
	Identifier getIdentifier();

	<T1> MapCodec<? extends CustomRegistryEntryList<T1>> createCodec(RegistryKey<? extends Registry<T1>> registryKey, Codec<RegistryEntry<T1>> entryCodec, boolean forceList);

	<T1> PacketCodec<RegistryByteBuf, ? extends CustomRegistryEntryList<T1>> createPacketCodec(RegistryKey<? extends Registry<T1>> registryKey);
}
