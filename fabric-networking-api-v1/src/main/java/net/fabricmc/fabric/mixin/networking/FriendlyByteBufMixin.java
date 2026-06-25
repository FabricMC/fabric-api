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

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;

@Mixin(FriendlyByteBuf.class)
public class FriendlyByteBufMixin {
	@ModifyArg(
			method = {
					"readCollection",
					"readMap(Ljava/util/function/IntFunction;Lnet/minecraft/network/codec/StreamDecoder;Lnet/minecraft/network/codec/StreamDecoder;)Ljava/util/Map;"
			},
			at = @At(value = "INVOKE", target = "Ljava/util/function/IntFunction;apply(I)Ljava/lang/Object;"),
			index = 0,
			require = 2
	)
	private int limitInitialCapacity(int value) {
		return Math.min(value, ByteBufCodecs.MAX_INITIAL_COLLECTION_SIZE);
	}
}
