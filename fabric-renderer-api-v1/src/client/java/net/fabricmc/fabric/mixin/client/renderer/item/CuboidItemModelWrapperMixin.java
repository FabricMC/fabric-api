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

package net.fabricmc.fabric.mixin.client.renderer.item;

import com.llamalad7.mixinextras.sugar.Local;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.CuboidItemModelWrapper;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.resources.model.geometry.QuadCollection;
import net.minecraft.world.entity.ItemOwner;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

import net.fabricmc.fabric.api.client.renderer.v1.model.MeshQuadCollection;

// NOTE: We intentionally do not validate the mesh like vanilla validates the quad list. Nowadays,
// both vanilla and FRAPI allow using multiple atlases from within the same layer, so perhaps the
// vanilla validation is a holdover from older versions. Regardless, we want to allow this for
// meshes.
@Mixin(CuboidItemModelWrapper.class)
abstract class CuboidItemModelWrapperMixin implements ItemModel {
	@Shadow
	@Final
	private QuadCollection quads;

	@Inject(method = "update", at = @At("RETURN"))
	private void onReturnUpdate(ItemStackRenderState output, ItemStack item, ItemModelResolver resolver, ItemDisplayContext displayContext, ClientLevel level, ItemOwner owner, int seed, CallbackInfo ci, @Local(name = "layer") ItemStackRenderState.LayerRenderState layer) {
		if (quads instanceof MeshQuadCollection meshQuadCollection) {
			meshQuadCollection.getMesh().outputTo(layer.emitter());
		}
	}
}
