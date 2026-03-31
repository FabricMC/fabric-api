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

import java.util.function.Consumer;

import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.renderer.item.ItemStackRenderState;

import net.fabricmc.fabric.api.client.renderer.v1.mesh.MutableMesh;
import net.fabricmc.fabric.impl.client.renderer.LayerRenderStateExtension;
import net.fabricmc.fabric.impl.client.renderer.QuadToPosPipe;

@Mixin(ItemStackRenderState.class)
abstract class ItemStackRenderStateMixin {
	@Inject(method = "visitExtents(Ljava/util/function/Consumer;)V", at = @At(value = "NEW", target = "com/mojang/blaze3d/vertex/PoseStack$Pose"))
	private void afterInitVecLoad(Consumer<Vector3fc> posConsumer, CallbackInfo ci, @Local(name = "scratch") Vector3f scratch, @Share("pipe") LocalRef<QuadToPosPipe> pipeRef) {
		pipeRef.set(new QuadToPosPipe(posConsumer, scratch));
	}

	@Inject(method = "visitExtents(Ljava/util/function/Consumer;)V", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/PoseStack$Pose;setIdentity()V"))
	private void afterLayerLoad(Consumer<Vector3fc> posConsumer, CallbackInfo ci, @Local(name = "scratch") Vector3f vec, @Local(name = "layer") ItemStackRenderState.LayerRenderState layer, @Local(name = "poseTransform") Matrix4f matrix, @Share("pipe") LocalRef<QuadToPosPipe> pipeRef) {
		MutableMesh mutableMesh = ((LayerRenderStateExtension) layer).fabric_getMutableMesh();

		if (mutableMesh != null && mutableMesh.size() > 0) {
			QuadToPosPipe pipe = pipeRef.get();
			pipe.matrix = matrix;
			// Use the mutable version here as it does not use a ThreadLocal or cursor stack, at least in Indigo
			mutableMesh.forEachMutable(pipe);
		}
	}
}
