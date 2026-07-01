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

package net.fabricmc.fabric.api.client.rendering.v1;

import net.minecraft.client.renderer.feature.submit.SubmitNode;
import net.minecraft.client.renderer.feature.submit.TranslucentSubmit;

/**
 * Vanilla's built-in {@link SubmitRenderPhase}s.
 */
public final class SubmitRenderPhases {
	public static final SubmitRenderPhase<SubmitNode> SOLID = new SubmitRenderPhase<>(x -> x.solid);
	public static final SubmitRenderPhase<SubmitNode> SHADOWS = new SubmitRenderPhase<>(x -> x.shadows);
	public static final SubmitRenderPhase<SubmitNode> NAME_TAGS = new SubmitRenderPhase<>(x -> x.nameTags);
	public static final SubmitRenderPhase<TranslucentSubmit> SEE_THROUGH_NAME_TAGS = new SubmitRenderPhase<>(x -> x.seeThroughNameTags);
	public static final SubmitRenderPhase<SubmitNode> TEXTS = new SubmitRenderPhase<>(x -> x.texts);
	public static final SubmitRenderPhase<SubmitNode> SHAPE_OUTLINES = new SubmitRenderPhase<>(x -> x.shapeOutlines);
	public static final SubmitRenderPhase<TranslucentSubmit> TRANSLUCENT_BLOCKS_AND_ITEMS = new SubmitRenderPhase<>(x -> x.translucentBlocksAndItems);
	public static final SubmitRenderPhase<TranslucentSubmit> TRANSLUCENT_MODELS = new SubmitRenderPhase<>(x -> x.translucentModels);
	public static final SubmitRenderPhase<SubmitNode> TRANSLUCENT_CUSTOM_GEOMETRY = new SubmitRenderPhase<>(x -> x.translucentCustomGeometry);
	public static final SubmitRenderPhase<SubmitNode> GIZMOS = new SubmitRenderPhase<>(x -> x.translucentGizmos);
	public static final SubmitRenderPhase<SubmitNode> BREAKING_OVERLAY = new SubmitRenderPhase<>(x -> x.breakingOverlay);
	public static final SubmitRenderPhase<SubmitNode> WATER_MASK = new SubmitRenderPhase<>(x -> x.waterMask);
	public static final SubmitRenderPhase<SubmitNode> AFTER_TERRAIN = new SubmitRenderPhase<>(x -> x.afterTerrain);
	public static final SubmitRenderPhase<SubmitNode> ALWAYS_ON_TOP = new SubmitRenderPhase<>(x -> x.alwaysOnTopGizmos);
	public static final SubmitRenderPhase<SubmitNode> OUTLINE = new SubmitRenderPhase<>(x -> x.outline);

	private SubmitRenderPhases() {
	}
}
