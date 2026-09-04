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

package net.fabricmc.fabric.test.registry.sync;

import static net.minecraft.commands.Commands.literal;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import com.mojang.logging.LogUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.slf4j.Logger;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.RandomSource;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.impl.registry.sync.validate.RegistryCustomContentState;

public class RegistryEntryWorldValidationTestInit implements ModInitializer {
	private static final Logger LOGGER = LogUtils.getLogger();

	@Override
	public void onInitialize() {
		CommandRegistrationCallback.EVENT.register(((dispatcher, buildContext, selection) -> {
			dispatcher.register(literal("override_registry_entries").executes(ctx -> {
				Path path = RegistryCustomContentState.getPath(ctx.getSource().getServer());

				if (!Files.isDirectory(path.getParent())) {
					try {
						Files.createDirectories(path.getParent());
					} catch (IOException e) {
						LOGGER.error("Failed to create folders!", e);
					}
				}

				try {
					RegistryCustomContentState state = RegistryCustomContentState.construct(ctx.getSource().registryAccess());

					generateFakeEntries(state, Registries.BLOCK.identifier(), 50, 4000);
					generateFakeEntries(state, Registries.ITEM.identifier(), 55, 3000);
					generateFakeEntries(state, Registries.ENTITY_TYPE.identifier(), 5, 15);
					generateFakeEntries(state, Registries.BLOCK_ENTITY_TYPE.identifier(), 10, 15);
					generateFakeEntries(state, Registries.VILLAGER_PROFESSION.identifier(), 2, 1);
					generateFakeEntries(state, Registries.ATTRIBUTE.identifier(), 8, 3);

					RegistryCustomContentState.writeFile(path, state);

					ctx.getSource().sendSystemMessage(Component.literal("Created test registry entry file! Leave and entry this world again to test it!"));
				} catch (IOException e) {
					LOGGER.error("Failed to copy the reference registry entries file!", e);
					ctx.getSource().sendFailure(Component.literal("Failed to copy the reference registry entries file! See logs for more info"));
				}

				return 1;
			}));
		}));
	}

	private void generateFakeEntries(RegistryCustomContentState state, Identifier registry, int amountNamespaces, int amountEntries) {
		RandomSource random = RandomSource.createThreadLocalInstance(registry.hashCode());
		List<Identifier> identifiers = state.entries().computeIfAbsent(registry, _ -> new ArrayList<>());

		for (int in = 0; in < amountNamespaces; in++) {
			String namespace = "fabric_" + random.nextInt();

			for (int ie = 0; ie < amountEntries; ie++) {
				identifiers.add(Identifier.fromNamespaceAndPath(namespace, RandomStringUtils.insecure().next(16, "abcdefghijklmnopqrstuvwxyz0123456789_")));
			}
		}
	}
}
