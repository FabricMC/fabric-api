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

import java.util.List;
import java.util.function.Function;

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

import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.item.BlockModelWrapper;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.resources.model.SpriteGetter;
import net.minecraft.world.item.ItemStack;

import net.fabricmc.fabric.api.renderer.v1.mesh.Mesh;
import net.fabricmc.fabric.api.renderer.v1.mesh.QuadAtlas;
import net.fabricmc.fabric.api.renderer.v1.model.SpriteFinder;
import net.fabricmc.fabric.impl.renderer.BasicItemModelExtension;

@Mixin(BlockModelWrapper.class)
abstract class BlockModelWrapperMixin implements ItemModel, BasicItemModelExtension {
	@Shadow
	@Final
	private static Function<ItemStack, RenderType> ITEM_RENDER_TYPE_GETTER;

	@Shadow
	@Final
	private static Function<ItemStack, RenderType> BLOCK_RENDER_TYPE_GETTER;

	@Shadow
	@Final
	private List<BakedQuad> quads;

	@Shadow
	@Final
	@Mutable
	private boolean animated;

	@Shadow
	@Final
	@Mutable
	private Function<ItemStack, RenderType> renderType;

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
		if (mesh.size() == 0) {
			return;
		}

		QuadAtlas atlas;

		if (quads.isEmpty()) {
			QuadAtlas[] mutableAtlas = new QuadAtlas[1];

			mesh.forEach(quad -> {
				if (mutableAtlas[0] == null) {
					mutableAtlas[0] = quad.atlas();
				} else if (quad.atlas() != mutableAtlas[0]) {
					throw new IllegalStateException("Multiple atlases used in model, expected " + mutableAtlas[0].getTextureId() + ", but also got " + quad.atlas().getTextureId());
				}
			});

			atlas = mutableAtlas[0];

			renderType = switch (atlas) {
			case ITEM -> ITEM_RENDER_TYPE_GETTER;
			case BLOCK -> BLOCK_RENDER_TYPE_GETTER;
			};
		} else {
			atlas = QuadAtlas.of(quads.getFirst().sprite().atlasLocation());

			if (atlas == null) {
				// We should log something here
				return;
			}

			mesh.forEach(quad -> {
				if (quad.atlas() != atlas) {
					throw new IllegalStateException("Multiple atlases used in model, expected " + atlas.getTextureId() + ", but also got " + quad.atlas().getTextureId());
				}
			});
		}

		this.mesh = mesh;

		if (!animated) {
			SpriteFinder spriteFinder = spriteGetter.spriteFinder(atlas.getTextureId());

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
