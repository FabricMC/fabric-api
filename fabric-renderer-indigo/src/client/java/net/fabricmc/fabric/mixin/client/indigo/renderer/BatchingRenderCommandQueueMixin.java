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

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.command.BatchingRenderCommandQueue;
import net.minecraft.client.render.command.RenderCommandQueue;
import net.minecraft.client.render.item.ItemRenderState;
import net.minecraft.client.render.model.BakedQuad;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemDisplayContext;

import net.fabricmc.fabric.api.renderer.v1.mesh.MeshView;
import net.fabricmc.fabric.impl.client.indigo.renderer.accessor.AccessBatchingRenderCommandQueue;
import net.fabricmc.fabric.impl.client.indigo.renderer.accessor.AccessRenderCommandQueue;
import net.fabricmc.fabric.impl.client.indigo.renderer.render.MeshItemCommand;

@Mixin(BatchingRenderCommandQueue.class)
abstract class BatchingRenderCommandQueueMixin implements RenderCommandQueue, AccessRenderCommandQueue, AccessBatchingRenderCommandQueue {
	@Shadow
	private boolean hasCommands;

	@Unique
	private final List<MeshItemCommand> meshItemCommands = new ArrayList<>();

	@Inject(method = "clear()V", at = @At("RETURN"))
	public void clear(CallbackInfo ci) {
		meshItemCommands.clear();
	}

	@Override
	public void fabric_submitItem(MatrixStack matrices, ItemDisplayContext displayContext, int light, int overlay, int outlineColors, int[] tintLayers, List<BakedQuad> quads, RenderLayer renderLayer, ItemRenderState.Glint glintType, MeshView mesh) {
		hasCommands = true;
		meshItemCommands.add(new MeshItemCommand(matrices.peek().copy(), displayContext, light, overlay, outlineColors, tintLayers, quads, renderLayer, glintType, mesh));
	}

	@Override
	public List<MeshItemCommand> fabric_getMeshItemCommands() {
		return meshItemCommands;
	}
}
