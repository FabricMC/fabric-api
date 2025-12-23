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

package net.fabricmc.fabric.test.rendering.client;

import java.util.function.Consumer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.mojang.serialization.MapCodec;
import org.joml.Vector3fc;
import org.jspecify.annotations.Nullable;

import net.minecraft.client.model.animal.allay.AllayModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Util;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.SpecialBlockRendererRegistry;

/**
 * Tests {@link SpecialBlockRendererRegistry} by rendering an allay model above TNT blocks in a minecart.
 */
public class SpecialBlockRendererTest implements ClientModInitializer {
	private static final Identifier ALLAY_TEXTURE = Identifier.withDefaultNamespace("textures/entity/allay/allay.png");

	@Override
	public void onInitializeClient() {
		SpecialBlockRendererRegistry.register(Blocks.TNT, new SpecialModelRenderer.Unbaked() {
			@Override
			public SpecialModelRenderer<?> bake(SpecialModelRenderer.BakingContext ctx) {
				AllayModel allayModel = new AllayModel(ctx.entityModelSet().bakeLayer(ModelLayers.ALLAY));

				return new SpecialModelRenderer<>() {
					@Override
					public void submit(@Nullable Object data, ItemDisplayContext displayContext, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int light, int overlay, boolean glint, int outlineColor) {
						poseStack.pushPose();
						poseStack.translate(0.5f, 0.0f, 0.5f);
						poseStack.translate(0, 1.46875f, 0);
						poseStack.scale(1, -1, 1);
						poseStack.mulPose(Axis.YP.rotation((float) (Util.getMillis() * 0.001)));
						poseStack.translate(0, -1.46875f, 0);
						submitNodeCollector.order(0)
								.submitCustomGeometry(poseStack, RenderTypes.solidMovingBlock(), (matricesEntry, vertexConsumer) -> allayModel.renderToBuffer(poseStack, vertexConsumer, light, overlay));
						poseStack.popPose();
					}

					@Override
					public void getExtents(Consumer<Vector3fc> consumer) { }

					@Override
					@Nullable
					public Object extractArgument(ItemStack stack) {
						return null;
					}
				};
			}

			@Override
			public MapCodec<? extends SpecialModelRenderer.Unbaked> type() {
				return null;
			}
		});
	}
}
