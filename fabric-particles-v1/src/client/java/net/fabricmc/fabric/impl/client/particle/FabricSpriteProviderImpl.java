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

package net.fabricmc.fabric.impl.client.particle;

import java.util.List;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.client.particle.SpriteProvider;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.client.texture.atlas.Atlases;
import net.minecraft.util.math.random.Random;

import net.fabricmc.fabric.api.client.particle.v1.FabricSpriteProvider;
import net.fabricmc.fabric.mixin.client.particle.ParticleManagerAccessor;

public class FabricSpriteProviderImpl implements FabricSpriteProvider {
	private final ParticleManager particleManager;
	private final SpriteProvider delegate;

	FabricSpriteProviderImpl(ParticleManager particleManager, SpriteProvider delegate) {
		this.particleManager = particleManager;
		this.delegate = delegate;
	}

	@Override
	public SpriteAtlasTexture getAtlas() {
		return MinecraftClient.getInstance().getAtlasManager().getAtlasTexture(Atlases.PARTICLES);
	}

	@Override
	public List<Sprite> getSprites() {
		return ((ParticleManagerAccessor.SimpleSpriteProviderAccessor) delegate).getSprites();
	}

	@Override
	public Sprite getSprite(int i, int j) {
		return delegate.getSprite(i, j);
	}

	@Override
	public Sprite getSprite(Random random) {
		return delegate.getSprite(random);
	}
}
