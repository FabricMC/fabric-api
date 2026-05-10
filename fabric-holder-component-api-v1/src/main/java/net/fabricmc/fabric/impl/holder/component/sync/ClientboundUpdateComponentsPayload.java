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

package net.fabricmc.fabric.impl.holder.component.sync;


import java.util.Map;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Reference2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;

import net.minecraft.core.Registry;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;

public record ClientboundUpdateComponentsPayload(
		// component values have to be sent as nbt tags as the StreamCodecs for them require RegistryFriendlyByteBuf which we don't have during configure
		// this is hopefully fine since they aren't updated often
		// TODO: optimize this format. im quite sure this could be smaller (ComponentTypeId could maybe be the int id?)
		// Map<RegistryKey, Map<HolderId, Map<ComponentTypeId, ComponentValue>>>
		Map<ResourceKey<? extends Registry<?>>, Map<Identifier, Map<Identifier, Tag>>> registryToComponents
) implements CustomPacketPayload {
	public static final Type<ClientboundUpdateComponentsPayload> TYPE = new Type<>(Identifier.fromNamespaceAndPath("fabric", "update_holder_components"));

	// (crime against humanity in codec form)
	public static final StreamCodec<FriendlyByteBuf, ClientboundUpdateComponentsPayload> STREAM_CODEC =
			ByteBufCodecs.<FriendlyByteBuf, ResourceKey<? extends Registry<?>>, Map<Identifier, Map<Identifier, Tag>>, Map<ResourceKey<? extends Registry<?>>, Map<Identifier, Map<Identifier, Tag>>>>map(
					Object2ObjectOpenHashMap::new,
					StreamCodec.ofMember(
							(key, buf) -> buf.writeResourceKey(key),
							FriendlyByteBuf::readRegistryKey
					),
					ByteBufCodecs.map(
							Object2ObjectOpenHashMap::new,
							Identifier.STREAM_CODEC,
							ByteBufCodecs.map(
									size -> size < 8 // this is straight out of the DataComponentMap.CODEC
											? new Reference2ObjectArrayMap<>(size)
											: new Reference2ObjectOpenHashMap<>(size),
									Identifier.STREAM_CODEC,
									ByteBufCodecs.TAG
							)
					)
			).map(
					ClientboundUpdateComponentsPayload::new,
					ClientboundUpdateComponentsPayload::registryToComponents
			);

	@Override
	public Type<ClientboundUpdateComponentsPayload> type() {
		return TYPE;
	}
}
