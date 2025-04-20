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

package net.fabricmc.fabric.mixin.datagen.client;

import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Stream;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import net.minecraft.block.Block;
import net.minecraft.client.data.ModelProvider;
import net.minecraft.client.item.ItemAsset;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.entry.RegistryEntry;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.impl.datagen.client.FabricItemAssetDefinitions;

@Mixin(ModelProvider.ItemAssets.class)
public class ModelProviderItemAssetsMixin implements FabricItemAssetDefinitions {
	@Unique
	private FabricDataOutput fabricDataOutput;
	@Unique
	private Set<Block> processedBlocks;

	@Override
	public void fabric_setProcessedBlocks(Set<Block> processedBlocks) {
		this.processedBlocks = processedBlocks;
	}

	@Override
	public void setFabricDataOutput(FabricDataOutput fabricDataOutput) {
		this.fabricDataOutput = fabricDataOutput;
	}

	@WrapOperation(method = "method_65470", at = @At(value = "INVOKE", target = "Ljava/util/Map;containsKey(Ljava/lang/Object;)Z", ordinal = 1, remap = false))
	private boolean filterItemsForProcessingMod(Map<Item, ItemAsset> map, Object o, Operation<Boolean> original) {
		BlockItem blockItem = (BlockItem) o;

		if (fabricDataOutput != null) {
			// Only generate the item model if the block state json was registered
			if (!processedBlocks.contains(blockItem.getBlock())) {
				return true;
			}

			if (!Registries.ITEM.getId(blockItem).getNamespace().equals(fabricDataOutput.getModId())) {
				// Skip over items that are not from the mod we are processing.
				return true;
			}
		}

		return original.call(map, o);
	}

	@Redirect(method = "resolveAndValidate", at = @At(value = "INVOKE", target = "Ljava/util/stream/Stream;filter(Ljava/util/function/Predicate;)Ljava/util/stream/Stream;", ordinal = 0, remap = false))
	private Stream<RegistryEntry.Reference<Item>> filterItemsForProcessingMod(Stream<RegistryEntry.Reference<Item>> instance, Predicate<RegistryEntry.Reference<Item>> predicate) {
		return instance.filter((item) -> {
			if (fabricDataOutput != null) {
				if (!fabricDataOutput.isStrictValidationEnabled()) {
					return false;
				}

				if (!item.registryKey().getValue().getNamespace().equals(fabricDataOutput.getModId())) {
					// Skip over items that are not from the mod we are processing.
					return false;
				}
			}

			return predicate.test(item);
		});
	}
}
