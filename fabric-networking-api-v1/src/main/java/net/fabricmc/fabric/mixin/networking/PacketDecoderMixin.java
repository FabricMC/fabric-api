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

package net.fabricmc.fabric.mixin.networking;

import java.util.List;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import net.minecraft.network.PacketDecoder;

import net.fabricmc.fabric.api.networking.v1.context.PacketContext;
import net.fabricmc.fabric.impl.networking.context.PacketContextImpl;
import net.fabricmc.fabric.impl.networking.context.PacketContextSetter;

// Lowered the default priority, as this should happen before other mods.
@Mixin(value = PacketDecoder.class, priority = 500)
public class PacketDecoderMixin implements PacketContextSetter {
	@Unique
	private PacketContext packetContext;

	@WrapMethod(method = "decode")
	private void wrapWithContext(ChannelHandlerContext ctx, ByteBuf input, List<Object> out, Operation<Void> original) {
		ScopedValue.where(PacketContextImpl.VALUE, this.packetContext).run(() -> original.call(ctx, input, out));
	}

	@Override
	public void fabric_setPacketContext(PacketContext context) {
		this.packetContext = context;
	}
}
