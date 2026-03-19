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

package net.fabricmc.fabric.mixin.tag;

import java.util.Collections;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import com.google.common.collect.Streams;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Decoder;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.Encoder;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Slice;

import net.minecraft.tags.TagEntry;
import net.minecraft.tags.TagFile;

import net.fabricmc.fabric.api.tag.v1.FabricTagEntry;
import net.fabricmc.fabric.impl.tag.FabricTagEntryInternals;
import net.fabricmc.fabric.impl.tag.util.WrapperCodec;

@Mixin(TagFile.class)
public class TagFileMixin {
	@Unique
	private static final ThreadLocal<List<TagEntry>> REMOVE_ENTRIES = ThreadLocal.withInitial(List::of);

	@Shadow
	@Final
	public static Codec<TagFile> CODEC;

	@ModifyArg(
			method = "lambda$static$0",
			at = @At(
					value = "INVOKE",
					target = "Lcom/mojang/datafixers/Products$P2;apply(Lcom/mojang/datafixers/kinds/Applicative;Ljava/util/function/BiFunction;)Lcom/mojang/datafixers/kinds/App;"
			)
	)
	private static BiFunction<List<TagEntry>, Boolean, TagFile> modify(BiFunction<List<TagEntry>, Boolean, TagFile> instance) {
		return (entries, replace) -> instance.apply(
				Streams.concat(entries.stream(), REMOVE_ENTRIES.get().stream()).toList(),
				replace
		);
	}

	@ModifyArg(
			method = "lambda$static$0",
			at = @At(
					value = "INVOKE:FIRST",
					target = "Lcom/mojang/serialization/MapCodec;forGetter(Ljava/util/function/Function;)Lcom/mojang/serialization/codecs/RecordCodecBuilder;"
			),
			slice = @Slice(from = @At(value = "CONSTANT", args = "stringValue=values"))
	)
	private static Function<TagFile, List<TagEntry>> wrapGetter(Function<TagFile, List<TagEntry>> getter) {
		return getter.andThen(list -> list.stream().filter(Predicate.not(entry -> ((FabricTagEntry) entry).isRemoved())).toList());
	}

	static {
		Codec<List<TagEntry>> removeEntryCodec = FabricTagEntryInternals.REMOVED_ENTRY_CODEC
				.listOf()
				.lenientOptionalFieldOf("fabric:remove", Collections.emptyList())
				.codec();

		CODEC = new WrapperCodec<>(CODEC, new WrapperCodec.Wrapper<>() {
			@Override
			public <T> DataResult<T> encode(TagFile input, DynamicOps<T> ops, T prefix, Encoder<TagFile> wrapped) {
				return wrapped.encode(input, ops, prefix).flatMap(
						result -> removeEntryCodec.encode(
								List.copyOf(
										input.entries()
												.stream()
												.filter(entry -> ((FabricTagEntry) entry).isRemoved())
												.toList()
								),
								ops,
								result
						)
				);
			}

			@Override
			public <T> DataResult<Pair<TagFile, T>> decode(DynamicOps<T> ops, T input, Decoder<TagFile> wrapped) {
				return removeEntryCodec.decode(ops, input).flatMap(
						result -> withRemovedEntries(result.getFirst(), () -> wrapped.decode(ops, input))
				);
			}
		});
	}

	@Unique
	private static <T> T withRemovedEntries(List<TagEntry> removed, Supplier<T> action) {
		List<TagEntry> initialValue = REMOVE_ENTRIES.get();

		try {
			REMOVE_ENTRIES.set(removed);
			return action.get();
		} finally {
			REMOVE_ENTRIES.set(initialValue);
		}
	}
}
