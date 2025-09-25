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

import java.util.Arrays;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleFactory;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.client.particle.ParticleRenderer;
import net.minecraft.client.particle.ParticleTextureSheet;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.Frustum;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.Submittable;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.state.CameraRenderState;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particle.SimpleParticleType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
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

	private record TestParticleFactory(
			FabricSpriteProvider spriteProvider) implements ParticleFactory<SimpleParticleType> {
		@Override
		public Particle createParticle(SimpleParticleType parameters, ClientWorld world, double x, double y, double z, double velocityX, double velocityY, double velocityZ, Random random) {
			return new TestParticle(world, x, y, z, velocityX, velocityY, velocityZ, spriteProvider.getSprite(random));
		}
	}

	private static class TestParticle extends Particle {
		private final Sprite sprite;

		TestParticle(ClientWorld world, double x, double y, double z, double velocityX, double velocityY, double velocityZ, Sprite sprite) {
			super(world, x, y, z, velocityX, velocityY, velocityZ);
			this.sprite = sprite;
		}

		@Override
		public ParticleTextureSheet textureSheet() {
			return TEST_PARTICLE_TEXTURE_SHEET;
		}

		private boolean intersectPoint(Frustum frustum) {
			return frustum.intersectPoint(x, y, z);
		}

		public void render(TestParticleSubmittable submittable, Camera camera, float tickProgress) {
			double frameX = MathHelper.lerp(tickProgress, lastX, x) - camera.getPos().x;
			double frameY = MathHelper.lerp(tickProgress, lastY, y) - camera.getPos().y;
			double frameZ = MathHelper.lerp(tickProgress, lastZ, z) - camera.getPos().z;
			submittable.addParticle(frameX, frameY, frameZ);
		}
	}

	private static class TestParticleRenderer extends ParticleRenderer<TestParticle> {
		final TestParticleSubmittable submittable = new TestParticleSubmittable();

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

	private static class TestParticleSubmittable implements Submittable {
		private final TestParticlePositions positions = new TestParticlePositions();

		@Override
		public void submit(OrderedRenderCommandQueue queue, CameraRenderState cameraRenderState) {
			MatrixStack matrices = new MatrixStack();

			for (int i = 0; i < positions.getStoredPoints(); i++) {
				matrices.push();
				Vec3d position = positions.getPoint(i);

				matrices.translate(position);

				queue.submitCustom(
						matrices,
						//help: find a better way to do this
						RenderLayer.getEntityCutout(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE),
						(matrixEntry, vertexConsumer) -> {
							//yes, this will render the entire atlas
							//also is in the shape of a rectangle, a shape impossible to create with a BillboardParticle
							vertexConsumer.vertex(matrixEntry, 0.5f, 0.0f, -1.0f)
									.texture(1f, 1f)
									.light(LightmapTextureManager.MAX_LIGHT_COORDINATE)
									.overlay(OverlayTexture.DEFAULT_UV)
									.color(-1)
									.normal(matrixEntry, 0f, 1f, 0f);
							vertexConsumer.vertex(matrixEntry, 0.5f, 0.0f, 1.0f)
									.texture(0f, 1f)
									.light(LightmapTextureManager.MAX_LIGHT_COORDINATE)
									.overlay(OverlayTexture.DEFAULT_UV)
									.color(-1)
									.normal(matrixEntry, 0f, 1f, 0f);
							vertexConsumer.vertex(matrixEntry, -0.5f, 0.0f, 1.0f)
									.texture(0f, 0f)
									.light(LightmapTextureManager.MAX_LIGHT_COORDINATE)
									.overlay(OverlayTexture.DEFAULT_UV)
									.color(-1)
									.normal(matrixEntry, 0f, 1f, 0f);
							vertexConsumer.vertex(matrixEntry, -0.5f, 0.0f, -1.0f)
									.texture(1f, 0f)
									.light(LightmapTextureManager.MAX_LIGHT_COORDINATE)
									.overlay(OverlayTexture.DEFAULT_UV)
									.color(-1)
									.normal(matrixEntry, 0f, 1f, 0f);
						}
				);
				matrices.pop();
			}
		}

		@Override
		public void onFrameEnd() {
			positions.reset();
		}

		public void addParticle(double x, double y, double z) {
			positions.addPoint(x, y, z);
		}
	}

	private static class TestParticlePositions {
		//note: this could be any kind of data, see BillboardParticleSubmittable for a more involved example
		private int maxPoints = 128;
		private double[] positionData = new double[maxPoints * 3];
		private int nextVertexIndex = 0;

		public void addPoint(double x, double y, double z) {
			if (nextVertexIndex >= maxPoints) {
				increaseCapacity();
			}

			int currentIndex = nextVertexIndex * 3;
			positionData[currentIndex++] = x;
			positionData[currentIndex++] = y;
			positionData[currentIndex] = z;
			++nextVertexIndex;
		}

		public Vec3d getPoint(int index) {
			int lookupIndex = index * 3;
			return new Vec3d(
					positionData[lookupIndex++],
					positionData[lookupIndex++],
					positionData[lookupIndex]
			);
		}

		public int getStoredPoints() {
			return nextVertexIndex + 1;
		}

		public void reset() {
			nextVertexIndex = 0;
		}

		private void increaseCapacity() {
			maxPoints *= 2;
			positionData = Arrays.copyOf(positionData, maxPoints * 3);
		}
	}
}
