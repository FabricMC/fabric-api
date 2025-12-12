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

package net.fabricmc.fabric.impl.resource.client;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import org.jetbrains.annotations.Unmodifiable;
import org.slf4j.Logger;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtOps;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.repository.Pack;

import net.fabricmc.fabric.impl.resource.pack.FabricPack;
import net.fabricmc.fabric.impl.resource.pack.ModNioPackResources;
import net.fabricmc.fabric.impl.resource.pack.ModResourcePackCreator;
import net.fabricmc.loader.api.FabricLoader;

// Track built-in resource packs if they are enabled by default.
// - If there is NO value with matching resource pack id, add it to the enabled packs and the tracker file.
// - If there is a matching value and pack id, do not add it to the enabled packs and let
//   the options value decides if it is enabled or not.
// - If there is a value without matching pack id (e.g. because the mod is removed),
//   remove it from the tracker file so that it would be enabled again if added back later.
public final class DefaultResourcePackStorage {
	private static final Logger LOGGER = LogUtils.getLogger();

	private static final Path DATA_DIR = FabricLoader.getInstance().getGameDir().resolve("data");
	private static final Path TRACKER_FILE_PATH = DATA_DIR.resolve("fabric_default_resource_packs.json");
	private static final Path OLD_TRACKER_FILE_PATH = DATA_DIR.resolve("fabricDefaultResourcePacks.dat");

	private static final Codec<Set<String>> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Codec.STRING.listOf().fieldOf("values").forGetter(List::copyOf)
	).apply(instance, Set::copyOf));

	public static List<String> process(Collection<String> originalResourcePacks) {
		if (Files.notExists(DATA_DIR)) {
			try {
				Files.createDirectories(DATA_DIR);
			} catch (IOException e) {
				LOGGER.warn("[Fabric Resource Loader] Could not create data directory: {}", DATA_DIR.toAbsolutePath());
			}
		}

		var trackedPacks = new HashSet<>(read());

		var removedPacks = new HashSet<>(trackedPacks);
		var resourcePacks = new LinkedHashSet<>(originalResourcePacks);

		var profiles = new ArrayList<Pack>();
		ModResourcePackCreator.CLIENT_RESOURCE_PACK_PROVIDER.loadPacks(profiles::add);

		for (Pack profile : profiles) {
			// Hidden packs are always enabled.
			if (((FabricPack) profile).fabric$isHidden()) {
				continue;
			}

			try (PackResources pack = profile.open()) {
				if (pack instanceof ModNioPackResources builtinPack && builtinPack.getActivationType().isEnabledByDefault()) {
					if (trackedPacks.add(builtinPack.packId())) {
						resourcePacks.add(profile.getId());
					} else {
						removedPacks.remove(builtinPack.packId());
					}
				}
			}
		}

		trackedPacks.removeAll(removedPacks);
		write(trackedPacks);

		return new ArrayList<>(resourcePacks);
	}

	private static @Unmodifiable Set<String> read() {
		if (Files.exists(TRACKER_FILE_PATH)) {
			try (Reader fileReader = Files.newBufferedReader(TRACKER_FILE_PATH); var reader = new JsonReader(fileReader)) {
				return CODEC.parse(JsonOps.INSTANCE, JsonParser.parseReader(reader)).getOrThrow();
			} catch (Exception e) {
				LOGGER.warn("[Fabric Resource Loader] Could not read {}", TRACKER_FILE_PATH.toAbsolutePath(), e);
			}
		}

		if (Files.exists(OLD_TRACKER_FILE_PATH)) {
			try {
				CompoundTag data = NbtIo.readCompressed(OLD_TRACKER_FILE_PATH, NbtAccounter.unlimitedHeap());
				return CODEC.parse(NbtOps.INSTANCE, data)
						.result()
						.orElse(Set.of());
			} catch (Exception e) {
				LOGGER.warn("[Fabric Resource Loader] Could not read {}", OLD_TRACKER_FILE_PATH.toAbsolutePath(), e);
			}
		}

		return Set.of();
	}

	private static void write(Set<String> values) {
		try {
			Files.writeString(TRACKER_FILE_PATH, CODEC.encodeStart(JsonOps.INSTANCE, values).getOrThrow().toString());
		} catch (Exception e) {
			LOGGER.warn("[Fabric Resource Loader] Could not read {}", TRACKER_FILE_PATH.toAbsolutePath(), e);
		}
	}
}
