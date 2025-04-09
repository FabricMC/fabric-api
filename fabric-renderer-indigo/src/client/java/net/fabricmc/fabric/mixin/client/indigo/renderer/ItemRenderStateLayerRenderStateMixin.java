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

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.item.ItemRenderState;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.BakedQuad;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemDisplayContext;

import net.fabricmc.fabric.api.renderer.v1.render.FabricLayerRenderState;
import net.fabricmc.fabric.impl.client.indigo.renderer.accessor.AccessLayerRenderState;
import net.fabricmc.fabric.impl.client.indigo.renderer.mesh.MutableMeshImpl;
import net.fabricmc.fabric.impl.client.indigo.renderer.render.ItemRenderContext;

@Mixin(value = ItemRenderState.LayerRenderState.class)
abstract class ItemRenderStateLayerRenderStateMixin implements FabricLayerRenderState, AccessLayerRenderState {
	@Unique
	private final MutableMeshImpl mutableMesh = new MutableMeshImpl();

	@Inject(method = "clear()V", at = @At("RETURN"))
	private void onReturnClear(CallbackInfo ci) {
		mutableMesh.clear();
	}

	@Redirect(method = "render", at = @At(value = "INVOKE", target = "net/minecraft/client/render/item/ItemRenderer.renderItem(Lnet/minecraft/item/ItemDisplayContext;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;II[ILjava/util/List;Lnet/minecraft/client/render/RenderLayer;Lnet/minecraft/client/render/item/ItemRenderState$Glint;)V"))
	private void renderItemProxy(ItemDisplayContext displayContext, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay, int[] tints, List<BakedQuad> quads, RenderLayer layer, ItemRenderState.Glint glint) {
		if (mutableMesh.size() > 0) {
			ItemRenderContext.POOL.get().renderItem(displayContext, matrices, vertexConsumers, light, overlay, tints, quads, mutableMesh, layer, glint);
		} else {
			ItemRenderer.renderItem(displayContext, matrices, vertexConsumers, light, overlay, tints, quads, layer, glint);
		}
	}

	@Override
	public MutableMeshImpl fabric_getMutableMesh() {
		return mutableMesh;
	}
}
