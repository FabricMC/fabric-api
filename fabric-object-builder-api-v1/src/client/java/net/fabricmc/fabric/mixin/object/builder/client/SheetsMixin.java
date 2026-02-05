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

import net.minecraft.client.renderer.MaterialMapper;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.resources.model.Material;
import net.minecraft.resources.Identifier;

import net.fabricmc.fabric.impl.object.builder.client.SignTypeTextureHelper;

@Mixin(Sheets.class)
abstract class SheetsMixin {
	@Inject(method = "<clinit>*", at = @At("RETURN"))
	private static void onReturnClinit(CallbackInfo ci) {
		SignTypeTextureHelper.shouldAddTextures = true;
	}

	@Redirect(method = "createSignMaterial", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/MaterialMapper;defaultNamespaceApply(Ljava/lang/String;)Lnet/minecraft/client/resources/model/Material;"))
	private static Material redirectSignVanillaId(MaterialMapper instance, String name) {
		return instance.apply(Identifier.parse(name));
	}

	@Redirect(method = "createHangingSignMaterial", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/MaterialMapper;defaultNamespaceApply(Ljava/lang/String;)Lnet/minecraft/client/resources/model/Material;"))
	private static Material redirectHangingVanillaId(MaterialMapper instance, String name) {
		return instance.apply(Identifier.parse(name));
	}
}
