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

package net.fabricmc.fabric.test.model.loading;

import java.util.function.Supplier;

import com.mojang.blaze3d.vertex.PoseStack;
import org.joml.AxisAngle4f;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Quaternionf;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.BlockModelRenderState;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.model.BlockDisplayContext;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.Blocks;

import net.fabricmc.fabric.api.client.renderer.v1.mesh.QuadEmitter;

public class BakedModelRenderLayer<S extends LivingEntityRenderState, M extends EntityModel<S>> extends RenderLayer<S, M> {
	private static final Matrix4fc IDENTITY_MATRIX4FC = new Matrix4f();
	public static final BlockDisplayContext BLOCK_DISPLAY_CONTEXT = BlockDisplayContext.create();

	private final Supplier<BlockStateModel> modelSupplier;

	public BakedModelRenderLayer(RenderLayerParent<S, M> context, Supplier<BlockStateModel> modelSupplier) {
		super(context);
		this.modelSupplier = modelSupplier;
	}

	@Override
	public void submit(PoseStack poseStack, SubmitNodeCollector nodeCollector, int light, S state, float limbAngle, float limbDistance) {
		BlockStateModel model = modelSupplier.get();
		poseStack.pushPose();
		poseStack.mulPose(new Quaternionf(new AxisAngle4f(state.ageInTicks * 0.07F - state.bodyRot * Mth.DEG_TO_RAD, 0, 1, 0)));
		poseStack.scale(-0.75F, -0.75F, 0.75F);
		float aboveHead = (float) (Math.sin(state.ageInTicks * 0.08F)) * 0.5F + 0.5F;
		poseStack.translate(-0.5F, 0.75F + aboveHead, -0.5F);

		// Normally the BlockModelRenderState would be stored in the entity render state and it
		// would be populated in the entity renderer's extractRenderState method, but that doesn't
		// seem possible to do without mixins for this case
		BlockModelRenderState renderState = new BlockModelRenderState();
		QuadEmitter emitter = renderState.setupMesh(IDENTITY_MATRIX4FC, model.hasMaterialFlag(BakedQuad.FLAG_TRANSLUCENT));
		model.emitQuads(
				emitter,
				BlockAndTintGetter.EMPTY,
				BlockPos.ZERO,
				Blocks.AIR.defaultBlockState(),
				renderState.scratchRandomSource(42L),
				_ -> false
		);
		renderState.submit(poseStack, nodeCollector, light, OverlayTexture.NO_OVERLAY, 0);

		poseStack.popPose();
	}
}
