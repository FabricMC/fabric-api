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

import org.joml.AxisAngle4f;
import org.joml.Quaternionf;

import net.minecraft.block.Blocks;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.render.entity.state.LivingEntityRenderState;
import net.minecraft.client.render.model.BlockStateModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.EmptyBlockRenderView;

import net.fabricmc.fabric.api.renderer.v1.render.FabricBlockModelRenderer;
import net.fabricmc.fabric.api.renderer.v1.render.RenderLayerHelper;

public class BakedModelFeatureRenderer<S extends LivingEntityRenderState, M extends EntityModel<S>> extends FeatureRenderer<S, M> {
	private final Supplier<BlockStateModel> modelSupplier;

	public BakedModelFeatureRenderer(FeatureRendererContext<S, M> context, Supplier<BlockStateModel> modelSupplier) {
		super(context);
		this.modelSupplier = modelSupplier;
	}

	@Override
	public void render(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, S state, float limbAngle, float limbDistance) {
		BlockStateModel model = modelSupplier.get();
		matrices.push();
		matrices.multiply(new Quaternionf(new AxisAngle4f(state.age * 0.07F - state.bodyYaw * MathHelper.RADIANS_PER_DEGREE, 0, 1, 0)));
		matrices.scale(-0.75F, -0.75F, 0.75F);
		float aboveHead = (float) (Math.sin(state.age * 0.08F)) * 0.5F + 0.5F;
		matrices.translate(-0.5F, 0.75F + aboveHead, -0.5F);
		FabricBlockModelRenderer.render(matrices.peek(), layer -> vertexConsumers.getBuffer(RenderLayerHelper.getEntityBlockLayer(layer)), model, 1, 1, 1, light, OverlayTexture.DEFAULT_UV, EmptyBlockRenderView.INSTANCE, BlockPos.ORIGIN, Blocks.AIR.getDefaultState());
		matrices.pop();
	}
}
