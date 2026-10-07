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

package net.fabricmc.fabric.impl.modprotocol;

import java.util.List;
import java.util.Optional;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;

import net.fabricmc.loader.api.Version;
import net.fabricmc.loader.api.metadata.version.VersionPredicate;

public record RemoteModProtocol(Identifier id, String name, String version,
								Optional<String> require) {
	public static final StreamCodec<FriendlyByteBuf, RemoteModProtocol> STREAM_CODEC = StreamCodec.composite(
			Identifier.STREAM_CODEC, RemoteModProtocol::id,
			ByteBufCodecs.STRING_UTF8, RemoteModProtocol::name,
			ByteBufCodecs.STRING_UTF8, RemoteModProtocol::version,
			ByteBufCodecs.optional(ByteBufCodecs.STRING_UTF8), RemoteModProtocol::require,
			RemoteModProtocol::new
	);

	public static final Codec<RemoteModProtocol> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Identifier.CODEC.fieldOf("id").forGetter(RemoteModProtocol::id),
			Codec.STRING.fieldOf("name").forGetter(RemoteModProtocol::name),
			Codec.STRING.fieldOf("version").forGetter(RemoteModProtocol::version),
			Codec.STRING.optionalFieldOf("require").forGetter(RemoteModProtocol::require)
	).apply(instance, RemoteModProtocol::new));

	public static final Codec<List<RemoteModProtocol>> LIST_CODEC = CODEC.listOf();

	public static final Codec<RemoteModProtocol> COMPRESSED_CODEC = Codec.STRING.flatXmap(string -> {
		int split1 = string.indexOf(0);
		int split2 = string.lastIndexOf(0);

		if (split1 == -1) {
			return DataResult.error(() -> "Invalid format! No null codepoint detected!");
		}

		DataResult<Identifier> id = Identifier.read(string.substring(0, split1));

		if (id.isError()) {
			return DataResult.error(id.error().orElseThrow().messageSupplier());
		}

		if (split1 == split2) {
			return DataResult.success(new RemoteModProtocol(id.getOrThrow(), "<?>", string.substring(split1 + 1), Optional.empty()));
		}

		return DataResult.success(new RemoteModProtocol(id.getOrThrow(), "<?>", string.substring(split1 + 1, split2), Optional.of(string.substring(split2 + 1))));
	}, protocol -> DataResult.success(protocol.id().toString() + '\u0000' + protocol.version + (protocol.require().isPresent() ? '\u0000' + protocol.require.get() : "")));

	public static final Codec<List<RemoteModProtocol>> COMPRESSED_LIST_CODEC = COMPRESSED_CODEC.listOf();

	public boolean matches(String version) {
		if (this.require.isEmpty()) {
			return true;
		}

		try {
			return VersionPredicate.parse(this.require.get()).test(Version.parse(version));
		} catch (Throwable e) {
			return false;
		}
	}
}
