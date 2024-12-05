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

package net.fabricmc.fabric.mixin.object.builder.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.render.TexturedRenderLayers;
import net.minecraft.util.Identifier;

import net.fabricmc.fabric.impl.object.builder.client.SignTypeTextureHelper;

@Mixin(TexturedRenderLayers.class)
abstract class TexturedRenderLayersMixin {
	@Inject(method = "<clinit>*", at = @At("RETURN"))
	private static void onReturnClinit(CallbackInfo ci) {
		SignTypeTextureHelper.shouldAddTextures = true;
	}

	@Redirect(method = "createSignTextureId", at = @At(value = "INVOKE", target = "net/minecraft/util/Identifier.ofVanilla(Ljava/lang/String;)Lnet/minecraft/util/Identifier;"))
	private static Identifier redirectSignVanillaId(String name) {
		return Identifier.of(name);
	}

	@Redirect(method = "createHangingSignTextureId", at = @At(value = "INVOKE", target = "net/minecraft/util/Identifier.ofVanilla(Ljava/lang/String;)Lnet/minecraft/util/Identifier;"))
	private static Identifier redirectHangingVanillaId(String name) {
		return Identifier.of(name);
	}
}
