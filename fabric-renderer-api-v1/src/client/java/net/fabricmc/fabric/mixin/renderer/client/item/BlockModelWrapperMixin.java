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

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.client.renderer.item.BlockModelWrapper;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.resources.model.SpriteGetter;
import net.minecraft.world.entity.ItemOwner;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

import net.fabricmc.fabric.api.renderer.v1.mesh.Mesh;
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
	private void onReturnUpdate(final ItemStackRenderState output, final ItemStack item, final ItemModelResolver resolver, final ItemDisplayContext displayContext, final @Nullable ClientLevel level, final @Nullable ItemOwner owner, final int seed, CallbackInfo ci, @Local ItemStackRenderState.LayerRenderState layer) {
		if (mesh != null) {
			// This logic matches that of ITEM_RENDER_TYPE_GETTER and BLOCK_RENDER_TYPE_GETTER
			ChunkSectionLayer defaultSectionLayer;

			if (item.getItem() instanceof BlockItem blockItem) {
				defaultSectionLayer = ItemBlockRenderTypes.getChunkRenderType(blockItem.getBlock().defaultBlockState());
			} else {
				defaultSectionLayer = ChunkSectionLayer.TRANSLUCENT;
			}

			layer.setRenderTypeGetter((quadAtlas, sectionLayer) -> {
				return switch (quadAtlas) {
				case BLOCK -> {
					if (sectionLayer == null) {
						sectionLayer = defaultSectionLayer;
					}

					if (sectionLayer != ChunkSectionLayer.TRANSLUCENT) {
						yield Sheets.cutoutBlockSheet();
					}

					yield Sheets.translucentBlockItemSheet();
				}
				case ITEM -> Sheets.translucentItemSheet();
				};
			});

			mesh.outputTo(layer.emitter());
		}
	}

	@Override
	public void fabric_setMesh(Mesh mesh, SpriteGetter spriteGetter) {
		this.mesh = mesh;

		if (!animated) {
			mesh.forEach(quad -> {
				if (animated) {
					return;
				}

				ItemStackRenderState.FoilType glint = quad.glint();

				if ((glint != null && glint != ItemStackRenderState.FoilType.NONE)
						|| spriteGetter.spriteFinder(quad.atlas().getTextureId()).find(quad).contents().isAnimated()) {
					animated = true;
				}
			});
		}
	}
}
