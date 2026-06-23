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

import java.util.List;
import java.util.function.Function;

import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.vertex.PoseStack;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import net.minecraft.client.renderer.OrderedSubmitNodeCollector;
import net.minecraft.client.renderer.SubmitNodeCollection;
import net.minecraft.client.renderer.block.BlockModelRenderState;
import net.minecraft.client.renderer.block.MovingBlockRenderState;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.client.renderer.feature.phase.SimpleFeatureRenderPhase;
import net.minecraft.client.renderer.feature.phase.TranslucentFeatureRenderPhase;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.level.block.state.BlockState;

import net.fabricmc.fabric.api.client.renderer.v1.mesh.Mesh;
import net.fabricmc.fabric.api.client.renderer.v1.mesh.MeshView;
import net.fabricmc.fabric.api.client.renderer.v1.render.submit.ExtendedBlockModelSubmit;
import net.fabricmc.fabric.api.client.renderer.v1.render.submit.ExtendedItemSubmit;

@Mixin(SubmitNodeCollection.class)
abstract class SubmitNodeCollectionMixin implements OrderedSubmitNodeCollector {
	@Shadow
	@Final
	public SimpleFeatureRenderPhase solid;
	@Shadow
	@Final
	public TranslucentFeatureRenderPhase translucentBlocksAndItems;
	@Shadow
	@Final
	public SimpleFeatureRenderPhase breakingOverlay;
	@Shadow
	@Final
	public SimpleFeatureRenderPhase outline;

	@Shadow
	@Nullable
	private static RenderType getOutlineRenderType(RenderType renderType) {
		return null;
	}

	@Override
	public void submitBlockModel(PoseStack poseStack, Function<ChunkSectionLayer, RenderType> renderTypeFunction, boolean translucent, List<BlockStateModelPart> parts, @Nullable Mesh mesh, int[] tintLayers, int lightCoords, int overlayCoords, int outlineColor) {
		PoseStack.Pose pose = poseStack.last().copy();
		Function<ChunkSectionLayer, @Nullable RenderType> filteringRenderTypeFunction = layer -> {
			RenderType renderType = renderTypeFunction.apply(layer);
			return renderType.isOutline() ? null : renderType;
		};
		ExtendedBlockModelSubmit submit = new ExtendedBlockModelSubmit(pose, filteringRenderTypeFunction, parts, mesh, tintLayers, lightCoords, overlayCoords, -1, null);

		if (translucent) {
			translucentBlocksAndItems.submit(submit);
		} else {
			solid.submit(submit);
		}

		if (outlineColor != 0) {
			Function<ChunkSectionLayer, @Nullable RenderType> outlineRenderTypeFunction = layer -> getOutlineRenderType(renderTypeFunction.apply(layer));
			outline.submit(new ExtendedBlockModelSubmit(pose, outlineRenderTypeFunction, parts, mesh, BlockModelRenderState.EMPTY_TINTS, LightCoordsUtil.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, outlineColor, null));
		}
	}

	@Override
	public void submitBreakingBlockModel(PoseStack poseStack, List<BlockStateModelPart> parts, Mesh mesh, int progress) {
		PoseStack.Pose pose = poseStack.last().copy();
		RenderType renderType = ModelBakery.DESTROY_TYPES.get(progress);
		breakingOverlay.submit(new ExtendedBlockModelSubmit(pose, _ -> renderType, List.copyOf(parts), mesh, BlockModelRenderState.EMPTY_TINTS, LightCoordsUtil.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, 0, pose));
	}

	@Override
	public void submitItem(PoseStack poseStack, ItemDisplayContext displayContext, int lightCoords, int overlayCoords, int outlineColor, int[] tintLayers, List<BakedQuad> quads, MeshView mesh, ItemStackRenderState.FoilType foilType) {
		PoseStack.Pose pose = poseStack.last().copy();
		ExtendedItemSubmit submit = new ExtendedItemSubmit(pose, displayContext, lightCoords, overlayCoords, 0, tintLayers, quads, mesh, foilType);

		if (submit.hasTranslucency()) {
			translucentBlocksAndItems.submit(submit);
		} else {
			solid.submit(submit);
		}

		if (outlineColor != 0) {
			outline.submit(new ExtendedItemSubmit(pose, displayContext, LightCoordsUtil.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, outlineColor, ItemStackRenderState.LayerRenderState.EMPTY_TINTS, quads, mesh, ItemStackRenderState.FoilType.NONE));
		}
	}

	@Redirect(method = "submitMovingBlock", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/block/dispatch/BlockStateModel;hasMaterialFlag(I)Z"))
	private boolean hasMaterialFlagProxy(BlockStateModel model, @BakedQuad.MaterialFlags int flag, @Local(name = "movingBlockRenderState") MovingBlockRenderState movingBlockRenderState) {
		BlockState blockState = movingBlockRenderState.blockState;
		long randomSeed = blockState.getSeed(movingBlockRenderState.randomSeedPos);
		RandomSource random = RandomSource.createThreadLocalInstance(randomSeed);
		return model.hasMaterialFlag(movingBlockRenderState, movingBlockRenderState.blockPos, blockState, random, flag);
	}
}
