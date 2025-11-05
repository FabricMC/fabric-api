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

import net.minecraft.client.renderer.item.BlockModelWrapper;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.client.resources.model.QuadCollection;

import net.fabricmc.fabric.api.renderer.v1.mesh.Mesh;
import net.fabricmc.fabric.api.renderer.v1.model.MeshBakedGeometry;
import net.fabricmc.fabric.impl.renderer.BasicItemModelExtension;

@Mixin(BlockModelWrapper.Unbaked.class)
abstract class BlockModelWrapperUnbakedMixin {
	@ModifyExpressionValue(method = "bake", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/resources/model/ResolvedModel;bakeTopGeometry(Lnet/minecraft/client/renderer/block/model/TextureSlots;Lnet/minecraft/client/resources/model/ModelBaker;Lnet/minecraft/client/resources/model/ModelState;)Lnet/minecraft/client/resources/model/QuadCollection;"))
	private QuadCollection captureMesh(QuadCollection geometry, @Share("mesh") LocalRef<Mesh> meshRef) {
		if (geometry instanceof MeshBakedGeometry meshBakedGeometry) {
			meshRef.set(meshBakedGeometry.getMesh());
		}

		return geometry;
	}

	@ModifyExpressionValue(method = "bake", at = @At(value = "NEW", target = "net/minecraft/client/renderer/item/BlockModelWrapper"))
	private BlockModelWrapper injectMesh(BlockModelWrapper model, ItemModel.BakingContext context, @Share("mesh") LocalRef<Mesh> meshRef) {
		Mesh mesh = meshRef.get();

		if (mesh != null) {
			((BasicItemModelExtension) model).fabric_setMesh(mesh, context.blockModelBaker().sprites());
		}

		return model;
	}
}
