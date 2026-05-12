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

import java.io.Reader;
import java.util.List;
import java.util.Map;

import com.google.gson.JsonElement;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.JsonOps;
import org.slf4j.Logger;

import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.util.StrictJsonParser;

import net.fabricmc.fabric.api.holder.component.v1.FabricDataComponentInitializer;

public class DataHolderComponentInitializer implements FabricDataComponentInitializer {
	private static final Logger LOGGER = LogUtils.getLogger();

	@Override
	public void run(Context context) {
		RegistryOps<JsonElement> ops = context.lookupProvider().createSerializationContext(JsonOps.INSTANCE);

		context.lookupProvider().listRegistryKeys().forEach(key -> parse(context, ops, key));
	}

	private <T> void parse(
			Context context,
			RegistryOps<JsonElement> ops,
			ResourceKey<? extends Registry<? extends T>> key1
	) {
		// TODO: less cursed way of doing this?
		@SuppressWarnings("unchecked") ResourceKey<? extends Registry<T>> key = (ResourceKey<? extends Registry<T>>) key1;

		HolderLookup.RegistryLookup<T> lookup = context.lookupProvider().lookupOrThrow(key);
		FileToIdConverter lister = FileToIdConverter.json(Registries.componentsDirPath(lookup.key()));

		for (Map.Entry<Identifier, List<Resource>> entry : lister.listMatchingResourceStacks(context.resourceManager()).entrySet()) {
			Identifier location = entry.getKey();
			Identifier id = lister.fileToId(location);

			lookup.get(ResourceKey.create(key, id)).ifPresent(holder -> {
				parse(context, ops, entry.getValue(), holder, location, id);
			});
		}
	}

	private <T> void parse(
			Context context,
			RegistryOps<JsonElement> ops,
			List<Resource> resources,
			Holder.Reference<T> holder,
			Identifier location,
			Identifier id
	) {
		for (Resource resource : resources) {
			try (Reader reader = resource.openAsReader()) {
				JsonElement element = StrictJsonParser.parse(reader);

				DataHolderComponentFile file = DataHolderComponentFile.CODEC.parse(ops, element).getOrThrow();

				DataComponentMap.Builder builder = context.builder(holder.key());

				if (file.replace()) builder.clear();
				builder.apply(file.components());
			} catch (Exception e) {
				LOGGER.error("Couldn't read component list {} from {} in data pack {}", id, location, resource.sourcePackId(), e);
			}
		}
	}
}
