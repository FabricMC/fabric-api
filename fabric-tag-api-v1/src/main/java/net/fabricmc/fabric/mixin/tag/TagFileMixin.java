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

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Decoder;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.Encoder;
import org.jspecify.annotations.NonNull;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.tags.TagEntry;
import net.minecraft.tags.TagFile;

import net.fabricmc.fabric.impl.tag.TagFileHooks;
import net.fabricmc.fabric.impl.tag.util.WrapperCodec;

@Mixin(TagFile.class)
public class TagFileMixin implements TagFileHooks {
	@Unique
	private List<TagEntry> removed = Collections.emptyList();

	@Shadow
	@Mutable
	@Final
	public static Codec<TagFile> CODEC;

	@Inject(method = "<clinit>", at = @At("TAIL"))
	private static void modifyCodec(CallbackInfo ci) {
		Codec<List<TagEntry>> removeEntryCodec = TagEntry.CODEC
				.listOf()
				.lenientOptionalFieldOf("fabric:remove", Collections.emptyList())
				.codec();

		CODEC = new WrapperCodec<>(CODEC, new WrapperCodec.Wrapper<>() {
			@Override
			public <T> DataResult<T> encode(TagFile input, DynamicOps<T> ops, T prefix, Encoder<TagFile> wrapped) {
				return wrapped.encode(input, ops, prefix).flatMap(
						result -> removeEntryCodec.encode(
								((TagFileHooks) (Object) input).fabric_removed(),
								ops,
								result
						)
				);
			}

			@Override
			public <T> DataResult<Pair<TagFile, T>> decode(DynamicOps<T> ops, T input, Decoder<TagFile> wrapped) {
				return removeEntryCodec.decode(ops, input).flatMap(
						result ->
								wrapped.decode(ops, input).map(pair -> pair.mapFirst(tagFile -> {
									((TagFileHooks) (Object) tagFile).fabric_setRemoved(result.getFirst());
									return tagFile;
								}))
				);
			}
		});
	}

	@Override
	public List<TagEntry> fabric_removed() {
		return removed;
	}

	@Override
	public void fabric_setRemoved(List<TagEntry> removed) {
		this.removed = removed;
	}
}
