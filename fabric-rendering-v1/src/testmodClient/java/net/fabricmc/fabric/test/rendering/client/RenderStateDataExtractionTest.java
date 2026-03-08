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

import net.fabricmc.fabric.api.client.rendering.v1.LivingEntityRenderLayerRegistrationCallback;

import net.fabricmc.fabric.api.client.rendering.v1.ModelLayerRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.RenderStateDataExtractor;
import net.fabricmc.fabric.api.client.rendering.v1.RenderStateDataKey;

import net.fabricmc.fabric.api.client.rendering.v1.RenderStateExtractionCallback;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.world.entity.Entity;

import net.minecraft.world.entity.LivingEntity;

import org.jspecify.annotations.NonNull;

import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;

import net.fabricmc.api.ClientModInitializer;

/**
 * Renders a small red cube in front of the player's chest when in f5. The cube's
 * transparency is determined by the player's health percentage.
 *
 * <p>
 * Tests:
 * <ul>
 *     <li>{@link RenderStateExtractionCallback}</li>
 * </ul>
 *
 * <p>
 * Assumes the following work as intended:
 * <ul>
 *     <li>{@link ModelLayerRegistry}</li>
 * 	   <li>{@link RenderStateDataKey}</li>
 *     <li>{@link LivingEntityRenderLayerRegistrationCallback}</li>
 * </ul>
 */
public class RenderStateDataExtractionTest implements ClientModInitializer {
	public static final ModelLayerLocation TEST_MODEL_LOCATION = new ModelLayerLocation(Identifier.fromNamespaceAndPath("fabric", "test_rse_model"), "test_rse_model");

	public static final RenderStateDataKey<Float> PLAYER_HEALTH_PERCENTAGE = RenderStateDataKey.create(() -> "Test RSE: Player Health Percentage");

	@Override
	public void onInitializeClient() {
		ModelLayerRegistry.registerModelLayer(TEST_MODEL_LOCATION, TestModel::createLayer);

		LivingEntityRenderLayerRegistrationCallback.EVENT.register((_, entityRenderer, registrationHelper, context) -> {
			if (entityRenderer instanceof AvatarRenderer<?> avatarRenderer) {
				registrationHelper.register(new TestRenderLayer(avatarRenderer, context.getModelSet()));
			}
		});

		RenderStateExtractionCallback.EVENT.register(context -> {
			if (context.renderer() instanceof AvatarRenderer<?>) {
				context.add(TestExtractor::new);
			}
		});
	}

	public static class TestRenderLayer extends RenderLayer<AvatarRenderState, PlayerModel> {
		private final TestModel model;

		public TestRenderLayer(RenderLayerParent<AvatarRenderState, PlayerModel> renderer, final EntityModelSet modelSet) {
			super(renderer);
			this.model = new TestModel(modelSet.bakeLayer(TEST_MODEL_LOCATION));
		}

		@Override
		public void submit(@NonNull PoseStack poseStack, @NonNull SubmitNodeCollector submitNodeCollector, int lightCoords, AvatarRenderState state, float yRot, float xRot) {
			int overlayCoords = LivingEntityRenderer.getOverlayCoords(state, 0.0F);
			float healthPercentage = state.getDataOrDefault(PLAYER_HEALTH_PERCENTAGE, 0f);
			int tint = (int) (healthPercentage * 255) << 24 | 0x00FF0000;
			submitNodeCollector.submitModel(model, state, poseStack, RenderTypes.entityTranslucent(Identifier.fromNamespaceAndPath("minecraft", "textures/block/diamond_block.png")), lightCoords, overlayCoords, tint, null, state.outlineColor, null);
		}
	}

	public static class TestModel extends Model<AvatarRenderState> {
		public TestModel(ModelPart root) {
			super(root, RenderTypes::entityTranslucent);
		}

		public static LayerDefinition createLayer() {
			MeshDefinition mesh = PlayerModel.createMesh(CubeDeformation.NONE, false);
			PartDefinition root = mesh.getRoot().clearRecursively();
			PartDefinition head = root.getChild("body");

			CubeListBuilder cube = CubeListBuilder.create().addBox(-4, 2, -12, 8, 8, 8);
			head.addOrReplaceChild("fabric:test_rse_model", cube, PartPose.ZERO);
			return LayerDefinition.create(mesh, 16, 16);
		}
	}

	public static class TestExtractor extends RenderStateDataExtractor {

		public TestExtractor(EntityRendererProvider.Context context) {
			super(context);
		}

		@Override
		public void extract(Entity entity, EntityRenderState state) {
			LivingEntity livingEntity = (LivingEntity) entity;
			state.setData(PLAYER_HEALTH_PERCENTAGE, livingEntity.getHealth() / livingEntity.getMaxHealth());
		}
	}
}
