package net.fabricmc.fabric.impl.holder.component;

import com.google.gson.JsonElement;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.JsonOps;

import net.fabricmc.fabric.api.holder.component.FabricDataComponentInitializer;

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

import org.slf4j.Logger;

import java.io.Reader;
import java.util.List;
import java.util.Map;

public class DataHolderComponentInitializer implements FabricDataComponentInitializer {
	private static final Logger LOGGER = LogUtils.getLogger();

	@Override
	public void run(Context context) {
		context.lookupProvider().listRegistryKeys().forEach(key -> parse(context, key));
	}

	// TODO: Less jank way of doing this? Not sure if its possible
	@SuppressWarnings("unchecked")
	private static <T> ResourceKey<T> createResourceKey(ResourceKey<? extends Registry<? extends T>> registry, Identifier path) {
		return ResourceKey.create((ResourceKey<? extends Registry<T>>) registry, path);
	}

	private <T> void parse(
			Context context,
			ResourceKey<? extends Registry<? extends T>> key
	) {

		HolderLookup.RegistryLookup<T> lookup = context.lookupProvider().lookupOrThrow(key);
		FileToIdConverter lister = FileToIdConverter.json(Registries.componentsDirPath(lookup.key()));
		RegistryOps<JsonElement> ops = context.lookupProvider().createSerializationContext(JsonOps.INSTANCE);

		for (Map.Entry<Identifier, List<Resource>> entry : lister.listMatchingResourceStacks(context.resourceManager()).entrySet()) {
			Identifier location = entry.getKey();
			Identifier id = lister.fileToId(location);

			lookup.get(createResourceKey(key, id)).ifPresent(holder -> {
				DataComponentMap.Builder builder = context.builder(holder.key());

				for (Resource resource : entry.getValue()) {
					try (Reader reader = resource.openAsReader()) {
						JsonElement element = StrictJsonParser.parse(reader);
						DataComponentMap map = DataComponentMap.CODEC.parse(ops, element).getOrThrow();
						builder.addAll(map);
					} catch (Exception e) {
						LOGGER.error("Couldn't read component list {} from {} in data pack {}", id, location, resource.sourcePackId(), e);
					}
				}
			});
		}
	}
}
