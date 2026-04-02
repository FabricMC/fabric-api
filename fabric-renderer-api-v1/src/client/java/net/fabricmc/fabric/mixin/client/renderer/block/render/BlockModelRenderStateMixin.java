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

package net.fabricmc.fabric.mixin.client.renderer.block.render;

import static net.minecraft.client.renderer.block.BlockModelRenderState.EMPTY_TINTS;

import java.util.Collections;
import java.util.List;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.mojang.blaze3d.vertex.PoseStack;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.joml.Matrix4fc;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.BlockModelRenderState;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.util.RandomSource;

import net.fabricmc.fabric.api.client.renderer.v1.Renderer;
import net.fabricmc.fabric.api.client.renderer.v1.mesh.Mesh;
import net.fabricmc.fabric.api.client.renderer.v1.mesh.MutableMesh;
import net.fabricmc.fabric.api.client.renderer.v1.mesh.QuadEmitter;
import net.fabricmc.fabric.api.client.renderer.v1.render.FabricBlockModelRenderState;

@Mixin(BlockModelRenderState.class)
public abstract class BlockModelRenderStateMixin implements FabricBlockModelRenderState {
	@Shadow
	@Nullable
	private List<BlockStateModelPart> modelParts;
	@Shadow
	@Nullable
	private Matrix4fc transformation;
	@Shadow
	@Nullable
	private RenderType renderType;
	@Shadow
	@Nullable
	private IntList tintLayers;
	@Shadow
	@Nullable
	private RandomSource randomSource;

	@Unique
	@Nullable
	private MutableMesh mesh;

	@Shadow
	@Nullable
	private static Matrix4fc identityToNull(Matrix4fc transformation) {
		return null;
	}

	@Override
	public QuadEmitter setupMesh(Matrix4fc transformation, boolean hasTranslucency) {
		this.transformation = identityToNull(transformation);
		renderType = hasTranslucency ? Sheets.translucentBlockSheet() : Sheets.cutoutBlockSheet();

		if (mesh == null) {
			mesh = Renderer.get().mutableMesh();
		} else {
			mesh.clear();
		}

		if (modelParts != null) {
			modelParts.clear();
		}

		return mesh.emitter();
	}

	@Inject(method = "clear()V", at = @At("RETURN"))
	private void onReturnClear(CallbackInfo ci) {
		mesh = null;
	}

	@ModifyReturnValue(method = "isEmpty()Z", at = @At("RETURN"))
	private boolean modifyIsEmpty(boolean original) {
		return original && mesh == null;
	}

	@Inject(method = "setupModel", at = @At("RETURN"))
	private void onReturnSetupModel(CallbackInfoReturnable<List<BlockStateModelPart>> cir) {
		if (mesh != null) {
			mesh.clear();
		}
	}

	// TODO: improve this injection or use a second submit for just the mesh
	@Inject(method = "submitModel", at = @At("HEAD"), cancellable = true)
	private void submitMesh(RenderType renderType, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int lightCoords, int overlayCoords, int outlineColor, CallbackInfo ci) {
		if (mesh != null && mesh.size() > 0) {
			List<BlockStateModelPart> modelPartsCopy = modelParts != null && !modelParts.isEmpty() ? new ObjectArrayList<>(modelParts) : Collections.emptyList();
			Mesh meshCopy = mesh.immutableCopy();
			int[] tints = tintLayers != null ? tintLayers.toArray(EMPTY_TINTS) : EMPTY_TINTS;

			if (transformation != null) {
				poseStack.pushPose();
				poseStack.mulPose(transformation);
				submitNodeCollector.submitBlockModel(poseStack, _ -> renderType, renderType.hasBlending(), modelPartsCopy, meshCopy, tints, lightCoords, overlayCoords, outlineColor);
				poseStack.popPose();
			} else {
				submitNodeCollector.submitBlockModel(poseStack, _ -> renderType, renderType.hasBlending(), modelPartsCopy, meshCopy, tints, lightCoords, overlayCoords, outlineColor);
			}

			ci.cancel();
		}
	}
}
