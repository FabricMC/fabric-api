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
import net.fabricmc.fabric.impl.client.particle.ParticleProviderRegistryImpl;

/**
 * Registry for adding particle providers on the client for
 * particle types created using {@link FabricParticleTypes} (or otherwise).
 *
 * @see FabricParticleTypes
 */
@ApiStatus.NonExtendable
public interface ParticleProviderRegistry {
	static ParticleProviderRegistry getInstance() {
		return ParticleProviderRegistryImpl.INSTANCE;
	}

	/**
	 * Registers a provider for constructing particles of the given type.
	 */
	<T extends ParticleOptions> void register(ParticleType<T> type, ParticleProvider<T> provider);

	/**
	 * Registers a delayed provider for constructing particles of the given type.
	 *
	 * <p>The provider method will be called with a sprite set to use for that particle when it comes time.
	 *
	 * <p>Particle sprites will be loaded from domain:/particles/particle_name.json as per vanilla minecraft behavior.
	 */
	<T extends ParticleOptions> void register(ParticleType<T> type, PendingParticleProvider<T> constructor);

	/**
	 * A pending particle provider.
	 *
	 * @param <T> The type of particle options this provider deals with.
	 */
	@FunctionalInterface
	interface PendingParticleProvider<T extends ParticleOptions> {
		/**
		 * Called to create a new particle provider.
		 *
		 * <p>Particle sprites will be loaded from domain:/particles/particle_name.json as per vanilla minecraft behavior.
		 *
		 * @param spriteSet The sprite set used to supply sprite textures when drawing the mod's particle.
		 *
		 * @return A new particle provider.
		 */
		ParticleProvider<T> create(FabricSpriteSet spriteSet);
	}
}
