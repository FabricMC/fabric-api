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

import java.util.List;

import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.particles.ParticleType;

/**
 * FabricSpriteProvider. It does the same thing as vanilla's SpriteProvider,
 * but in a way that's accessible to mods, and that exposes the atlas as well.
 *
 * <p>Custom sprites registered using ParticleFactoryRegistry have the options
 * to supply a particle factory which will receive an instance of this
 * interface containing the sprites set loaded for their particle from the
 * active resource packs.
 *
 * @see ParticleFactoryRegistry#register(ParticleType, ParticleProvider)
 * @see ParticleFactoryRegistry.PendingParticleFactory
 */
public interface FabricSpriteProvider extends SpriteSet {
	/**
	 * Returns the entire particles texture atlas.
	 */
	TextureAtlas getAtlas();

	/**
	 * Gets the list of all sprites available for this particle to use.
	 * This is defined in your resource pack.
	 */
	List<TextureAtlasSprite> getSprites();
}
