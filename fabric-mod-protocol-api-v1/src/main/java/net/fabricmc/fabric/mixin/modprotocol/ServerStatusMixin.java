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

package net.fabricmc.fabric.mixin.modprotocol;

import java.util.List;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

import net.minecraft.network.protocol.status.ServerStatus;

import net.fabricmc.fabric.impl.modprotocol.ModProtocolHolder;
import net.fabricmc.fabric.impl.modprotocol.RemoteModProtocol;

@Mixin(ServerStatus.class)
public class ServerStatusMixin implements ModProtocolHolder {
	@Unique
	@Nullable
	private List<RemoteModProtocol> modProtocol;

	@Unique
	private boolean alwaysCompatible = false;

	@Override
	public List<RemoteModProtocol> fabric$getModProtocol() {
		return this.modProtocol;
	}

	@Override
	public void fabric$setModProtocol(List<RemoteModProtocol> protocol) {
		this.modProtocol = protocol;
	}

	@Override
	public boolean fabric$getAlwaysCompatible() {
		return this.alwaysCompatible;
	}

	@Override
	public void fabric$setAlwaysCompatible(boolean value) {
		this.alwaysCompatible = value;
	}

	@ModifyExpressionValue(method = "<clinit>", at = @At(value = "INVOKE", target = "Lcom/mojang/serialization/codecs/RecordCodecBuilder;create(Ljava/util/function/Function;)Lcom/mojang/serialization/Codec;"))
	private static Codec<ServerStatus> extendCodec(Codec<ServerStatus> original) {
		return new Codec<>() {
			@Override
			public <T> DataResult<Pair<ServerStatus, T>> decode(DynamicOps<T> ops, T input) {
				DataResult<Pair<ServerStatus, T>> decoded = original.decode(ops, input);

				if (decoded.isSuccess()) {
					ModProtocolHolder holder = ModProtocolHolder.of(decoded.getOrThrow().getFirst());

					DataResult<T> versions = ops.get(input, "fabric:mod_protocol_v1/versions");

					if (versions.isSuccess()) {
						DataResult<Pair<List<RemoteModProtocol>, T>> result = RemoteModProtocol.COMPRESSED_LIST_CODEC.decode(ops, versions.getOrThrow());

						if (result.isSuccess()) {
							holder.fabric$setModProtocol(result.getOrThrow().getFirst());
						}
					}

					DataResult<Boolean> alwaysCompatible = ops.get(input, "fabric:mod_protocol_v1/always_compatible").flatMap(ops::getBooleanValue);

					if (alwaysCompatible.isSuccess()) {
						holder.fabric$setAlwaysCompatible(alwaysCompatible.getOrThrow());
					}
				}

				return decoded;
			}

			@Override
			public <T> DataResult<T> encode(ServerStatus input, DynamicOps<T> ops, T prefix) {
				DataResult<T> encode = original.encode(input, ops, prefix);

				ModProtocolHolder holder = ModProtocolHolder.of(input);

				if (encode.isSuccess() && holder.fabric$getModProtocol() != null) {
					DataResult<T> protocol = RemoteModProtocol.COMPRESSED_LIST_CODEC.encodeStart(ops, holder.fabric$getModProtocol());

					if (protocol.isSuccess()) {
						encode = ops.mergeToMap(encode.getOrThrow(), ops.createString("fabric:mod_protocol_v1/versions"), protocol.getOrThrow());
					}

					if (holder.fabric$getAlwaysCompatible()) {
						encode = ops.mergeToMap(encode.getOrThrow(), ops.createString("fabric:mod_protocol_v1/always_compatible"), ops.createBoolean(true));
					}
				}

				return encode;
			}
		};
	}
}
