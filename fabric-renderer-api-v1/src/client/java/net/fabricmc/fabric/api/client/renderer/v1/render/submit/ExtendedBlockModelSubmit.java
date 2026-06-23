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

package net.fabricmc.fabric.api.client.renderer.v1.render.submit;

import java.util.List;
import java.util.function.Function;

import com.mojang.blaze3d.vertex.PoseStack;
import org.jspecify.annotations.Nullable;

import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.client.renderer.feature.BlockModelFeatureRenderer;
import net.minecraft.client.renderer.feature.FeatureRendererType;
import net.minecraft.client.renderer.feature.submit.TranslucentSubmit;
import net.minecraft.client.renderer.rendertype.RenderType;

import net.fabricmc.fabric.api.client.renderer.v1.mesh.Mesh;

/**
 * An alternative to {@link BlockModelFeatureRenderer.Submit} that optionally accepts a {@link Mesh}.
 */
//CHECKSTYLE.OFF: MatchXpath
public record ExtendedBlockModelSubmit(PoseStack.Pose pose,
									Function<ChunkSectionLayer, @Nullable RenderType> renderTypeFunction,
									List<BlockStateModelPart> modelParts, @Nullable Mesh mesh,
									int[] tintLayers, int lightCoords, int overlayCoords,
									int tintColor, PoseStack.@Nullable Pose sheetedDecalPose) implements TranslucentSubmit {
	//CHECKSTYLE.ON: MatchXpath
	public static final FeatureRendererType<ExtendedBlockModelSubmit> TYPE = FeatureRendererType.create("Extended Block Model");

	@Override
	public float distanceToCameraSq() {
		return TranslucentSubmit.computeDistanceToCameraSq(pose.pose(), 0.5F, 0.5F, 0.5F);
	}

	@Override
	public FeatureRendererType<? extends TranslucentSubmit> featureType() {
		return TYPE;
	}
}
