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

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayers;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.command.RenderCommandQueue;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.state.BipedEntityRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Identifier;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.ArmorRenderer;

public class ArmorRenderingTests implements ClientModInitializer {
	private BipedEntityModel<BipedEntityRenderState> armorModel;
	private final Identifier texture = Identifier.ofVanilla("textures/block/dirt.png");

	// Renders a biped model with dirt texture, replacing diamond helmet and diamond chest plate rendering
	// Also makes diamond sword a valid helmet and renders them as dirt helmets. Their default head item rendering is disabled.
	@Override
	public void onInitializeClient() {
		ArmorRenderer armorRenderer = new ArmorRenderer() {
			@Override
			public void render(MatrixStack matrices, OrderedRenderCommandQueue orderedRenderCommandQueue, ItemStack stack, BipedEntityRenderState renderState, EquipmentSlot slot, int light, BipedEntityModel<BipedEntityRenderState> contextModel) {
				if (armorModel == null) {
					armorModel = new BipedEntityModel<>(MinecraftClient.getInstance().getLoadedEntityModels().getModelPart(EntityModelLayers.PLAYER));
				}

				armorModel.setAngles(renderState);
				armorModel.setVisible(false);
				armorModel.body.visible = slot == EquipmentSlot.CHEST;
				armorModel.leftArm.visible = slot == EquipmentSlot.CHEST;
				armorModel.rightArm.visible = slot == EquipmentSlot.CHEST;
				armorModel.head.visible = slot == EquipmentSlot.HEAD;

				RenderCommandQueue renderCommandQueue = orderedRenderCommandQueue.getBatchingQueue(0);
				renderCommandQueue.submitModel(armorModel, renderState, matrices, RenderLayers.armorCutoutNoCull(texture), light, OverlayTexture.DEFAULT_UV, 0xFFFFFFFF, null, 0, null);

				if (stack.hasGlint()) {
					renderCommandQueue.submitModel(armorModel, renderState, matrices, RenderLayers.armorEntityGlint(), light, OverlayTexture.DEFAULT_UV, 0xFFFFFFFF, null, 0, null);
				}
			}

			@Override
			public boolean shouldRenderDefaultHeadItem(LivingEntity entity, ItemStack stack) {
				return !stack.isOf(Items.DIAMOND_SWORD);
			}
		};

		ArmorRenderer.register(armorRenderer, Items.DIAMOND_HELMET, Items.DIAMOND_CHESTPLATE, Items.DIAMOND_SWORD);
	}
}
