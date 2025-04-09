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

package net.fabricmc.fabric.mixin.renderer.client.item;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import net.minecraft.client.render.item.model.BasicItemModel;
import net.minecraft.client.render.model.BakedGeometry;

import net.fabricmc.fabric.api.renderer.v1.mesh.Mesh;
import net.fabricmc.fabric.api.renderer.v1.model.MeshBakedGeometry;
import net.fabricmc.fabric.impl.renderer.BasicItemModelExtension;

@Mixin(BasicItemModel.Unbaked.class)
abstract class BasicItemModelUnbakedMixin {
	@ModifyExpressionValue(method = "bake", at = @At(value = "INVOKE", target = "net/minecraft/client/render/model/BakedSimpleModel.bakeGeometry(Lnet/minecraft/client/render/model/ModelTextures;Lnet/minecraft/client/render/model/Baker;Lnet/minecraft/client/render/model/ModelBakeSettings;)Lnet/minecraft/client/render/model/BakedGeometry;"))
	private BakedGeometry captureMesh(BakedGeometry geometry, @Share("mesh") LocalRef<Mesh> meshRef) {
		if (geometry instanceof MeshBakedGeometry meshBakedGeometry) {
			meshRef.set(meshBakedGeometry.getMesh());
		}

		return geometry;
	}

	@ModifyExpressionValue(method = "bake", at = @At(value = "NEW", target = "net/minecraft/client/render/item/model/BasicItemModel"))
	private BasicItemModel injectMesh(BasicItemModel model, @Share("mesh") LocalRef<Mesh> meshRef) {
		Mesh mesh = meshRef.get();

		if (mesh != null) {
			((BasicItemModelExtension) model).setMesh(mesh);
		}

		return model;
	}
}
