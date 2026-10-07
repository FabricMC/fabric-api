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

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import net.minecraft.network.protocol.status.ServerStatus;
import net.minecraft.server.MinecraftServer;

import net.fabricmc.fabric.impl.modprotocol.ModProtocolHolder;
import net.fabricmc.fabric.impl.modprotocol.ModProtocolManager;

@Mixin(MinecraftServer.class)
public class MinecraftServerMixin {
	@ModifyReturnValue(method = "buildServerStatus", at = @At("RETURN"))
	private ServerStatus addModProtocol(ServerStatus original) {
		if (!ModProtocolManager.SYNCED_PROTOCOLS.isEmpty()) {
			ModProtocolHolder.of(original).fabric$setModProtocol(ModProtocolManager.SYNCED_PROTOCOLS);
		}

		return original;
	}
}
