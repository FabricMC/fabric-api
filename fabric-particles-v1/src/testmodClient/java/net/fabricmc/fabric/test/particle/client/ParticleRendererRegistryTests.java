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

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.particle.BillboardParticle;
import net.minecraft.client.particle.BillboardParticleSubmittable;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleFactory;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.client.particle.ParticleRenderer;
import net.minecraft.client.particle.ParticleTextureSheet;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.Frustum;
import net.minecraft.client.render.Submittable;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particle.SimpleParticleType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.random.Random;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.particle.v1.FabricSpriteProvider;
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;
import net.fabricmc.fabric.api.client.particle.v1.ParticleRendererRegistry;
import net.fabricmc.fabric.api.particle.v1.FabricParticleTypes;

public class ParticleRendererRegistryTests implements ClientModInitializer {
	private static final Identifier PARTICLE_ID = Identifier.of("fabric-particles-v1-testmod", "test");
	private static final SimpleParticleType TEST_PARTICLE_TYPE = FabricParticleTypes.simple();
	private static final ParticleTextureSheet TEST_PARTICLE_TEXTURE_SHEET = new ParticleTextureSheet(PARTICLE_ID.toString());

	@Override
	public void onInitializeClient() {
		Registry.register(Registries.PARTICLE_TYPE, PARTICLE_ID, TEST_PARTICLE_TYPE);
		ParticleFactoryRegistry.getInstance().register(TEST_PARTICLE_TYPE, TestParticleFactory::new);

		ParticleRendererRegistry.register(TEST_PARTICLE_TEXTURE_SHEET, TestParticleRenderer::new);
		ParticleRendererRegistry.registerOrdering(TEST_PARTICLE_TEXTURE_SHEET, ParticleTextureSheet.ITEM_PICKUP);

		ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) ->
				dispatcher.register(ClientCommandManager.literal("custom_particles").executes(context -> {
					ClientWorld world = MinecraftClient.getInstance().world;
					Random random = world.getRandom();
					ClientPlayerEntity player = context.getSource().getPlayer();

					for (int i = 0; i < 35; i++) {
						world.addParticleClient(
								TEST_PARTICLE_TYPE,
								player.getX(), player.getY(), player.getZ(),
								MathHelper.nextBetween(random, -1.0F, 1.0F),
								0.5F,
								MathHelper.nextBetween(random, -1.0F, 1.0F)
						);
					}

					return 1;
				})));
	}

	private record TestParticleFactory(FabricSpriteProvider spriteProvider) implements ParticleFactory<SimpleParticleType> {
		@Override
		public Particle createParticle(SimpleParticleType parameters, ClientWorld world, double x, double y, double z, double velocityX, double velocityY, double velocityZ, Random random) {
			return new TestParticle(world, x, y, z, velocityX, velocityY, velocityZ, spriteProvider.getSprite(random));
		}
	}

	private static class TestParticle extends BillboardParticle {
		TestParticle(ClientWorld world, double x, double y, double z, double velocityX, double velocityY, double velocityZ, Sprite sprite) {
			super(world, x, y, z, velocityX, velocityY, velocityZ, sprite);
		}

		@Override
		protected RenderType getRenderType() {
			return RenderType.PARTICLE_ATLAS_OPAQUE;
		}

		@Override
		public ParticleTextureSheet textureSheet() {
			return TEST_PARTICLE_TEXTURE_SHEET;
		}

		private boolean intersectPoint(Frustum frustum) {
			return frustum.intersectPoint(x, y, z);
		}
	}

	private static class TestParticleRenderer extends ParticleRenderer<TestParticle> {
		final BillboardParticleSubmittable submittable = new BillboardParticleSubmittable();

		TestParticleRenderer(ParticleManager particleManager) {
			super(particleManager);
		}

		@Override
		public Submittable render(Frustum frustum, Camera camera, float tickProgress) {
			for (TestParticle particle : this.particles) {
				if (!particle.intersectPoint(frustum)) {
					continue;
				}

				particle.render(this.submittable, camera, tickProgress);
			}

			return submittable;
		}
	}
}
