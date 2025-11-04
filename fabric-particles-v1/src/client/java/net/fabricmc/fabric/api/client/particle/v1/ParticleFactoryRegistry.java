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

import org.jetbrains.annotations.ApiStatus;

import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;

import net.fabricmc.fabric.api.particle.v1.FabricParticleTypes;
import net.fabricmc.fabric.impl.client.particle.ParticleFactoryRegistryImpl;

/**
 * Registry for adding particle factories on the client for
 * particle types created using FabricParticleTypes (or otherwise).
 *
 * @see FabricParticleTypes
 */
@ApiStatus.NonExtendable
public interface ParticleFactoryRegistry {
	static ParticleFactoryRegistry getInstance() {
		return ParticleFactoryRegistryImpl.INSTANCE;
	}

	/**
	 * Registers a factory for constructing particles of the given type.
	 */
	<T extends ParticleOptions> void register(ParticleType<T> type, ParticleProvider<T> factory);

	/**
	 * Registers a delayed factory for constructing particles of the given type.
	 *
	 * <p>The factory method will be called with a sprite provider to use for that particle when it comes time.
	 *
	 * <p>Particle sprites will be loaded from domain:/particles/particle_name.json as per vanilla minecraft behavior.
	 */
	<T extends ParticleOptions> void register(ParticleType<T> type, PendingParticleFactory<T> constructor);

	/**
	 * A pending particle factory.
	 *
	 * @param <T> The type of particle effects this factory deals with.
	 */
	@FunctionalInterface
	interface PendingParticleFactory<T extends ParticleOptions> {
		/**
		 * Called to create a new particle factory.
		 *
		 * <p>Particle sprites will be loaded from domain:/particles/particle_name.json as per vanilla minecraft behavior.
		 *
		 * @param provider The sprite provider used to supply sprite textures when drawing the mod's particle.
		 *
		 * @return A new particle factory.
		 */
		ParticleProvider<T> create(FabricSpriteProvider provider);
	}
}
