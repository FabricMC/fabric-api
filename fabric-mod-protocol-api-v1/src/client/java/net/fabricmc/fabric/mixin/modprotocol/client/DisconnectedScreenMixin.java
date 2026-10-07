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
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.screens.DisconnectedScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import net.fabricmc.fabric.impl.modprotocol.ModProtocolManager;
import net.fabricmc.fabric.impl.modprotocol.client.ClientModProtocolInit;
import net.fabricmc.fabric.impl.modprotocol.client.MismatchedVersionScreen;

@Mixin(DisconnectedScreen.class)
public abstract class DisconnectedScreenMixin extends Screen {
	@Unique
	private static final Component MISMATCHED_VERSIONS_BUTTON = Component.translatable("text.fabric-mod-protocol-v1.mismatched_versions.button");

	@Unique
	private ModProtocolManager.@Nullable ValidationResult validationResult = null;

	@Shadow
	@Final
	private LinearLayout layout;

	protected DisconnectedScreenMixin(Component title) {
		super(title);
	}

	@Inject(method = "<init>(Lnet/minecraft/client/gui/screens/Screen;Lnet/minecraft/network/chat/Component;Lnet/minecraft/network/DisconnectionDetails;Lnet/minecraft/network/chat/Component;)V", at = @At("TAIL"))
	private void catchResults(CallbackInfo ci) {
		if (ClientModProtocolInit.VALIDATION_RESULT_SCOPED_VALUE.isBound()) {
			this.validationResult = ClientModProtocolInit.VALIDATION_RESULT_SCOPED_VALUE.get();
		}
	}

	@Inject(method = "init", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/DisconnectionDetails;bugReportLink()Ljava/util/Optional;"))
	private void addDetailsButton(CallbackInfo ci) {
		if (this.validationResult == null || this.validationResult.missingCount() <= ModProtocolManager.INLINE_VERSION_COUNT) {
			return;
		}

		this.layout.addChild(Button.builder(MISMATCHED_VERSIONS_BUTTON, _ -> this.minecraft.setScreenAndShow(new MismatchedVersionScreen(this.validationResult, this))).width(200).build());
	}
}
