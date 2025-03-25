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

import net.minecraft.client.render.item.ItemRenderState;

import net.fabricmc.fabric.impl.client.indigo.renderer.accessor.AccessLayerRenderState;
import net.fabricmc.fabric.impl.client.indigo.renderer.mesh.MutableMeshImpl;
import net.fabricmc.fabric.impl.client.indigo.renderer.render.QuadToPosPipe;

@Mixin(ItemRenderState.class)
abstract class ItemRenderStateMixin {
	@Inject(method = "load(Ljava/util/function/Consumer;)V", at = @At(value = "NEW", target = "net/minecraft/client/util/math/MatrixStack$Entry"))
	private void afterInitVecLoad(Consumer<Vector3fc> posConsumer, CallbackInfo ci, @Local Vector3f vec, @Share("pipe") LocalRef<QuadToPosPipe> pipeRef) {
		pipeRef.set(new QuadToPosPipe(posConsumer, vec));
	}

	@Inject(method = "load(Ljava/util/function/Consumer;)V", at = @At(value = "INVOKE", target = "net/minecraft/client/util/math/MatrixStack$Entry.loadIdentity()V", shift = At.Shift.BEFORE))
	private void afterLayerLoad(Consumer<Vector3fc> posConsumer, CallbackInfo ci, @Local(ordinal = 0) Vector3f vec, @Local ItemRenderState.LayerRenderState layer, @Local Matrix4f matrix, @Share("pipe") LocalRef<QuadToPosPipe> pipeRef) {
		MutableMeshImpl mutableMesh = ((AccessLayerRenderState) layer).fabric_getMutableMesh();

		if (mutableMesh.size() > 0) {
			QuadToPosPipe pipe = pipeRef.get();
			pipe.matrix = matrix;
			// Use the mutable version here as it does not use a ThreadLocal or cursor stack
			mutableMesh.forEachMutable(pipe);
		}
	}
}
