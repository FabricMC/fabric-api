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

package net.fabricmc.fabric.mixin.command.client;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

import net.minecraft.commands.SuggestionBox;
import net.minecraft.network.protocol.game.ClientboundCommandsPacket;

import net.fabricmc.fabric.impl.command.client.ClientCommandInternals;

@Mixin(SuggestionBox.class)
abstract class SuggestionBoxMixin implements ClientCommandInternals.LastReceivedCommandsPacketAccessor {
	@Unique
	private @Nullable ClientboundCommandsPacket lastReceivedCommandsPacket;

	@ModifyReturnValue(method = "fromPacket", at = @At("RETURN"))
	private static SuggestionBox storePacket(SuggestionBox original, ClientboundCommandsPacket packet) {
		((SuggestionBoxMixin) (Object) original).lastReceivedCommandsPacket = packet;
		return original;
	}

	@Override
	public @Nullable ClientboundCommandsPacket fabric_api$getLastReceivedCommandsPacket() {
		return lastReceivedCommandsPacket;
	}
}
