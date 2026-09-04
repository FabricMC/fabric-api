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

package net.fabricmc.fabric.impl.registry.sync.validate;

import java.io.IOException;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.mojang.logging.LogUtils;
import org.slf4j.Logger;

import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraft.world.level.storage.LevelStorageSource;

import net.fabricmc.fabric.api.event.registry.RegistryAttribute;
import net.fabricmc.fabric.api.event.registry.RegistryAttributeHolder;

public record RegistryCustomContentState(Map<Identifier, List<Identifier>> entries, Status status) {
	private static final Logger LOGGER = LogUtils.getLogger();

	private static final int MAX_ENTRIES_PER_NAMESPACE = 50;

	public static final RegistryCustomContentState EMPTY = new RegistryCustomContentState(Map.of(), Status.VALID);

	public static RegistryCustomContentState construct(RegistryAccess registryAccess) {
		var map = new HashMap<Identifier, List<Identifier>>();

		registryAccess.listRegistries().filter(registryLookup -> RegistryAttributeHolder.get(registryLookup.key()).hasAttribute(RegistryAttribute.SAVE_DATA_VALIDATED))
				.forEach(registry -> {
					var list = new ArrayList<Identifier>();

					registry.listElementIds().map(ResourceKey::identifier).filter(id -> !id.getNamespace().equals(Identifier.DEFAULT_NAMESPACE)).forEach(list::add);

					if (!list.isEmpty()) {
						map.put(registry.key().identifier(), list);
					}
				});

		return map.isEmpty() ? EMPTY : new RegistryCustomContentState(map, Status.VALID);
	}

	private static Path getPath(LevelStorageSource.LevelStorageAccess worldAccess, String suffix) {
		return getPathRelative(worldAccess.getLevelPath(LevelResource.DATA), suffix);
	}

	public static Path getPath(MinecraftServer server) {
		return getPathRelative(server.getWorldPath(LevelResource.DATA), "");
	}

	private static Path getPathRelative(Path path, String suffix) {
		return path.resolve("fabric").resolve("registry_entries.dat" + suffix);
	}

