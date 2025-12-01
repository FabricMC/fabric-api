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

import net.minecraft.client.model.object.armorstand.ArmorStandArmorModel;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.ArmorStandRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.client.renderer.entity.state.ArmorStandRenderState;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.LivingEntityFeatureRendererRegistrationCallback;

/**
 * This test exists solely for testing generics.
 * As such it is not in the mod json
 */
public class FeatureRendererGenericTests implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		// These aren't tests in the normal sense. These exist to test that generics are sane.
		LivingEntityFeatureRendererRegistrationCallback.EVENT.register((entityType, entityRenderer, registrationHelper, context) -> {
			if (entityRenderer instanceof AvatarRenderer) {
				registrationHelper.register(new TestPlayerFeature((AvatarRenderer) entityRenderer));

				// This is T extends AbstractClientPlayerEntity
				registrationHelper.register(new GenericTestPlayerFeature<>((AvatarRenderer) entityRenderer));
			}

			if (entityRenderer instanceof ArmorStandRenderer) {
				registrationHelper.register(new TestArmorStandFeature((ArmorStandRenderer) entityRenderer));
			}

			// Obviously not recommended, just used for testing generics
			// TODO 1.21.2
			// registrationHelper.register(new ElytraFeatureRenderer<>(entityRenderer, context.getModelLoader()));

			if (entityRenderer instanceof HumanoidMobRenderer) {
				// It works, method ref is encouraged
				registrationHelper.register(new ItemInHandLayer<>((HumanoidMobRenderer<?, ?, ?>) entityRenderer));
			}
		});

		LivingEntityFeatureRendererRegistrationCallback.EVENT.register(this::registerFeatures);
	}

	private void registerFeatures(EntityType<? extends LivingEntity> entityType, LivingEntityRenderer<?, ?, ?> entityRenderer, LivingEntityFeatureRendererRegistrationCallback.RegistrationHelper registrationHelper, EntityRendererProvider.Context context) {
		if (entityRenderer instanceof AvatarRenderer playerEntityRenderer) {
			registrationHelper.register(new TestPlayerFeature(playerEntityRenderer));

			// This is T extends AbstractClientPlayerEntity
			registrationHelper.register(new GenericTestPlayerFeature<>(playerEntityRenderer));
		}

		if (entityRenderer instanceof ArmorStandRenderer armorStandEntityRenderer) {
			registrationHelper.register(new TestArmorStandFeature(armorStandEntityRenderer));
		}

		// Obviously not recommended, just used for testing generics.
		// TODO 1.21.2
		// registrationHelper.register(new ElytraFeatureRenderer<>(entityRenderer, context.getModelLoader()));

		if (entityRenderer instanceof HumanoidMobRenderer<?, ?, ?> bipedEntityRenderer) {
			// It works, method ref is encouraged
			registrationHelper.register(new ItemInHandLayer<>(bipedEntityRenderer));
		}
	}

	static class TestPlayerFeature extends RenderLayer<AvatarRenderState, PlayerModel> {
		TestPlayerFeature(RenderLayerParent<AvatarRenderState, PlayerModel> featureRendererContext) {
			super(featureRendererContext);
		}

		@Override
		public void submit(PoseStack matrices, SubmitNodeCollector commandQueue, int light, AvatarRenderState state, float limbAngle, float limbDistance) {
		}
	}

	static class GenericTestPlayerFeature<T extends AvatarRenderState, M extends PlayerModel> extends RenderLayer<T, M> {
		GenericTestPlayerFeature(RenderLayerParent<T, M> featureRendererContext) {
			super(featureRendererContext);
		}

		@Override
		public void submit(PoseStack matrices, SubmitNodeCollector commandQueue, int light, AvatarRenderState state, float limbAngle, float limbDistance) {
		}
	}

	static class TestArmorStandFeature extends RenderLayer<ArmorStandRenderState, ArmorStandArmorModel> {
		TestArmorStandFeature(RenderLayerParent<ArmorStandRenderState, ArmorStandArmorModel> context) {
			super(context);
		}

		@Override
		public void submit(PoseStack matrices, SubmitNodeCollector commandQueue, int light, ArmorStandRenderState state, float limbAngle, float limbDistance) {
		}
	}
}
