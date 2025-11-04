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

package net.fabricmc.fabric.api.client.particle.v1;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;

/**
 * Events related to particle rendering.
 */
public final class ParticleRenderEvents {
	private ParticleRenderEvents() {
	}

	/**
	 * An event that checks if a {@linkplain net.minecraft.client.particle.TerrainParticle block dust particle}
	 * can be tinted using the corresponding block's {@linkplain net.minecraft.client.color.block.BlockColor color provider}.
	 *
	 * <p>The default return value of this event is {@code true}. If any callback returns {@code false} for a given call,
	 * further iteration will be canceled and the event invoker will return {@code false}.
	 */
	public static final Event<AllowBlockDustTint> ALLOW_BLOCK_DUST_TINT = EventFactory.createArrayBacked(AllowBlockDustTint.class, callbacks -> (state, world, pos) -> {
		for (AllowBlockDustTint callback : callbacks) {
			if (!callback.allowBlockDustTint(state, world, pos)) {
				return false;
			}
		}

		return true;
	});

	@FunctionalInterface
	public interface AllowBlockDustTint {
		/**
		 * Checks whether a {@linkplain net.minecraft.client.particle.TerrainParticle block dust particle} can be
		 * tinted using the corresponding block's {@linkplain net.minecraft.client.color.block.BlockColor color provider}.
		 *
		 * @param state the block state that the particle represents
		 * @param world the world the particle is created in
		 * @param pos   the position of the particle
		 * @return {@code true} if color provider tinting should be allowed, {@code false} otherwise
		 */
		boolean allowBlockDustTint(BlockState state, ClientLevel world, BlockPos pos);
	}
}
