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

import net.minecraft.tags.FluidTags;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.particle.v1.ParticleRenderEvents;
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;
import net.fabricmc.fabric.test.particle.ParticleTestSetup;
import net.fabricmc.fabric.test.particle.ParticleTintTestBlock;

public final class ParticleRenderEventTests implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		ColorProviderRegistry.BLOCK.register((state, world, pos, tintIndex) -> {
			if (tintIndex == 0) {
				return ((ParticleTintTestBlock) state.getBlock()).color;
			}

			return -1;
		}, ParticleTestSetup.ALWAYS_TINTED, ParticleTestSetup.TINTED_OVER_WATER, ParticleTestSetup.NEVER_TINTED);

		ParticleRenderEvents.ALLOW_BLOCK_DUST_TINT.register((state, world, pos) -> {
			if (state.is(ParticleTestSetup.NEVER_TINTED)) {
				return false;
			} else if (state.is(ParticleTestSetup.TINTED_OVER_WATER)) {
				return world.getFluidState(pos.below()).is(FluidTags.WATER);
			}

			return true;
		});
	}
}
