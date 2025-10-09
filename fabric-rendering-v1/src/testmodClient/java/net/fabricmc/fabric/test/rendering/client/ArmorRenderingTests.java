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

import net.minecraft.client.model.Dilation;
import net.minecraft.client.model.TexturedModelData;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.command.RenderCommandQueue;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.client.render.entity.model.EquipmentModelData;
import net.minecraft.client.render.entity.state.BipedEntityRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Identifier;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.ArmorRenderer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;

public class ArmorRenderingTests implements ClientModInitializer {
	private static final Identifier TEXTURE = Identifier.ofVanilla("textures/block/dirt.png");
	private static final String MOD_ID = "fabric-rendering-v1-testmod";

	// Renders a biped model with dirt texture, replacing diamond helmet and diamond chest plate rendering
	// Also makes diamond sword a valid helmet and renders them as dirt helmets. Their default head item rendering is disabled.
	@Override
	public void onInitializeClient() {
		EquipmentModelData<EntityModelLayer> armorModelData = new EquipmentModelData<>("helmet", "chestplate", "leggings", "boots")
				.map(name -> new EntityModelLayer(Identifier.of(MOD_ID, "test_armor"), name));
		EntityModelLayerRegistry.registerEquipmentModelLayers(armorModelData, () -> BipedEntityModel.createEquipmentModelData(new Dilation(0.5f), new Dilation(1f)).map(modelData -> TexturedModelData.of(modelData, 64, 32)));
		ArmorRenderer.register(context -> new ArmorRendererTestImpl(context, armorModelData.head()), Items.DIAMOND_HELMET, Items.DIAMOND_SWORD);
		ArmorRenderer.register(context -> new ArmorRendererTestImpl(context, armorModelData.chest()), Items.DIAMOND_CHESTPLATE);
	}

	record ArmorRendererTestImpl(BipedEntityModel<BipedEntityRenderState> model) implements ArmorRenderer {
		ArmorRendererTestImpl(EntityRendererFactory.Context context, EntityModelLayer entityModelLayer) {
			this(new BipedEntityModel<>(context.getPart(entityModelLayer)));
		}

		@Override
		public void render(MatrixStack matrices, OrderedRenderCommandQueue orderedRenderCommandQueue, ItemStack stack, BipedEntityRenderState bipedEntityRenderState, EquipmentSlot slot, int light, BipedEntityModel<BipedEntityRenderState> contextModel) {
			RenderCommandQueue renderCommandQueue = orderedRenderCommandQueue.getBatchingQueue(0);
			ArmorRenderer.submitTransformCopyingModel(contextModel, bipedEntityRenderState, model, bipedEntityRenderState, false, renderCommandQueue, matrices, RenderLayer.getArmorCutoutNoCull(TEXTURE), light, OverlayTexture.DEFAULT_UV, 0, null);

			if (stack.hasGlint()) {
				ArmorRenderer.submitTransformCopyingModel(contextModel, bipedEntityRenderState, model, bipedEntityRenderState, false, renderCommandQueue, matrices, RenderLayer.getArmorEntityGlint(), light, OverlayTexture.DEFAULT_UV, 0, null);
			}
		}

		@Override
		public boolean shouldRenderDefaultHeadItem(LivingEntity entity, ItemStack stack) {
			return !stack.isOf(Items.DIAMOND_SWORD);
		}
	}
}
