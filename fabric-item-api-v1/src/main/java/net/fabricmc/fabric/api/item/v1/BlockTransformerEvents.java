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

public final class BlockTransformerEvents {
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
		 * Modifies a {@link BlockTransformer}.
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
