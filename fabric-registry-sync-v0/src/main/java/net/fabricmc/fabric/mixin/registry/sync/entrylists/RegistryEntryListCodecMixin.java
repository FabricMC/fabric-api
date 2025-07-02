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

package net.fabricmc.fabric.mixin.registry.sync.entrylists;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.entry.RegistryEntryList;
import net.minecraft.registry.entry.RegistryEntryListCodec;

import net.fabricmc.fabric.api.event.registry.entrylists.CustomRegistryEntryList;
import net.fabricmc.fabric.impl.registry.sync.entrylists.CustomRegistryEntryListSerializerImpl;

@SuppressWarnings("unchecked")
@Mixin(RegistryEntryListCodec.class)
class RegistryEntryListCodecMixin {
	@Unique
	private Codec<CustomRegistryEntryList<?>> fabric$codec;

	@Inject(
			method = "<init>",
			at = @At("TAIL")
	)
	private <T> void bindFabricCodec(RegistryKey<? extends Registry<T>> registry, Codec<RegistryEntry<T>> entryCodec, boolean alwaysSerializeAsList, CallbackInfo ci) {
		fabric$codec = CustomRegistryEntryListSerializerImpl.SERIALIZER_CODEC.dispatch(
				"fabric:type",
				CustomRegistryEntryList::getSerializer,
				serializer -> serializer.createCodec(registry, entryCodec, alwaysSerializeAsList)
		);
	}

	@Inject(
			method = "encode(Lnet/minecraft/registry/entry/RegistryEntryList;Lcom/mojang/serialization/DynamicOps;Ljava/lang/Object;)Lcom/mojang/serialization/DataResult;",
			at = @At(
					value = "FIELD",
					target = "Lnet/minecraft/registry/entry/RegistryEntryListCodec;entryListStorageCodec:Lcom/mojang/serialization/Codec;"
			),
			cancellable = true
	)
	private <T, E> void encodeCustomWithCustomCodec(RegistryEntryList<E> registryEntryList, DynamicOps<T> dynamicOps, T prefix, CallbackInfoReturnable<DataResult<T>> cir) {
		if (registryEntryList instanceof CustomRegistryEntryList<E> customRegistryEntryList) {
			cir.setReturnValue(fabric$codec.encode(customRegistryEntryList, dynamicOps, prefix));
		}
	}

	@Inject(
			method = "decode",
			at = @At(
					value = "FIELD",
					target = "Lnet/minecraft/registry/entry/RegistryEntryListCodec;entryListStorageCodec:Lcom/mojang/serialization/Codec;"
			),
			cancellable = true
	)
	private <T, E> void decodeCustomWithCustomCodec(DynamicOps<T> ops, T input, CallbackInfoReturnable<DataResult<Pair<RegistryEntryList<E>, T>>> cir) {
		if (CustomRegistryEntryListSerializerImpl.isSerializedCustomRegistryEntryList(ops, input)) {
			cir.setReturnValue(
					fabric$codec.decode(ops, input).map(
							pair -> pair.mapFirst(
									list -> (RegistryEntryList<E>) list
							)
					)
			);
		}
	}
}
