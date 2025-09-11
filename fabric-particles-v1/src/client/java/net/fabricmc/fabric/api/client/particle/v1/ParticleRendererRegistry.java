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

import java.util.Locale;
import java.util.function.Function;

import net.minecraft.client.particle.ParticleManager;
import net.minecraft.client.particle.ParticleRenderer;
import net.minecraft.client.particle.ParticleTextureSheet;
import net.minecraft.util.Identifier;

import net.fabricmc.fabric.impl.client.particle.ParticleRendererRegistryImpl;

public interface ParticleRendererRegistry {
	static void register(ParticleTextureSheet textureSheet, Function<ParticleManager, ParticleRenderer<?>> function) {
		registerBefore(ParticleTextureSheet.NO_RENDER, textureSheet, function);
	}

	static void registerAfter(ParticleTextureSheet other, ParticleTextureSheet textureSheet, Function<ParticleManager, ParticleRenderer<?>> function) {
		registerAfter(getId(other), textureSheet, function);
	}

	static void registerAfter(Identifier other, ParticleTextureSheet textureSheet, Function<ParticleManager, ParticleRenderer<?>> function) {
		ParticleRendererRegistryImpl.INSTANCE.register(ParticleRendererRegistryImpl.Order.AFTER, other, textureSheet, function);
	}

	static void registerBefore(ParticleTextureSheet other, ParticleTextureSheet textureSheet, Function<ParticleManager, ParticleRenderer<?>> function) {
		registerBefore(getId(other), textureSheet, function);
	}

	static void registerBefore(Identifier other, ParticleTextureSheet textureSheet, Function<ParticleManager, ParticleRenderer<?>> function) {
		ParticleRendererRegistryImpl.INSTANCE.register(ParticleRendererRegistryImpl.Order.BEFORE, other, textureSheet, function);
	}

	static Identifier getId(ParticleTextureSheet textureSheet) {
		if (textureSheet == ParticleTextureSheet.SINGLE_QUADS
				|| textureSheet == ParticleTextureSheet.NO_RENDER
				|| textureSheet == ParticleTextureSheet.ELDER_GUARDIANS
				|| textureSheet == ParticleTextureSheet.ITEM_PICKUP) {
			return ofVanilla(textureSheet);
		}

		return Identifier.of(textureSheet.name());
	}

	private static Identifier ofVanilla(ParticleTextureSheet sheet) {
		return Identifier.ofVanilla(sheet.name().toLowerCase(Locale.ROOT));
	}
}
