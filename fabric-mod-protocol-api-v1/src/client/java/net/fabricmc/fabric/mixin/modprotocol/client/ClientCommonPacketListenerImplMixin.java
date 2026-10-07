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

package net.fabricmc.fabric.mixin.modprotocol.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ClientCommonPacketListenerImpl;
import net.minecraft.network.DisconnectionDetails;

import net.fabricmc.fabric.impl.modprotocol.ModProtocolManager;
import net.fabricmc.fabric.impl.modprotocol.client.ClientModProtocolInit;

@Mixin(ClientCommonPacketListenerImpl.class)
public abstract class ClientCommonPacketListenerImplMixin {
	@WrapOperation(method = "onDisconnect", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/ClientCommonPacketListenerImpl;createDisconnectScreen(Lnet/minecraft/network/DisconnectionDetails;)Lnet/minecraft/client/gui/screens/Screen;"))
	private Screen provideInfoForExtraButton(ClientCommonPacketListenerImpl instance, DisconnectionDetails details, Operation<Screen> original) {
		ModProtocolManager.ValidationResult result = instance.getPacketContext().get(ClientModProtocolInit.VALIDATION_RESULT_KEY);

		if (result == null) {
			return original.call(instance, details);
		}

		return ScopedValue.where(ClientModProtocolInit.VALIDATION_RESULT_SCOPED_VALUE, result).call(() -> original.call(instance, details));
	}
}
