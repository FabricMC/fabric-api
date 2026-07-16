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

package net.fabricmc.fabric.mixin.client.renderer.item;

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
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.world.item.ItemDisplayContext;

import net.fabricmc.fabric.api.client.renderer.v1.Renderer;
import net.fabricmc.fabric.api.client.renderer.v1.mesh.MutableMesh;
import net.fabricmc.fabric.api.client.renderer.v1.mesh.QuadEmitter;
import net.fabricmc.fabric.api.client.renderer.v1.render.FabricLayerRenderState;
import net.fabricmc.fabric.impl.client.renderer.LayerRenderStateExtension;

@Mixin(ItemStackRenderState.LayerRenderState.class)
abstract class ItemStackRenderStateLayerRenderStateMixin implements FabricLayerRenderState, LayerRenderStateExtension {
	@Unique
	@Nullable
	private MutableMesh mutableMesh;

	@Override
	public QuadEmitter emitter() {
		if (mutableMesh == null) {
			mutableMesh = Renderer.get().mutableMesh();
		}

		return mutableMesh.emitter();
	}

	@Override
	@Nullable
	public MutableMesh fabric_getMutableMesh() {
		return mutableMesh;
	}

	@Inject(method = "clear()V", at = @At("RETURN"))
	private void onReturnClear(CallbackInfo ci) {
		if (mutableMesh != null) {
			mutableMesh.clear();
		}
	}

	@Redirect(method = "submit", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/SubmitNodeCollector;submitItem(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/world/item/ItemDisplayContext;III[ILjava/util/List;Lnet/minecraft/client/renderer/item/ItemStackRenderState$FoilType;)V"))
	private void submitItemProxy(
			SubmitNodeCollector submitNodeCollector,
			PoseStack poseStack,
			ItemDisplayContext displayContext,
			int light,
			int overlay,
			int outlineColor,
			int[] tints,
			List<BakedQuad> quads,
			ItemStackRenderState.FoilType foilType
	) {
		if (mutableMesh != null && mutableMesh.size() > 0) {
			// We don't have to copy the mesh here because vanilla doesn't copy the quad list either.
			submitNodeCollector.submitItem(poseStack, displayContext, light, overlay, outlineColor, tints, quads,
					mutableMesh, foilType);
		} else {
			submitNodeCollector.submitItem(poseStack, displayContext, light, overlay, outlineColor, tints, quads,
					foilType);
		}
	}
}
