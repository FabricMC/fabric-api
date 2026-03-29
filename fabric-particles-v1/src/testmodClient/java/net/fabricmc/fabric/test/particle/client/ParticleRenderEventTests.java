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

import java.util.List;

import net.minecraft.client.color.block.BlockTintSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.block.state.BlockState;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.particle.v1.ParticleRenderEvents;
import net.fabricmc.fabric.api.client.rendering.v1.BlockColorRegistry;
import net.fabricmc.fabric.test.particle.ParticleTestSetup;

public final class ParticleRenderEventTests implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		BlockTintSource tintSource = new BlockTintSource() {
			@Override
			public int color(BlockState state) {
				return -1;
			}
		};

		BlockColorRegistry.register(List.of(tintSource), ParticleTestSetup.ALWAYS_TINTED, ParticleTestSetup.TINTED_OVER_WATER, ParticleTestSetup.NEVER_TINTED);

		ParticleRenderEvents.ALLOW_TERRAIN_PARTICLE_TINT.register((state, level, pos) -> {
			if (state.is(ParticleTestSetup.NEVER_TINTED)) {
				return false;
			} else if (state.is(ParticleTestSetup.TINTED_OVER_WATER)) {
				return level.getFluidState(pos.below()).is(FluidTags.WATER);
			}

			return true;
		});
	}
}
