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

package net.fabricmc.fabric.impl.registry.sync.packet;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import it.unimi.dsi.fastutil.objects.Object2IntLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;

import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;

import net.fabricmc.fabric.api.event.registry.RegistryAttribute;
import net.fabricmc.fabric.api.event.registry.RegistryAttributeHolder;
import net.fabricmc.fabric.impl.registry.sync.RegistryAttributeImpl;

/**
 * A more optimized method to sync registry ids to client.
 * Produces smaller packets than the old nbt-based method.
 *
 * <p>This method optimizes the packet in multiple ways:
 * <ul>
 *     <li>Directly writes into the buffer instead of using an nbt;</li>
 *     <li>Groups all {@link Identifier}s with same namespace together and only sends those unique namespaces once for each group;</li>
 *     <li>Groups consecutive rawIds together and only sends the difference of the first rawId and the last rawId of the bulk before.
 *     This is based on the assumption that mods generally register all of their objects at once,
 *     therefore making the rawIds somewhat densely packed.</li>
 * </ul>
 */
public record RegistrySyncPayload(
		Map<Identifier, Object2IntMap<Identifier>> registryMap,
		Map<Identifier, EnumSet<RegistryAttribute>> registryAttributes
) implements CustomPacketPayload {
	public static final CustomPacketPayload.Type<RegistrySyncPayload> ID = new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath("fabric", "registry/sync"));
	public static final StreamCodec<FriendlyByteBuf, RegistrySyncPayload> CODEC = CustomPacketPayload.codec(RegistrySyncPayload::write, RegistrySyncPayload::read);

	public RegistrySyncPayload(Map<Identifier, Object2IntMap<Identifier>> registryMap) {
		this(registryMap, getRegistryAttributeMap(registryMap));
	}

	private static Map<Identifier, EnumSet<RegistryAttribute>> getRegistryAttributeMap(Map<Identifier, Object2IntMap<Identifier>> registryMap) {
		Map<Identifier, EnumSet<RegistryAttribute>> registryAttributes = new LinkedHashMap<>();
		registryMap.forEach((regId, idMap) -> {
			ResourceKey<Registry<Object>> registryKey = ResourceKey.createRegistryKey(regId);
			RegistryAttributeImpl holder = (RegistryAttributeImpl) RegistryAttributeHolder.get(registryKey);
			registryAttributes.put(regId, holder.getAttributes());
		});
		return registryAttributes;
	}

	private static RegistrySyncPayload read(FriendlyByteBuf combinedBuf) {
		Map<Identifier, Object2IntMap<Identifier>> syncedRegistryMap = new LinkedHashMap<>();
		Map<Identifier, EnumSet<RegistryAttribute>> syncedRegistryAttributes = new LinkedHashMap<>();
		int regNamespaceGroupAmount = combinedBuf.readVarInt();

		for (int i = 0; i < regNamespaceGroupAmount; i++) {
			String regNamespace = unoptimizeNamespace(combinedBuf.readUtf());
			int regNamespaceGroupLength = combinedBuf.readVarInt();

			for (int j = 0; j < regNamespaceGroupLength; j++) {
				String regPath = combinedBuf.readUtf();
				EnumSet<RegistryAttribute> attributes = decodeRegistryAttributes(combinedBuf.readByte());
				Object2IntMap<Identifier> idMap = new Object2IntLinkedOpenHashMap<>();
				int idNamespaceGroupAmount = combinedBuf.readVarInt();

				int lastBulkLastRawId = 0;

				for (int k = 0; k < idNamespaceGroupAmount; k++) {
					String idNamespace = unoptimizeNamespace(combinedBuf.readUtf());
					int rawIdBulkAmount = combinedBuf.readVarInt();

					for (int l = 0; l < rawIdBulkAmount; l++) {
						int bulkRawIdStartDiff = combinedBuf.readVarInt();
						int bulkSize = combinedBuf.readVarInt();

						int currentRawId = (lastBulkLastRawId + bulkRawIdStartDiff) - 1;

						for (int m = 0; m < bulkSize; m++) {
							currentRawId++;
							String idPath = combinedBuf.readUtf();
							idMap.put(Identifier.fromNamespaceAndPath(idNamespace, idPath), currentRawId);
						}

						lastBulkLastRawId = currentRawId;
					}
				}

				Identifier registryId = Identifier.fromNamespaceAndPath(regNamespace, regPath);
				syncedRegistryMap.put(registryId, idMap);
				syncedRegistryAttributes.put(registryId, attributes);
			}
		}

		return new RegistrySyncPayload(syncedRegistryMap, syncedRegistryAttributes);
	}

	private void write(FriendlyByteBuf buf) {
		// Group registry ids with same namespace.
		Map<String, List<Identifier>> regNamespaceGroups = registryMap.keySet().stream()
				.collect(Collectors.groupingBy(Identifier::getNamespace));

		buf.writeVarInt(regNamespaceGroups.size());

		regNamespaceGroups.forEach((regNamespace, regIds) -> {
			buf.writeUtf(optimizeNamespace(regNamespace));
			buf.writeVarInt(regIds.size());

			for (Identifier regId : regIds) {
				buf.writeUtf(regId.getPath());
				buf.writeByte(encodeRegistryAttributes(registryAttributes.getOrDefault(regId, EnumSet.noneOf(RegistryAttribute.class))));

				Object2IntMap<Identifier> idMap = registryMap.get(regId);

				// Sort object ids by its namespace. We use linked map here to keep the original namespace ordering.
				Map<String, List<Object2IntMap.Entry<Identifier>>> idNamespaceGroups = idMap.object2IntEntrySet().stream()
						.collect(Collectors.groupingBy(e -> e.getKey().getNamespace(), LinkedHashMap::new, Collectors.toCollection(ArrayList::new)));

				buf.writeVarInt(idNamespaceGroups.size());

				int lastBulkLastRawId = 0;

				for (Map.Entry<String, List<Object2IntMap.Entry<Identifier>>> idNamespaceEntry : idNamespaceGroups.entrySet()) {
					// Make sure the ids are sorted by its raw id.
					List<Object2IntMap.Entry<Identifier>> idPairs = idNamespaceEntry.getValue();
					idPairs.sort(Comparator.comparingInt(Object2IntMap.Entry::getIntValue));

					// Group consecutive raw ids together.
					List<List<Object2IntMap.Entry<Identifier>>> bulks = new ArrayList<>();

					Iterator<Object2IntMap.Entry<Identifier>> idPairIter = idPairs.iterator();
					List<Object2IntMap.Entry<Identifier>> currentBulk = new ArrayList<>();
					Object2IntMap.Entry<Identifier> currentPair = idPairIter.next();
					currentBulk.add(currentPair);

					while (idPairIter.hasNext()) {
						currentPair = idPairIter.next();

						if (currentBulk.get(currentBulk.size() - 1).getIntValue() + 1 != currentPair.getIntValue()) {
							bulks.add(currentBulk);
							currentBulk = new ArrayList<>();
						}

						currentBulk.add(currentPair);
					}

					bulks.add(currentBulk);

					buf.writeUtf(optimizeNamespace(idNamespaceEntry.getKey()));
					buf.writeVarInt(bulks.size());

					for (List<Object2IntMap.Entry<Identifier>> bulk : bulks) {
						int firstRawId = bulk.get(0).getIntValue();
						int bulkRawIdStartDiff = firstRawId - lastBulkLastRawId;

						buf.writeVarInt(bulkRawIdStartDiff);
						buf.writeVarInt(bulk.size());

						for (Object2IntMap.Entry<Identifier> idPair : bulk) {
							buf.writeUtf(idPair.getKey().getPath());

							lastBulkLastRawId = idPair.getIntValue();
						}
					}
				}
			}
		});
	}

	private static byte encodeRegistryAttributes(EnumSet<RegistryAttribute> attributes) {
		byte encoded = 0;

		// Only send the optional marker.
		if (attributes.contains(RegistryAttribute.OPTIONAL)) {
			encoded |= 0x1;
		}

		return encoded;
	}

	private static EnumSet<RegistryAttribute> decodeRegistryAttributes(byte encoded) {
		EnumSet<RegistryAttribute> attributes = EnumSet.noneOf(RegistryAttribute.class);

		if ((encoded & 0x1) != 0) {
			attributes.add(RegistryAttribute.OPTIONAL);
		}

		return attributes;
	}

	private static String optimizeNamespace(String namespace) {
		return namespace.equals(Identifier.DEFAULT_NAMESPACE) ? "" : namespace;
	}

	private static String unoptimizeNamespace(String namespace) {
		return namespace.isEmpty() ? Identifier.DEFAULT_NAMESPACE : namespace;
	}

	@Override
	public Type<RegistrySyncPayload> type() {
		return ID;
	}
}
