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

package net.fabricmc.fabric.mixin.client.indigo.renderer;

import java.util.List;

import com.mojang.blaze3d.vertex.PoseStack;
import org.spongepowered.asm.mixin.Mixin;

import net.minecraft.client.renderer.OrderedSubmitNodeCollector;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.SubmitNodeStorage;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.world.item.ItemDisplayContext;

import net.fabricmc.fabric.api.renderer.v1.mesh.MeshView;
import net.fabricmc.fabric.api.renderer.v1.render.ItemRenderTypeGetter;
import net.fabricmc.fabric.impl.client.indigo.renderer.accessor.AccessOrderedSubmitNodeCollector;

@Mixin(SubmitNodeStorage.class)
abstract class SubmitNodeStorageMixin implements SubmitNodeCollector, AccessOrderedSubmitNodeCollector {
	@Override
	public void fabric_submitItem(
			PoseStack poseStack,
			ItemDisplayContext displayContext,
			int light,
			int overlay,
			int outlineColors,
			int[] tintLayers,
			List<BakedQuad> quads,
			RenderType renderType,
			ItemStackRenderState.FoilType foilType,
			MeshView mesh,
			ItemRenderTypeGetter renderTypeGetter
	) {
		OrderedSubmitNodeCollector nodeCollector = order(0);

		if (nodeCollector instanceof AccessOrderedSubmitNodeCollector access) {
			access.fabric_submitItem(poseStack, displayContext, light, overlay, outlineColors, tintLayers, quads,
					renderType,
					foilType, mesh, renderTypeGetter);
		} else {
			nodeCollector.submitItem(poseStack, displayContext, light, overlay, outlineColors, tintLayers, quads,
					renderType,
					foilType
			);
		}
	}
}
