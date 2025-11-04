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

package net.fabricmc.fabric.impl.particle;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

public class ExtendedBlockStateParticleEffectPacketCodec implements StreamCodec<RegistryFriendlyByteBuf, BlockParticleOption> {
	private static final int PACKET_MARKER = -1;
	private final StreamCodec<? super RegistryFriendlyByteBuf, BlockParticleOption> fallback;

	public ExtendedBlockStateParticleEffectPacketCodec(StreamCodec<? super RegistryFriendlyByteBuf, BlockParticleOption> fallback) {
		this.fallback = fallback;
	}

	@Override
	public BlockParticleOption decode(RegistryFriendlyByteBuf buf) {
		int index = buf.readerIndex();

		if (buf.readVarInt() != PACKET_MARKER) {
			// Reset index for vanilla's normal deserialization logic.
			buf.readerIndex(index);
			return fallback.decode(buf);
		}

		BlockParticleOption value = fallback.decode(buf);
		BlockPos pos = BlockPos.STREAM_CODEC.decode(buf);
		((BlockStateParticleEffectExtension) value).fabric_setBlockPos(pos);
		return value;
	}

	@Override
	public void encode(RegistryFriendlyByteBuf buf, BlockParticleOption value) {
		BlockPos pos = value.getBlockPos();

		if (pos == null || ExtendedBlockStateParticleEffectSync.shouldEncodeFallback(buf)) {
			fallback.encode(buf, value);
			return;
		}

		buf.writeVarInt(PACKET_MARKER);
		fallback.encode(buf, value);
		BlockPos.STREAM_CODEC.encode(buf, pos);
	}
}
