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

package net.fabricmc.fabric.api.item.v1;

import java.util.List;

import net.minecraft.core.component.BlockTransformer;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;

/**
 * Events relating to block transformers, allowing for advanced modification of block transformers as they're loaded.
 *
 * <p>For adding new transformations to the vanilla transformers of axes, shovels, and hoes, see {@link BlockTransformerHelper}
 * or the various {@linkplain net.minecraft.tags.BlockTags block tags} utilized by those vanilla transformers.
 */
public final class BlockTransformerEvents {
	/**
	 * An event that allows a {@link BlockTransformer} to be modified without needing to fully override it.
	 *
	 * <p>This should only be used to modify the behavior of <em>external</em> block transformers, where 'external' means
	 * either vanilla, from a data pack, or from another mod.
	 *
	 * <p>For instance, a mod might add a transformer for one of its tools, and you may wish to add a new transformation
	 * to the transformer for your own blocks.
	 *
	 * <p>For your own block transformers, you should simply define them in your mod's data pack. See the
	 * <a href="https://minecraft.wiki/w/Block_transformer_definition">Block Transformer Definition page</a> on the
	 * Minecraft Wiki for more information, and consider generating the JSON files with data generation.
	 *
	 * <p>Note: If you wish to add transformations to axes, shovels, or hoes, consider using methods in
	 * {@link BlockTransformerHelper} or extending the {@linkplain net.minecraft.tags.BlockTags relevant block tags}
	 * through your mod's data pack instead.
	 */
	public static final Event<BlockTransformerEvents.Modify> MODIFY = EventFactory.createArrayBacked(
			BlockTransformerEvents.Modify.class,
			callbacks -> (key, transforms, source, registries) -> {
				for (BlockTransformerEvents.Modify callback : callbacks) {
					callback.modify(key, transforms, source, registries);
				}
			}
	);

	@FunctionalInterface
	public interface Modify {
		/**
		 * Allows for modification of a {@link BlockTransformer}.
		 *
		 * <p>Modification is achieved by altering the list of {@link BlockTransformer.BlockTransformData transform data}.
		 *
		 * @param key The ID of the block transformer
		 * @param transforms The list of transform data
		 * @param source The source of the block transformer
		 * @param registryInfoLookup Lookup interface used to access registry information
		 */
		void modify(
				ResourceKey<BlockTransformer> key,
				List<BlockTransformer.BlockTransformData> transforms,
				ResourceSource source,
				RegistryOps.RegistryInfoLookup registryInfoLookup
		);
	}
}
