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

package net.fabricmc.fabric.impl.modprotocol;

import java.util.Optional;
import java.util.function.BiConsumer;

import net.minecraft.resources.Identifier;

import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.metadata.CustomValue;
import net.fabricmc.loader.api.metadata.ModMetadata;
import net.fabricmc.loader.impl.util.version.VersionPredicateParser;

public class ModProtocolLocator {
	public static void provide(BiConsumer<ModContainer, ModProtocolImpl> consumer) {
		for (ModContainer mod : FabricLoader.getInstance().getAllMods()) {
			create(mod, consumer);
		}
	}

	private static void create(ModContainer container, BiConsumer<ModContainer, ModProtocolImpl> consumer) {
		ModMetadata meta = container.getMetadata();
		CustomValue definition = meta.getCustomValue("fabric:mod_protocol_v1");

		if (definition == null) {
			return;
		}

		if (definition.getType() == CustomValue.CvType.ARRAY) {
			for (CustomValue entry : definition.getAsArray()) {
				consumer.accept(container, decodeFullDefinition(entry, meta, true));
			}
		} else if (definition.getType() == CustomValue.CvType.STRING) {
			Optional<String> requirement = Optional.of(definition.getAsString());
			consumer.accept(container, new ModProtocolImpl(Identifier.fromNamespaceAndPath("mod", meta.getId()), meta.getName(), meta.getVersion().getFriendlyString(), requirement, requirement));
		} else {
			consumer.accept(container, decodeFullDefinition(definition, meta, false));
		}
	}

	public static ModProtocolImpl decodeFullDefinition(CustomValue entry, ModMetadata meta, boolean requireFullData) {
		if (entry.getType() != CustomValue.CvType.OBJECT) {
			throw new RuntimeException("Mod Protocol entry provided by '" + meta.getId() + "' is not valid!");
		}

		CustomValue.CvObject object = entry.getAsObject();
		Identifier id;
		String name;
		String version;
		Optional<String> requiredClient = Optional.empty();
		Optional<String> requiredServer = Optional.empty();

		CustomValue idField = object.get("id");
		CustomValue nameField = object.get("name");
		CustomValue versionField = object.get("version");
		CustomValue requiredField = object.get("require");
		CustomValue requiredClientField = object.get("require_client");
		CustomValue requiredServerField = object.get("require_server");

		if (!requireFullData && idField == null) {
			id = Identifier.fromNamespaceAndPath("mod", meta.getId());
		} else if (idField != null && idField.getType() == CustomValue.CvType.STRING) {
			id = Identifier.parse(idField.getAsString());
		} else {
			throw new RuntimeException("Mod Protocol entry provided by '" + meta.getId() + "' is not valid! The 'id' field is " + (idField == null ? "missing!" : "not a string!"));
		}

		if (!requireFullData && nameField == null) {
			name = meta.getName();
		} else if (nameField != null && nameField.getType() == CustomValue.CvType.STRING) {
			name = nameField.getAsString();
		} else {
			throw new RuntimeException("Mod Protocol entry provided by '" + meta.getId() + "' is not valid! The 'name' field is " + (nameField == null ? "missing!" : "not a string!"));
		}

		if (!requireFullData && versionField == null) {
			version = meta.getVersion().getFriendlyString();
		} else if (versionField != null && versionField.getType() == CustomValue.CvType.STRING) {
			version = versionField.getAsString();
		} else {
			throw new RuntimeException("Mod Protocol entry provided by '" + meta.getId() + "' is not valid! The 'version' field is " + (versionField == null ? "missing!" : "not a string!"));
		}

		if (requiredField != null && requiredField.getType() == CustomValue.CvType.STRING) {
			Optional<String> value = Optional.of(requiredField.getAsString());

			try {
				VersionPredicateParser.parse(value.get());
			} catch (Throwable e) {
				throw new RuntimeException("Mod Protocol entry provided by '" + meta.getId() + "' is not valid!", e);
			}

			requiredClient = value;
			requiredServer = value;
		} else if (requiredField != null) {
			throw new RuntimeException("Mod Protocol entry provided by '" + meta.getId() + "' is not valid! The 'required' field is not a string!");
		}

		if (requiredClientField != null && requiredClientField.getType() == CustomValue.CvType.STRING) {
			requiredClient = Optional.of(requiredClientField.getAsString());

			try {
				VersionPredicateParser.parse(requiredClient.get());
			} catch (Throwable e) {
				throw new RuntimeException("Mod Protocol entry provided by '" + meta.getId() + "' is not valid!", e);
			}
		} else if (requiredClientField != null) {
			throw new RuntimeException("Mod Protocol entry provided by '" + meta.getId() + "' is not valid! The 'required_client' field is not a string!");
		}

		if (requiredServerField != null && requiredServerField.getType() == CustomValue.CvType.STRING) {
			requiredServer = Optional.of(requiredServerField.getAsString());

			try {
				VersionPredicateParser.parse(requiredServer.get());
			} catch (Throwable e) {
				throw new RuntimeException("Mod Protocol entry provided by '" + meta.getId() + "' is not valid!", e);
			}
		} else if (requiredServerField != null) {
			throw new RuntimeException("Mod Protocol entry provided by '" + meta.getId() + "' is not valid! The 'required_server' field is not a string!");
		}

		return new ModProtocolImpl(id, name, version, requiredClient, requiredServer);
	}
}
