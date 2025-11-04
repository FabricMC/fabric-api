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
import org.joml.Quaternionf;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.model.BlockStateModel;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.Mth;

public class BakedModelFeatureRenderer<S extends LivingEntityRenderState, M extends EntityModel<S>> extends RenderLayer<S, M> {
	private final Supplier<BlockStateModel> modelSupplier;

	public BakedModelFeatureRenderer(RenderLayerParent<S, M> context, Supplier<BlockStateModel> modelSupplier) {
		super(context);
		this.modelSupplier = modelSupplier;
	}

	@Override
	public void submit(PoseStack matrices, SubmitNodeCollector commandQueue, int light, S state, float limbAngle, float limbDistance) {
		BlockStateModel model = modelSupplier.get();
		matrices.pushPose();
		matrices.mulPose(new Quaternionf(new AxisAngle4f(state.ageInTicks * 0.07F - state.bodyRot * Mth.DEG_TO_RAD, 0, 1, 0)));
		matrices.scale(-0.75F, -0.75F, 0.75F);
		float aboveHead = (float) (Math.sin(state.ageInTicks * 0.08F)) * 0.5F + 0.5F;
		matrices.translate(-0.5F, 0.75F + aboveHead, -0.5F);
		// FIXME 1.21.9
		// FabricBlockModelRenderer.render(matrices.peek(), RenderLayerHelper.entityDelegate(vertexConsumers), model, 1, 1, 1, light, OverlayTexture.DEFAULT_UV, EmptyBlockRenderView.INSTANCE, BlockPos.ORIGIN, Blocks.AIR.getDefaultState());
		commandQueue.order(0).submitBlockModel(matrices, Sheets.cutoutBlockSheet(), model, 1, 1, 1, light, OverlayTexture.NO_OVERLAY, 0);
		matrices.popPose();
	}
}
