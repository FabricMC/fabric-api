package net.fabricmc.fabric.impl.holder.component;

import com.google.gson.JsonElement;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;

import net.fabricmc.fabric.api.holder.component.FabricDataComponentInitializer;

import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
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
import java.util.Optional;

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

			BuiltInRegistries.DATA_COMPONENT_TYPE.getOptional(id).ifPresent(componentType -> {
				parse(context, ops, entry.getValue(), key, componentType, location, id);
			});
		}
	}

	private <T, C> void parse(
			Context context,
			RegistryOps<JsonElement> ops,
			List<Resource> resources,
			ResourceKey<? extends Registry<T>> registryKey,
			DataComponentType<C> componentType,
			Identifier location,
			Identifier id
	) {

		for (Resource resource : resources) {
			try (Reader reader = resource.openAsReader()) {
				JsonElement element = StrictJsonParser.parse(reader);
				DataHolderComponentFile<T, C> file = DataHolderComponentFile.codec(registryKey, componentType).parse(ops, element).getOrThrow();

				for (Map.Entry<ResourceKey<T>, C> entry : file.components().entrySet()) {
					var builder = context.builder(entry.getKey());
					builder.set(componentType, entry.getValue());
				}
			} catch (Exception e) {
				LOGGER.error("Couldn't read component list {} from {} in data pack {}", id, location, resource.sourcePackId(), e);
			}
		}
	}
}
