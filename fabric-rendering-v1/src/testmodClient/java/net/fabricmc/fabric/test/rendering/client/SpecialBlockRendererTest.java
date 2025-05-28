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

import java.util.Set;

import com.mojang.serialization.MapCodec;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import net.minecraft.block.Blocks;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.model.AllayEntityModel;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.model.LoadedEntityModels;
import net.minecraft.client.render.item.model.special.SpecialModelRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemDisplayContext;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.math.RotationAxis;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.SpecialBlockRendererRegistry;

/**
 * Tests {@link SpecialBlockRendererRegistry} by rendering an allay model above TNT blocks in a minecart.
 */
public class SpecialBlockRendererTest implements ClientModInitializer {
	private static final Identifier ALLAY_TEXTURE = Identifier.ofVanilla("textures/entity/allay/allay.png");

	@Override
	public void onInitializeClient() {
		SpecialBlockRendererRegistry.register(Blocks.TNT, new SpecialModelRenderer.Unbaked() {
			@Override
			public SpecialModelRenderer<?> bake(LoadedEntityModels entityModels) {
				AllayEntityModel allayModel = new AllayEntityModel(entityModels.getModelPart(EntityModelLayers.ALLAY));

				return new SpecialModelRenderer<>() {
					@Override
					public void render(@Nullable Object data, ItemDisplayContext displayContext, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay, boolean glint) {
						matrices.push();
						matrices.translate(0.5f, 0.0f, 0.5f);
						matrices.translate(0, 1.46875f, 0);
						matrices.scale(1, -1, 1);
						matrices.multiply(RotationAxis.POSITIVE_Y.rotation((float) (Util.getMeasuringTimeMs() * 0.001)));
						matrices.translate(0, -1.46875f, 0);
						VertexConsumer vertexConsumer = vertexConsumers.getBuffer(allayModel.getLayer(ALLAY_TEXTURE));
						allayModel.render(matrices, vertexConsumer, light, overlay);
						matrices.pop();
					}

					@Override
					public void method_72175(Set<Vector3f> set) {
					}

					@Override
					@Nullable
					public Object getData(ItemStack stack) {
						return null;
					}
				};
			}

			@Override
			public MapCodec<? extends SpecialModelRenderer.Unbaked> getCodec() {
				return null;
			}
		});
	}
}
