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

package net.fabricmc.fabric.mixin.datagen;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;
import java.util.function.Supplier;

import com.google.gson.JsonElement;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.block.Block;
import net.minecraft.data.DataOutput;
import net.minecraft.data.DataWriter;
import net.minecraft.data.client.BlockStateModelGenerator;
import net.minecraft.data.client.BlockStateSupplier;
import net.minecraft.data.client.ItemModelGenerator;
import net.minecraft.data.client.ModelProvider;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.Identifier;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricModelProvider;

@Mixin(ModelProvider.class)
public class ModelProviderMixin {
	@Unique
	private FabricDataOutput fabricDataOutput;

	@Unique
	private static final ThreadLocal<FabricDataOutput> fabricDataOutputThreadLocal = new ThreadLocal<>();

	@Unique
	private static final ThreadLocal<Map<Block, BlockStateSupplier>> blockStateMapThreadLocal = new ThreadLocal<>();

	@Inject(method = "<init>", at = @At("RETURN"))
	public void init(DataOutput output, CallbackInfo ci) {
		if (output instanceof FabricDataOutput fabricDataOutput) {
			this.fabricDataOutput = fabricDataOutput;
		}
	}

	@WrapOperation(method = "run", at = @At(value = "INVOKE", target = "Lnet/minecraft/data/client/BlockStateModelGenerator;register()V"))
	private void registerBlockStateModels(BlockStateModelGenerator instance, Operation<Void> original) {
		if (((Object) this) instanceof FabricModelProvider fabricModelProvider) {
			fabricModelProvider.generateBlockStateModels(instance);
		} else {
			// Fallback to the vanilla registration when not a fabric provider
			original.call(instance);
		}
	}

	@WrapOperation(method = "run", at = @At(value = "INVOKE", target = "Lnet/minecraft/data/client/ItemModelGenerator;register()V"))
	private void registerItemModels(ItemModelGenerator instance, Operation<Void> original) {
		if (((Object) this) instanceof FabricModelProvider fabricModelProvider) {
			fabricModelProvider.generateItemModels(instance);
		} else {
			// Fallback to the vanilla registration when not a fabric provider
			original.call(instance);
		}
	}

	@Inject(method = "run", at = @At(value = "INVOKE_ASSIGN", target = "com/google/common/collect/Maps.newHashMap()Ljava/util/HashMap;", ordinal = 0, remap = false))
	private void runHead(DataWriter writer, CallbackInfoReturnable<CompletableFuture<?>> cir, @Local Map<Block, BlockStateSupplier> map) {
		fabricDataOutputThreadLocal.set(fabricDataOutput);
		blockStateMapThreadLocal.set(map);
	}

	@Inject(method = "run", at = @At("TAIL"))
	private void runTail(DataWriter writer, CallbackInfoReturnable<CompletableFuture<?>> cir) {
		fabricDataOutputThreadLocal.remove();
		blockStateMapThreadLocal.remove();
	}

	// Target the first .filter() call, to filter out blocks that are not from the mod we are processing.
	@ModifyArg(method = "run", at = @At(value = "INVOKE", target = "Ljava/util/stream/Stream;filter(Ljava/util/function/Predicate;)Ljava/util/stream/Stream;", ordinal = 0, remap = false))
	private Predicate<Map.Entry<RegistryKey<Block>, Block>> filterBlocksForProcessingMod(Predicate<Map.Entry<RegistryKey<Block>, Block>> original) {
		if (fabricDataOutput != null) {
			return original
					.and(e -> fabricDataOutput.isStrictValidationEnabled())
					// Skip over blocks that are not from the mod we are processing.
					.and(e -> e.getKey().getValue().getNamespace().equals(fabricDataOutput.getModId()));
		}

		return original;
	}

	@Inject(method = "method_25741", at = @At(value = "INVOKE", target = "Lnet/minecraft/data/client/ModelIds;getItemModelId(Lnet/minecraft/item/Item;)Lnet/minecraft/util/Identifier;"), cancellable = true)
	private static void filterItemsForProcessingMod(Set<Item> set, Map<Identifier, Supplier<JsonElement>> map, Block block, CallbackInfo ci, @Local Item item) {
		FabricDataOutput dataOutput = fabricDataOutputThreadLocal.get();

		if (dataOutput != null) {
			// Only generate the item model if the block state json was registered
			if (!blockStateMapThreadLocal.get().containsKey(block)) {
				ci.cancel();
				return;
			}

			if (!Registries.ITEM.getId(item).getNamespace().equals(dataOutput.getModId())) {
				// Skip over any items from other mods.
				ci.cancel();
			}
		}
	}
}
