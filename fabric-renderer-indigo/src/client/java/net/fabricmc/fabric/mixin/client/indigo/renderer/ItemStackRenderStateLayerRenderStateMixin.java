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
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.world.item.ItemDisplayContext;

import net.fabricmc.fabric.api.renderer.v1.render.FabricLayerRenderState;
import net.fabricmc.fabric.api.renderer.v1.render.ItemRenderTypeGetter;
import net.fabricmc.fabric.impl.client.indigo.renderer.accessor.AccessLayerRenderState;
import net.fabricmc.fabric.impl.client.indigo.renderer.accessor.AccessRenderCommandQueue;
import net.fabricmc.fabric.impl.client.indigo.renderer.mesh.MutableMeshImpl;

@Mixin(value = ItemStackRenderState.LayerRenderState.class)
abstract class ItemStackRenderStateLayerRenderStateMixin implements FabricLayerRenderState, AccessLayerRenderState {
	@Unique
	private final MutableMeshImpl mutableMesh = new MutableMeshImpl();

	@Unique
	@Nullable
	private ItemRenderTypeGetter renderTypeGetter = null;

	@Inject(method = "clear()V", at = @At("RETURN"))
	private void onReturnClear(CallbackInfo ci) {
		mutableMesh.clear();
		renderTypeGetter = null;
	}

	@Redirect(method = "submit", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/SubmitNodeCollector;submitItem(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/world/item/ItemDisplayContext;III[ILjava/util/List;Lnet/minecraft/client/renderer/rendertype/RenderType;Lnet/minecraft/client/renderer/item/ItemStackRenderState$FoilType;)V"))
	private void submitItemProxy(SubmitNodeCollector commandQueue, PoseStack matrices, ItemDisplayContext displayContext, int light, int overlay, int outlineColor, int[] tints, List<BakedQuad> quads, RenderType layer, ItemStackRenderState.FoilType glint) {
		if (mutableMesh.size() > 0 && commandQueue instanceof AccessRenderCommandQueue access) {
			// We don't have to copy the mesh here because vanilla doesn't copy the tint array or quad list either.
			access.fabric_submitItem(matrices, displayContext, light, overlay, outlineColor, tints, quads, layer, glint, mutableMesh, renderTypeGetter);
		} else {
			commandQueue.submitItem(matrices, displayContext, light, overlay, outlineColor, tints, quads, layer, glint);
		}
	}

	@Override
	public MutableMeshImpl fabric_getMutableMesh() {
		return mutableMesh;
	}

	@Override
	public void fabric_setRenderTypeGetter(ItemRenderTypeGetter renderTypeGetter) {
		this.renderTypeGetter = renderTypeGetter;
	}
}
