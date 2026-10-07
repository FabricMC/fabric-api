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

import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.SharedConstants;
import net.minecraft.client.gui.screens.multiplayer.ServerSelectionList;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.network.chat.Component;

import net.fabricmc.fabric.impl.modprotocol.ModProtocolManager;
import net.fabricmc.fabric.impl.modprotocol.client.FabricServerData;

@Mixin(ServerSelectionList.OnlineServerEntry.class)
public class ServerSelectionListOnlineServerEntryMixin {
	@Unique
	private static final Component INCOMPATIBLE_MODS = Component.translatable("text.fabric-mod-protocol-v1.status.incompatible_mods");

	@Shadow
	@Final
	private ServerData serverData;

	@Shadow
	private @Nullable Component statusIconTooltip;

	@ModifyArg(method = "lambda$extractContent$2", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/ServerData;setState(Lnet/minecraft/client/multiplayer/ServerData$State;)V"))
	private ServerData.State markAsIncompatibleIfModsAreWrong(ServerData.State state) {
		ModProtocolManager.ValidationResult result = ((FabricServerData) this.serverData).fabric$getValidationResult();

		return result == null || result.isSuccess() ? state : ServerData.State.INCOMPATIBLE;
	}

	@Inject(method = "refreshStatus", at = @At("TAIL"))
	private void replaceIncompatibilityText(CallbackInfo ci) {
		ModProtocolManager.ValidationResult result = ((FabricServerData) this.serverData).fabric$getValidationResult();

		if (this.serverData.state() != ServerData.State.INCOMPATIBLE || this.serverData.protocol != SharedConstants.getProtocolVersion() || result == null || result.isSuccess()) {
			return;
		}

		this.statusIconTooltip = INCOMPATIBLE_MODS;
	}
}
