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
import net.fabricmc.fabric.impl.client.indigo.renderer.accessor.AccessRenderCommandQueue;

@Mixin(SubmitNodeStorage.class)
abstract class SubmitNodeStorageMixin implements SubmitNodeCollector, AccessRenderCommandQueue {
	@Override
	public void fabric_submitItem(
			PoseStack matrices,
			ItemDisplayContext displayContext,
			int light,
			int overlay,
			int outlineColors,
			int[] tintLayers,
			List<BakedQuad> quads,
			RenderType renderLayer,
			ItemStackRenderState.FoilType glintType,
			MeshView mesh,
			ItemRenderTypeGetter renderTypeGetter
	) {
		OrderedSubmitNodeCollector queue = order(0);

		if (queue instanceof AccessRenderCommandQueue access) {
			access.fabric_submitItem(matrices, displayContext, light, overlay, outlineColors, tintLayers, quads, renderLayer, glintType, mesh, renderTypeGetter);
		} else {
			queue.submitItem(matrices, displayContext, light, overlay, outlineColors, tintLayers, quads, renderLayer, glintType);
		}
	}
}
