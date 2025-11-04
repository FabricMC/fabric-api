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

package net.fabricmc.fabric.test.particle.client;

import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.client.particle.ParticleGroup;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.state.ParticleGroupRenderState;
import net.minecraft.client.renderer.state.QuadParticleRenderState;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.particle.v1.FabricSpriteProvider;
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;
import net.fabricmc.fabric.api.client.particle.v1.ParticleRendererRegistry;
import net.fabricmc.fabric.api.particle.v1.FabricParticleTypes;

public class ParticleRendererRegistryTests implements ClientModInitializer {
	private static final Identifier PARTICLE_ID = Identifier.fromNamespaceAndPath("fabric-particles-v1-testmod", "test");
	private static final SimpleParticleType TEST_PARTICLE_TYPE = FabricParticleTypes.simple();
	private static final ParticleRenderType TEST_PARTICLE_TEXTURE_SHEET = new ParticleRenderType(PARTICLE_ID.toString());

	@Override
	public void onInitializeClient() {
		Registry.register(BuiltInRegistries.PARTICLE_TYPE, PARTICLE_ID, TEST_PARTICLE_TYPE);
		ParticleFactoryRegistry.getInstance().register(TEST_PARTICLE_TYPE, TestParticleFactory::new);

		ParticleRendererRegistry.register(TEST_PARTICLE_TEXTURE_SHEET, TestParticleRenderer::new);
		ParticleRendererRegistry.registerOrdering(TEST_PARTICLE_TEXTURE_SHEET, ParticleRenderType.ITEM_PICKUP);

		ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) ->
				dispatcher.register(ClientCommandManager.literal("custom_particles").executes(context -> {
					ClientLevel world = Minecraft.getInstance().level;
					RandomSource random = world.getRandom();
					LocalPlayer player = context.getSource().getPlayer();

					for (int i = 0; i < 35; i++) {
						world.addParticle(
								TEST_PARTICLE_TYPE,
								player.getX(), player.getY(), player.getZ(),
								Mth.randomBetween(random, -1.0F, 1.0F),
								0.5F,
								Mth.randomBetween(random, -1.0F, 1.0F)
						);
					}

					return 1;
				})));
	}

	private record TestParticleFactory(FabricSpriteProvider spriteProvider) implements ParticleProvider<SimpleParticleType> {
		@Override
		public Particle createParticle(SimpleParticleType parameters, ClientLevel world, double x, double y, double z, double velocityX, double velocityY, double velocityZ, RandomSource random) {
			return new TestParticle(world, x, y, z, velocityX, velocityY, velocityZ, spriteProvider.get(random));
		}
	}

	private static class TestParticle extends SingleQuadParticle {
		TestParticle(ClientLevel world, double x, double y, double z, double velocityX, double velocityY, double velocityZ, TextureAtlasSprite sprite) {
			super(world, x, y, z, velocityX, velocityY, velocityZ, sprite);
		}

		@Override
		protected Layer getLayer() {
			return Layer.OPAQUE;
		}

		@Override
		public ParticleRenderType getGroup() {
			return TEST_PARTICLE_TEXTURE_SHEET;
		}

		private boolean intersectPoint(Frustum frustum) {
			return frustum.pointInFrustum(x, y, z);
		}
	}

	private static class TestParticleRenderer extends ParticleGroup<TestParticle> {
		final QuadParticleRenderState submittable = new QuadParticleRenderState();

		TestParticleRenderer(ParticleEngine particleManager) {
			super(particleManager);
		}

		@Override
		public ParticleGroupRenderState extractRenderState(Frustum frustum, Camera camera, float tickProgress) {
			for (TestParticle particle : this.particles) {
				if (!particle.intersectPoint(frustum)) {
					continue;
				}

				particle.extract(this.submittable, camera, tickProgress);
			}

			return submittable;
		}
	}
}
