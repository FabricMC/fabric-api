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

package net.fabricmc.fabric.test.registry.sync.client;

import java.util.EnumSet;
import java.util.Map;
import java.util.Objects;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;

import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.registry.RegistryAttribute;
import net.fabricmc.fabric.impl.client.registry.sync.ClientRegistrySyncHandler;
import net.fabricmc.fabric.impl.registry.sync.RemapException;
import net.fabricmc.fabric.impl.registry.sync.packet.RegistryPacketHandler;

public class RegistrySyncClientTest implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
				dispatcher.register(CommandManager.literal("remote_remap_error_test").executes(context -> {
					Map<Identifier, Object2IntMap<Identifier>> registryData = Map.of(
							RegistryKeys.BLOCK.getValue(), createFakeRegistryEntries(),
							RegistryKeys.ITEM.getValue(), createFakeRegistryEntries()
					);
					Map<Identifier, EnumSet<RegistryAttribute>> attributes = Map.of(
							RegistryKeys.BLOCK.getValue(), EnumSet.noneOf(RegistryAttribute.class),
							RegistryKeys.ITEM.getValue(), EnumSet.noneOf(RegistryAttribute.class)
					);

					try {
						ClientRegistrySyncHandler.checkRemoteRemap(new RegistryPacketHandler.SyncedPacketData(registryData, attributes));
					} catch (RemapException e) {
						final ServerPlayerEntity player = context.getSource().getPlayer();

						if (player != null) {
							player.networkHandler.disconnect(Objects.requireNonNull(e.getText()));
						}

						return 1;
					}

					throw new IllegalStateException();
				})));
	}

	private static Object2IntMap<Identifier> createFakeRegistryEntries() {
		Object2IntMap<Identifier> map = new Object2IntOpenHashMap<>();

		for (int i = 0; i < 12; i++) {
			map.put(Identifier.of("mod_" + i, "entry"), 0);
		}

		return map;
	}
}
