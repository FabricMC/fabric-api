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

import com.llamalad7.mixinextras.sugar.Local;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.renderer.item.BlockModelWrapper;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.resources.model.SpriteGetter;

import net.fabricmc.fabric.api.renderer.v1.mesh.Mesh;
import net.fabricmc.fabric.api.renderer.v1.model.SpriteFinder;
import net.fabricmc.fabric.impl.renderer.BasicItemModelExtension;

@Mixin(BlockModelWrapper.class)
abstract class BlockModelWrapperMixin implements ItemModel, BasicItemModelExtension {
	@Shadow
	@Final
	@Mutable
	private boolean animated;

	@Unique
	@Nullable
	private Mesh mesh;

	@Inject(method = "update", at = @At("RETURN"))
	private void onReturnUpdate(CallbackInfo ci, @Local ItemStackRenderState.LayerRenderState layer) {
		if (mesh != null) {
			mesh.outputTo(layer.emitter());
		}
	}

	@Override
	public void fabric_setMesh(Mesh mesh, SpriteGetter spriteGetter) {
		this.mesh = mesh;

		if (!animated) {
			SpriteFinder spriteFinder = spriteGetter.spriteFinder(TextureAtlas.LOCATION_BLOCKS);

			mesh.forEach(quad -> {
				if (animated) {
					return;
				}

				ItemStackRenderState.FoilType glint = quad.glint();

				if ((glint != null && glint != ItemStackRenderState.FoilType.NONE) || spriteFinder.find(quad).contents().isAnimated()) {
					animated = true;
				}
			});
		}
	}
}
