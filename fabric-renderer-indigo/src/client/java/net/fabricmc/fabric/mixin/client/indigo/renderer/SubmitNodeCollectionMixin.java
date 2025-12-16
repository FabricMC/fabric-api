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

import net.fabricmc.fabric.api.renderer.v1.mesh.MeshView;
import net.fabricmc.fabric.api.renderer.v1.render.ItemRenderTypeGetter;
import net.fabricmc.fabric.impl.client.indigo.renderer.accessor.AccessBatchingRenderCommandQueue;
import net.fabricmc.fabric.impl.client.indigo.renderer.accessor.AccessRenderCommandQueue;
import net.fabricmc.fabric.impl.client.indigo.renderer.render.MeshItemCommand;

@Mixin(SubmitNodeCollection.class)
abstract class SubmitNodeCollectionMixin implements OrderedSubmitNodeCollector, AccessRenderCommandQueue, AccessBatchingRenderCommandQueue {
	@Shadow
	private boolean wasUsed;

	@Unique
	private final List<MeshItemCommand> meshItemCommands = new ArrayList<>();

	@Inject(method = "clear()V", at = @At("RETURN"))
	public void clear(CallbackInfo ci) {
		meshItemCommands.clear();
	}

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
			@Nullable ItemRenderTypeGetter renderTypeGetter
	) {
		wasUsed = true;
		meshItemCommands.add(new MeshItemCommand(
				matrices.last().copy(),
				displayContext,
				light,
				overlay,
				outlineColors,
				tintLayers,
				quads,
				renderLayer,
				glintType,
				mesh,
				renderTypeGetter
		));
	}

	@Override
	public List<MeshItemCommand> fabric_getMeshItemCommands() {
		return meshItemCommands;
	}
}
