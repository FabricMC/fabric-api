package net.fabricmc.fabric.impl.holder.component;

import com.mojang.serialization.Codec;

import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.resources.ResourceKey;

import java.util.Map;

// TODO: Make public api and use for datagen
public record DataHolderComponentFile<T, C>(
		boolean replace,
		Map<ResourceKey<T>, C> components
) {
	public static <T, C> Codec<DataHolderComponentFile<T, C>> codec(ResourceKey<? extends Registry<T>> registryKey, DataComponentType<C> componentType) {
		return RecordCodecBuilder.create(instance -> instance.group(
				Codec.BOOL.optionalFieldOf("replace", false).forGetter(DataHolderComponentFile::replace),
				Codec.unboundedMap(
						ResourceKey.codec(registryKey),
						componentType.codecOrThrow()
				).fieldOf("components").forGetter(DataHolderComponentFile::components)
		).apply(instance, DataHolderComponentFile::new));
	}
}
