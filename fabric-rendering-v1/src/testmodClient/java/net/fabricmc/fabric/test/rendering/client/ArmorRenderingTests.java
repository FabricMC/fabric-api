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

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.renderer.OrderedSubmitNodeCollector;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.ArmorModelSet;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.ArmorRenderer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;

public class ArmorRenderingTests implements ClientModInitializer {
	private static final Identifier TEXTURE = Identifier.withDefaultNamespace("textures/block/dirt.png");
	private static final String MOD_ID = "fabric-rendering-v1-testmod";

	// Renders a biped model with dirt texture, replacing diamond helmet and diamond chest plate rendering
	// Also makes diamond sword a valid helmet and renders them as dirt helmets. Their default head item rendering is disabled.
	@Override
	public void onInitializeClient() {
		ArmorModelSet<ModelLayerLocation> armorModelData = new ArmorModelSet<>("helmet", "chestplate", "leggings", "boots")
				.map(name -> new ModelLayerLocation(Identifier.fromNamespaceAndPath(MOD_ID, "test_armor"), name));
		EntityModelLayerRegistry.registerEquipmentModelLayers(armorModelData, () -> HumanoidModel.createArmorMeshSet(new CubeDeformation(0.5f), new CubeDeformation(1f)).map(modelData -> LayerDefinition.create(modelData, 64, 32)));
		ArmorRenderer.register(context -> new ArmorRendererTestImpl(context, armorModelData.head()), Items.DIAMOND_HELMET, Items.DIAMOND_SWORD);
		ArmorRenderer.register(context -> new ArmorRendererTestImpl(context, armorModelData.chest()), Items.DIAMOND_CHESTPLATE);
	}

	record ArmorRendererTestImpl(HumanoidModel<HumanoidRenderState> model) implements ArmorRenderer {
		ArmorRendererTestImpl(EntityRendererProvider.Context context, ModelLayerLocation entityModelLayer) {
			this(new HumanoidModel<>(context.bakeLayer(entityModelLayer)));
		}

		@Override
		public void render(PoseStack matrices, SubmitNodeCollector orderedRenderCommandQueue, ItemStack stack, HumanoidRenderState bipedEntityRenderState, EquipmentSlot slot, int light, HumanoidModel<HumanoidRenderState> contextModel) {
			OrderedSubmitNodeCollector renderCommandQueue = orderedRenderCommandQueue.order(0);
			ArmorRenderer.submitTransformCopyingModel(contextModel, bipedEntityRenderState, model, bipedEntityRenderState, false, renderCommandQueue, matrices, RenderTypes.armorCutoutNoCull(TEXTURE), light, OverlayTexture.NO_OVERLAY, 0, null);

			if (stack.hasFoil()) {
				ArmorRenderer.submitTransformCopyingModel(contextModel, bipedEntityRenderState, model, bipedEntityRenderState, false, renderCommandQueue, matrices, RenderTypes.armorEntityGlint(), light, OverlayTexture.NO_OVERLAY, 0, null);
			}
		}

		@Override
		public boolean shouldRenderDefaultHeadItem(LivingEntity entity, ItemStack stack) {
			return !stack.is(Items.DIAMOND_SWORD);
		}
	}
}
