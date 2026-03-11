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
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.blockentity.HangingSignRenderer;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.state.properties.WoodType;

@Mixin(ModelLayers.class)
public class ModelLayersMixin {
	@Inject(method = "createStandingSignModelName", at = @At("HEAD"), cancellable = true)
	private static void createStandingSign(WoodType type, CallbackInfoReturnable<ModelLayerLocation> cir) {
		if (type.name().indexOf(Identifier.NAMESPACE_SEPARATOR) != -1) {
			Identifier identifier = Identifier.parse(type.name());
			cir.setReturnValue(new ModelLayerLocation(identifier.withPrefix("sign/standing/"), "main"));
		}
	}

	@Inject(method = "createWallSignModelName", at = @At("HEAD"), cancellable = true)
	private static void createWallSign(WoodType type, CallbackInfoReturnable<ModelLayerLocation> cir) {
		if (type.name().indexOf(Identifier.NAMESPACE_SEPARATOR) != -1) {
			Identifier identifier = Identifier.parse(type.name());
			cir.setReturnValue(new ModelLayerLocation(identifier.withPrefix("sign/wall/"), "main"));
		}
	}

	@Inject(method = "createHangingSignModelName", at = @At("HEAD"), cancellable = true)
	private static void createHangingSign(WoodType type, HangingSignRenderer.AttachmentType attachmentType, CallbackInfoReturnable<ModelLayerLocation> cir) {
		if (type.name().indexOf(Identifier.NAMESPACE_SEPARATOR) != -1) {
			Identifier identifier = Identifier.parse(type.name());
			cir.setReturnValue(new ModelLayerLocation(identifier.withPath(path -> {
				return "hanging_sign/" + path + "/" + attachmentType.getSerializedName();
			}), "main"));
		}
	}
}
