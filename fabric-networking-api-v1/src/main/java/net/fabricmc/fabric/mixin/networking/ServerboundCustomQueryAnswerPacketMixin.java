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

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.login.ServerboundCustomQueryAnswerPacket;
import net.minecraft.network.protocol.login.custom.CustomQueryAnswerPayload;

import net.fabricmc.fabric.impl.networking.payload.PacketByteBufLoginQueryResponse;
import net.fabricmc.fabric.impl.networking.payload.PayloadHelper;

@Mixin(ServerboundCustomQueryAnswerPacket.class)
public class ServerboundCustomQueryAnswerPacketMixin {
	@Shadow
	@Final
	private static int MAX_PAYLOAD_SIZE;

	@Inject(method = "readPayload", at = @At("HEAD"), cancellable = true)
	private static void readResponse(int queryId, FriendlyByteBuf buf, CallbackInfoReturnable<CustomQueryAnswerPayload> cir) {
		boolean hasPayload = buf.readBoolean();

		if (!hasPayload) {
			cir.setReturnValue(null);
			return;
		}

		cir.setReturnValue(new PacketByteBufLoginQueryResponse(PayloadHelper.read(buf, MAX_PAYLOAD_SIZE)));
	}
}
