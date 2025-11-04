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

package net.fabricmc.fabric.api.datagen.v1.provider;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

import org.jspecify.annotations.Nullable;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.tags.TagAppender;
import net.minecraft.data.tags.TagsProvider;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagBuilder;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.material.Fluid;

import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;

/**
 * Implement this class (or one of the inner classes) to generate a tag list.
 *
 * <p>Register your implementation using {@link FabricDataGenerator.Pack#addProvider} in a {@link net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint}.
 *
 * <p>When generating tags for modded dynamic registry entries (such as biomes), either the entry
 * must be added to the registry using {@link net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint#buildRegistry(RegistrySetBuilder)}
 * or {@link TagBuilder#addOptionalElement(Identifier)} must be used. Otherwise, the data generator cannot
 * find the entry and crashes.
 *
 * <p>Commonly used implementations of this class are provided:
 *
 * @see BlockTagProvider
 * @see ItemTagProvider
 * @see FluidTagProvider
 * @see EntityTypeTagProvider
 */
public abstract class FabricTagProvider<T> extends TagsProvider<T> {
	private final FabricDataOutput output;
	private final Map<Identifier, AliasGroupBuilder> aliasGroupBuilders = new HashMap<>();

	/**
	 * Constructs a new {@link FabricTagProvider} with the default computed path.
	 *
	 * <p>Common implementations of this class are provided.
	 *
	 * @param output        the {@link FabricDataOutput} instance
	 * @param registriesFuture      the backing registry for the tag type
	 */
	public FabricTagProvider(FabricDataOutput output, ResourceKey<? extends Registry<T>> registryKey, CompletableFuture<HolderLookup.Provider> registriesFuture) {
		super(output, registryKey, registriesFuture);
		this.output = output;
	}

	/**
	 * Implement this method and then use {@link FabricTagProvider#builder} to get and register new tag builders.
	 */
	protected abstract void addTags(HolderLookup.Provider wrapperLookup);

	protected TagAppender<ResourceKey<T>, T> builder(TagKey<T> tag) {
		TagBuilder tagBuilder = this.getOrCreateRawBuilder(tag);
		return TagAppender.forBuilder(tagBuilder);
	}

	/**
	 * Gets an {@link AliasGroupBuilder} with the given ID.
	 *
	 * @param groupId the group ID
	 * @return the alias group builder
	 */
	protected AliasGroupBuilder aliasGroup(Identifier groupId) {
		return aliasGroupBuilders.computeIfAbsent(groupId, key -> new AliasGroupBuilder());
	}

	/**
	 * Gets an {@link AliasGroupBuilder} with the given ID.
	 *
	 * @param group the group name
	 * @return the alias group builder
	 */
	protected AliasGroupBuilder aliasGroup(String group) {
		Identifier groupId = Identifier.fromNamespaceAndPath(output.getModId(), group);
		return aliasGroupBuilders.computeIfAbsent(groupId, key -> new AliasGroupBuilder());
	}

	/**
	 * {@return a read-only map of alias group builders by the alias group ID}.
	 */
	public Map<Identifier, AliasGroupBuilder> getAliasGroupBuilders() {
		return Collections.unmodifiableMap(aliasGroupBuilders);
	}

	/**
	 * Parent class for tag providers that support adding registered values directly.
	 *
	 * @apiNote This class should not be subclassed directly. Either use a subclass provided by
	 * this API, or use the regular {@link FabricTagProvider}. (Ability to add registered values
	 * directly should be considered as deprecated.)
	 */
	public abstract static class FabricValueLookupTagProvider<T> extends FabricTagProvider<T> {
		private final Function<T, ResourceKey<T>> valueToKey;

		protected FabricValueLookupTagProvider(FabricDataOutput output, ResourceKey<? extends Registry<T>> registryKey, CompletableFuture<HolderLookup.Provider> registriesFuture, Function<T, ResourceKey<T>> valueToKey) {
			super(output, registryKey, registriesFuture);
			this.valueToKey = valueToKey;
		}

		protected TagAppender<T, T> valueLookupBuilder(TagKey<T> tag) {
			TagBuilder tagBuilder = this.getOrCreateRawBuilder(tag);
			return TagAppender.<T>forBuilder(tagBuilder).map(this.valueToKey);
		}
	}

	/**
	 * Extend this class to create {@link Block} tags in the "/block" tag directory.
	 */
	public abstract static class BlockTagProvider extends FabricValueLookupTagProvider<Block> {
		public BlockTagProvider(FabricDataOutput output, CompletableFuture<HolderLookup.Provider> registriesFuture) {
			super(output, Registries.BLOCK, registriesFuture, block -> block.builtInRegistryHolder().key());
		}
	}

