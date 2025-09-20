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

import org.jetbrains.annotations.Nullable;

import net.minecraft.client.particle.ParticleManager;
import net.minecraft.client.particle.ParticleRenderer;
import net.minecraft.client.particle.ParticleTextureSheet;
import net.minecraft.util.Identifier;

import net.fabricmc.fabric.impl.client.particle.ParticleRendererRegistryImpl;

/**
 * A registry for custom {@link ParticleRenderer}s.
 */
public final class ParticleRendererRegistry {
	/**
	 * Registers a {@link ParticleRenderer} factory for the given {@link ParticleTextureSheet}.
	 *
	 * @param textureSheet the texture sheet
	 * @param function the factory function
	 */
	public static void register(ParticleTextureSheet textureSheet, Function<ParticleManager, ParticleRenderer<?>> function) {
		ParticleRendererRegistryImpl.INSTANCE.register(textureSheet, function);
	}

	/**
	 * Registers a rendering order between two {@link ParticleTextureSheet}s.
	 *
	 * <p>The first texture sheet will be rendered before the second texture sheet.
	 *
	 * <p>Note that the rendering order of vanilla texture sheets is already defined by Minecraft,
	 * and you cannot change the order of vanilla texture sheets with this method.
	 *
	 * @param first  the texture sheet to render first
	 * @param second the texture sheet to render second
	 */
	public static void registerOrdering(ParticleTextureSheet first, Identifier second) {
		registerOrdering(getId(first), second);
	}

	/**
	 * Registers a rendering order between two {@link ParticleTextureSheet}s.
	 *
	 * <p>The first texture sheet will be rendered before the second texture sheet.
	 *
	 * <p>Note that the rendering order of vanilla texture sheets is already defined by Minecraft,
	 * and you cannot change the order of vanilla texture sheets with this method.
	 *
	 * @param first  the texture sheet to render first
	 * @param second the texture sheet to render second
	 */
	public static void registerOrdering(ParticleTextureSheet first, ParticleTextureSheet second) {
		registerOrdering(getId(first), getId(second));
	}

	/**
	 * Registers a rendering order between two {@link ParticleTextureSheet}s.
	 *
	 * <p>The first texture sheet will be rendered before the second texture sheet.
	 *
	 * <p>Note that the rendering order of vanilla texture sheets is already defined by Minecraft,
	 * and you cannot change the order of vanilla texture sheets with this method.
	 *
	 * @param first  the texture sheet to render first
	 * @param second the texture sheet to render second
	 */
	public static void registerOrdering(Identifier first, ParticleTextureSheet second) {
		registerOrdering(first, getId(second));
	}

	/**
	 * Registers a rendering order between two {@link ParticleTextureSheet}s.
	 *
	 * <p>The first texture sheet will be rendered before the second texture sheet.
	 *
	 * <p>Note that the rendering order of vanilla texture sheets is already defined by Minecraft,
	 * and you cannot change the order of vanilla texture sheets with this method.
	 *
	 * @param first  the texture sheet to render first
	 * @param second the texture sheet to render second
	 */
	public static void registerOrdering(Identifier first, Identifier second) {
		ParticleRendererRegistryImpl.INSTANCE.registerOrdering(first, second);
	}

	/**
	 * Gets the {@link ParticleTextureSheet} registered with the given identifier.
	 *
	 * @param id the identifier of the texture sheet
	 * @return the texture sheet, or null if none is registered with the given identifier
	 */
	public static @Nullable ParticleTextureSheet getParticleTextureSheet(Identifier id) {
		return ParticleRendererRegistryImpl.INSTANCE.getParticleTextureSheet(id);
	}

	/**
	 * Gets the identifier for the given {@link ParticleTextureSheet}.
	 *
	 * @param textureSheet the texture sheet
	 * @return the identifier
	 */
	public static Identifier getId(ParticleTextureSheet textureSheet) {
		if (textureSheet == ParticleTextureSheet.SINGLE_QUADS
				|| textureSheet == ParticleTextureSheet.NO_RENDER
				|| textureSheet == ParticleTextureSheet.ELDER_GUARDIANS
				|| textureSheet == ParticleTextureSheet.ITEM_PICKUP) {
			return Identifier.ofVanilla(textureSheet.name().toLowerCase(Locale.ROOT));
		}

		return Identifier.of(textureSheet.name());
	}

	private ParticleRendererRegistry() {
	}
}
