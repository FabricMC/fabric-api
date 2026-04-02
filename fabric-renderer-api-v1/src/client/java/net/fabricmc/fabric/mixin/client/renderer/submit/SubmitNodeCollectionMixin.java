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

package net.fabricmc.fabric.mixin.client.renderer.submit;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import com.mojang.blaze3d.vertex.PoseStack;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.renderer.OrderedSubmitNodeCollector;
import net.minecraft.client.renderer.SubmitNodeCollection;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.world.item.ItemDisplayContext;

import net.fabricmc.fabric.api.client.renderer.v1.mesh.Mesh;
import net.fabricmc.fabric.api.client.renderer.v1.mesh.MeshView;
import net.fabricmc.fabric.api.client.renderer.v1.render.FabricSubmitNodeCollection;

@Mixin(SubmitNodeCollection.class)
abstract class SubmitNodeCollectionMixin implements OrderedSubmitNodeCollector, FabricSubmitNodeCollection {
	@Shadow
	private boolean wasUsed;

	@Unique
	private final List<ExtendedBlockModelSubmit> extendedBlockModelSubmits = new ArrayList<>();
	@Unique
	private final List<ExtendedItemSubmit> extendedItemSubmits = new ArrayList<>();

	@Override
	public void submitBlockModel(PoseStack poseStack, Function<ChunkSectionLayer, RenderType> renderTypeFunction, boolean translucent, List<BlockStateModelPart> parts, @Nullable Mesh mesh, int[] tintLayers, int lightCoords, int overlayCoords, int outlineColor) {
		wasUsed = true;
		extendedBlockModelSubmits.add(new ExtendedBlockModelSubmit(poseStack.last().copy(), renderTypeFunction, translucent, parts, mesh, tintLayers, lightCoords, overlayCoords, outlineColor));
	}

	@Override
	public void submitItem(PoseStack poseStack, ItemDisplayContext displayContext, int lightCoords, int overlayCoords, int outlineColor, int[] tintLayers, List<BakedQuad> quads, MeshView mesh, ItemStackRenderState.FoilType foilType) {
		this.wasUsed = true;
		extendedItemSubmits.add(new ExtendedItemSubmit(poseStack.last().copy(), displayContext, lightCoords, overlayCoords, outlineColor, tintLayers, quads, mesh, foilType));
	}

	@Override
	public List<ExtendedBlockModelSubmit> getExtendedBlockModelSubmits() {
		return extendedBlockModelSubmits;
	}

	@Override
	public List<ExtendedItemSubmit> getExtendedItemSubmits() {
		return extendedItemSubmits;
	}

	@Inject(method = "clear", at = @At("RETURN"))
	private void onReturnClear(CallbackInfo ci) {
		extendedBlockModelSubmits.clear();
		extendedItemSubmits.clear();
	}
}