	/**
	 * Extend this class to create {@link BlockEntityType} tags in the "/block_entity_type" tag directory.
	 */
	public abstract static class BlockEntityTypeTagProvider extends FabricValueLookupTagProvider<BlockEntityType<?>> {
		public BlockEntityTypeTagProvider(FabricDataOutput output, CompletableFuture<HolderLookup.Provider> registriesFuture) {
			super(output, Registries.BLOCK_ENTITY_TYPE, registriesFuture, type -> type.builtInRegistryHolder().key());
		}
	}

	/**
	 * Extend this class to create {@link Item} tags in the "/item" tag directory.
	 */
	public abstract static class ItemTagProvider extends FabricValueLookupTagProvider<Item> {
		@Nullable
		private final Function<TagKey<Block>, TagBuilder> blockTagBuilderProvider;

		/**
		 * Construct an {@link ItemTagProvider} tag provider <b>with</b> an associated {@link BlockTagProvider} tag provider.
		 *
		 * @param output The {@link FabricDataOutput} instance
		 */
		public ItemTagProvider(FabricDataOutput output, CompletableFuture<HolderLookup.Provider> registriesFuture, FabricTagProvider.@Nullable BlockTagProvider blockTagProvider) {
			super(output, Registries.ITEM, registriesFuture, item -> item.builtInRegistryHolder().key());

			this.blockTagBuilderProvider = blockTagProvider == null ? null : blockTagProvider::getOrCreateRawBuilder;
		}

		/**
		 * Construct an {@link ItemTagProvider} tag provider <b>without</b> an associated {@link BlockTagProvider} tag provider.
		 *
		 * @param output The {@link FabricDataOutput} instance
		 */
		public ItemTagProvider(FabricDataOutput output, CompletableFuture<HolderLookup.Provider> registriesFuture) {
			this(output, registriesFuture, null);
		}

		/**
		 * Copy the entries from a tag with the {@link Block} type into this item tag.
		 *
		 * <p>The {@link ItemTagProvider} tag provider must be constructed with an associated {@link BlockTagProvider} tag provider to use this method.
		 *
		 * @param blockTag The block tag to copy from.
		 * @param itemTag  The item tag to copy to.
		 */
		public void copy(TagKey<Block> blockTag, TagKey<Item> itemTag) {
			TagBuilder blockTagBuilder = Objects.requireNonNull(this.blockTagBuilderProvider, "Pass Block tag provider via constructor to use copy").apply(blockTag);
			TagBuilder itemTagBuilder = this.getOrCreateRawBuilder(itemTag);
			blockTagBuilder.build().forEach(itemTagBuilder::add);
		}
	}

	/**
	 * Extend this class to create {@link Fluid} tags in the "/fluid" tag directory.
	 */
	public abstract static class FluidTagProvider extends FabricValueLookupTagProvider<Fluid> {
		public FluidTagProvider(FabricDataOutput output, CompletableFuture<HolderLookup.Provider> registriesFuture) {
			super(output, Registries.FLUID, registriesFuture, fluid -> fluid.builtInRegistryHolder().key());
		}
	}

	/**
	 * Extend this class to create {@link EntityType} tags in the "/entity_type" tag directory.
	 */
	public abstract static class EntityTypeTagProvider extends FabricValueLookupTagProvider<EntityType<?>> {
		public EntityTypeTagProvider(FabricDataOutput output, CompletableFuture<HolderLookup.Provider> registriesFuture) {
			super(output, Registries.ENTITY_TYPE, registriesFuture, type -> type.builtInRegistryHolder().key());
		}
	}

	/**
	 * A builder for tag alias groups.
	 */
	public final class AliasGroupBuilder {
		private final List<TagKey<T>> tags = new ArrayList<>();

		private AliasGroupBuilder() {
		}

		/**
		 * {@return a read-only list of the tags in this alias group}.
		 */
		public List<TagKey<T>> getTags() {
			return Collections.unmodifiableList(tags);
		}

		public AliasGroupBuilder add(TagKey<T> tag) {
			if (tag.registry() != registryKey) {
				throw new IllegalArgumentException("Tag " + tag + " isn't from the registry " + registryKey);
			}

			this.tags.add(tag);
			return this;
		}

		@SafeVarargs
		public final AliasGroupBuilder add(TagKey<T>... tags) {
			for (TagKey<T> tag : tags) {
				add(tag);
			}

			return this;
		}

		public AliasGroupBuilder add(Identifier tag) {
			this.tags.add(TagKey.create(registryKey, tag));
			return this;
		}

		public AliasGroupBuilder add(Identifier... tags) {
			for (Identifier tag : tags) {
				this.tags.add(TagKey.create(registryKey, tag));
			}

			return this;
		}
	}
}
