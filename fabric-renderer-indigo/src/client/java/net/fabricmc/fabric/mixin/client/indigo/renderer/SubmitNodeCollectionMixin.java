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

import java.util.ArrayList;
import java.util.List;

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
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.world.item.ItemDisplayContext;

import net.fabricmc.fabric.api.client.renderer.v1.mesh.MeshView;
import net.fabricmc.fabric.api.client.renderer.v1.render.ItemRenderTypeGetter;
import net.fabricmc.fabric.impl.client.indigo.renderer.accessor.AccessOrderedSubmitNodeCollector;
import net.fabricmc.fabric.impl.client.indigo.renderer.accessor.AccessSubmitNodeCollection;
import net.fabricmc.fabric.impl.client.indigo.renderer.render.MeshItemSubmit;

@Mixin(SubmitNodeCollection.class)
abstract class SubmitNodeCollectionMixin implements OrderedSubmitNodeCollector, AccessOrderedSubmitNodeCollector, AccessSubmitNodeCollection {
	@Shadow
	private boolean wasUsed;

	@Unique
	private final List<MeshItemSubmit> meshItemSubmits = new ArrayList<>();

	@Inject(method = "clear()V", at = @At("RETURN"))
	public void clear(CallbackInfo ci) {
		meshItemSubmits.clear();
	}

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
			@Nullable ItemRenderTypeGetter renderTypeGetter
	) {
		wasUsed = true;
		meshItemSubmits.add(new MeshItemSubmit(
				poseStack.last().copy(),
				displayContext,
				light,
				overlay,
				outlineColors,
				tintLayers,
				quads,
				renderType,
				foilType,
				mesh,
				renderTypeGetter
		));
	}

	@Override
	public List<MeshItemSubmit> fabric_getMeshItemSubmits() {
		return meshItemSubmits;
	}
}