	public static void writeIfNeeded(LevelStorageSource.LevelStorageAccess storageSource, RegistryAccess registryAccess) {
		try {
			Path tempPath = getPath(storageSource, "_tmp");
			Path path = getPath(storageSource, "");

			RegistryCustomContentState state = RegistryCustomContentState.construct(registryAccess);

			if (!Files.exists(path) && state.isEmpty()) {
				return;
			}

			writeFile(tempPath, state);

			try {
				Files.move(tempPath, path, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
			} catch (AtomicMoveNotSupportedException _) {
				// Fallback
				Files.move(tempPath, path, StandardCopyOption.REPLACE_EXISTING);
			}
		} catch (Throwable e) {
			LOGGER.error("Failed to write the registry entries file!", e);
		}
	}

	public boolean isEmpty() {
		return this.entries.isEmpty() && this.status == Status.VALID;
	}

	public static void writeFile(Path path, RegistryCustomContentState state) throws IOException {
		if (!Files.isDirectory(path.getParent())) {
			Files.createDirectories(path.getParent());
		}

		NbtIo.writeCompressed(state.toNbt(), path);
	}

	public static RegistryCustomContentState readFile(LevelStorageSource.LevelStorageAccess access) {
		return readFile(RegistryCustomContentState.getPath(access, ""));
	}

	public static RegistryCustomContentState readFile(Path path) {
		if (!Files.exists(path)) {
			return EMPTY;
		}

		try {
			return RegistryCustomContentState.fromNbt(NbtIo.readCompressed(path, NbtAccounter.unlimitedHeap()));
		} catch (Throwable e) {
			LOGGER.error("Failed to read the registry entries file!", e);
		}

		return new RegistryCustomContentState(Map.of(), Status.INVALID_FILE);
	}

	private static RegistryCustomContentState fromNbt(CompoundTag tag) {
		Status status = Status.VALID;

		if (tag.getIntOr("format_version", -1) != 0) {
			status = Status.UNSUPPORTED_VERSION;
		}

		var map = new HashMap<Identifier, List<Identifier>>();

		Optional<CompoundTag> registries = tag.getCompound("registries");

		if (registries.isPresent()) {
			for (String key : registries.get().keySet()) {
				Optional<ListTag> entries = registries.get().getList(key);

				Identifier registryId = Identifier.tryParse(key);

				if (registryId != null && entries.isPresent()) {
					var idList = new ArrayList<Identifier>();

					for (Tag entry : entries.get()) {
						if (entry instanceof StringTag(String value)) {
							idList.add(Identifier.parse(value));
						} else if (status == Status.VALID) {
							status = Status.INVALID_FILE;
						}
					}

					map.put(registryId, idList);
				} else if (status != Status.UNSUPPORTED_VERSION) {
					status = Status.INVALID_FILE;
				}
			}
		} else if (status != Status.UNSUPPORTED_VERSION) {
			status = Status.INVALID_FILE;
		}

		return new RegistryCustomContentState(map, status);
	}

	private CompoundTag toNbt() {
		var tag = new CompoundTag();
		tag.putInt("format_version", 0);

		var registries = new CompoundTag();

		for (Map.Entry<Identifier, List<Identifier>> entry : this.entries.entrySet()) {
			var list = new ListTag();

			for (Identifier id : entry.getValue()) {
				list.add(StringTag.valueOf(id.toString()));
			}

			registries.put(entry.getKey().toShortString(), list);
		}

		tag.put("registries", registries);
		return tag;
	}

	public Missing validate(RegistryAccess registryAccess) {
		var map = new HashMap<Identifier, List<Identifier>>();
		var registries = new ArrayList<Identifier>();

		for (Map.Entry<Identifier, List<Identifier>> entry : this.entries.entrySet()) {
			Optional<Registry<Object>> registry = registryAccess.lookup(ResourceKey.createRegistryKey(entry.getKey()));

			if (registry.isPresent()) {
				for (Identifier identifier : entry.getValue()) {
					if (!registry.get().containsKey(identifier)) {
						map.computeIfAbsent(entry.getKey(), _ -> new ArrayList<>()).add(identifier);
					}
				}
			} else {
				registries.add(entry.getKey());
			}
		}

		return new Missing(map, registries, this.status);
	}

	public record Missing(Map<Identifier, List<Identifier>> entries, List<Identifier> registries, Status status) {
		public static final Missing NONE = new Missing(Map.of(), List.of(), Status.VALID);

		public Details asDetails() {
			var list = new ArrayList<Details.Section>();

			if (status == Status.INVALID_FILE) {
				list.add(new Details.Section(
						Component.translatable("fabric-registry-sync-v0.missing-entries.invalid_file").withColor(TextColor.RED),
						List.of()
				));
			} else if (status == Status.UNSUPPORTED_VERSION) {
				list.add(new Details.Section(
						Component.translatable("fabric-registry-sync-v0.missing-entries.unsupported_version").withColor(TextColor.GOLD),
						List.of()
				));
			}

			for (Map.Entry<Identifier, List<Identifier>> entry : this.entries.entrySet()) {
				var m = new HashMap<String, List<String>>();
				var body = new ArrayList<Component>();

				for (Identifier id : entry.getValue()) {
					m.computeIfAbsent(id.getNamespace(), _ -> new ArrayList<>()).add(id.getPath());
				}

				for (Map.Entry<String, List<String>> id : m.entrySet()) {
					var group = new ArrayList<Component>();
					group.add(Component.translatable("fabric-registry-sync-v0.missing-entries.details.missing_for_namespace", id.getValue().size(), id.getKey()));

					int i = 0;
					int finalEntry = Math.min(MAX_ENTRIES_PER_NAMESPACE, id.getValue().size());

					for (; i < finalEntry; i++) {
						group.add(Component.translatable("fabric-registry-sync-v0.missing-entries.details.missing_for_namespace.entry", id.getValue().get(i)).withColor(TextColor.GRAY));
					}

					if (i < id.getValue().size()) {
						group.add(Component.translatable("fabric-registry-sync-v0.missing-entries.details.missing_for_namespace.too_many", id.getValue().size() - i).withStyle(Style.EMPTY.withItalic(true).withColor(TextColor.GRAY)));
					}

					body.add(ComponentUtils.formatList(group, CommonComponents.NEW_LINE));
				}

				list.add(new Details.Section(
						Component.translatable("fabric-registry-sync-v0.missing-entries.details.missing_registry_entry", entry.getKey().toString(), entry.getValue().size()).withColor(TextColor.YELLOW),
						body
				));
			}

			if (!this.registries.isEmpty()) {
				var body = new ArrayList<Component>();

				for (Identifier registry : this.registries) {
					body.add(Component.translatable("fabric-registry-sync-v0.missing-entries.details.missing_registry.entry", registry.toString()).withColor(TextColor.GRAY));
				}

				list.add(new Details.Section(
						Component.translatable("fabric-registry-sync-v0.missing-entries.details.missing_registry", this.registries.size()).withColor(TextColor.YELLOW),
						List.of(ComponentUtils.formatList(body, CommonComponents.NEW_LINE))
				));
			}

			return new Details(list);
		}

		public boolean isEmpty() {
			return this.entries.isEmpty() && this.registries.isEmpty() && this.status == Status.VALID;
		}
	}

	public enum Status {
		VALID,
		INVALID_FILE,
		UNSUPPORTED_VERSION,
	}
}
