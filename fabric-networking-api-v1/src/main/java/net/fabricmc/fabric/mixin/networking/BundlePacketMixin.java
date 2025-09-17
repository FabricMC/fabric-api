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

import java.util.ArrayList;
import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import net.minecraft.network.packet.BundlePacket;
import net.minecraft.network.packet.Packet;

@Mixin(BundlePacket.class)
public class BundlePacketMixin {
	@ModifyVariable(method = "<init>", at = @At("HEAD"), argsOnly = true)
	private static Iterable<? extends Packet<?>> flattenBundlePackets(Iterable<? extends Packet<?>> value) {
		var packets = new ArrayList<Packet<?>>();
		iterateBundle(value, packets);
		return packets;
	}

	@Unique
	private static void iterateBundle(Iterable<? extends Packet<?>> value, List<Packet<?>> result) {
		for (Packet<?> packet : value) {
			if (packet instanceof BundlePacket<?> bundlePacket) {
				iterateBundle(bundlePacket.getPackets(), result);
			} else {
				result.add(packet);
			}
		}
	}
}
