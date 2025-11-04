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

import org.jspecify.annotations.Nullable;

import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.client.particle.ParticleGroup;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.resources.Identifier;

import net.fabricmc.fabric.impl.client.particle.ParticleRendererRegistryImpl;

/**
 * A registry for custom {@link ParticleGroup}s.
 */
public final class ParticleRendererRegistry {
	/**
	 * Registers a {@link ParticleGroup} factory for the given {@link ParticleRenderType}.
	 *
	 * @param textureSheet the texture sheet
	 * @param function the factory function
	 */
	public static void register(ParticleRenderType textureSheet, Function<ParticleEngine, ParticleGroup<?>> function) {
		ParticleRendererRegistryImpl.INSTANCE.register(textureSheet, function);
	}

	/**
	 * Registers a rendering order between two {@link ParticleRenderType}s.
	 *
	 * <p>The first texture sheet will be rendered before the second texture sheet.
	 *
	 * <p>Note that the rendering order of vanilla texture sheets is already defined by Minecraft,
	 * and you cannot change the order of vanilla texture sheets with this method.
	 *
	 * @param first  the texture sheet to render first
	 * @param second the texture sheet to render second
	 */
	public static void registerOrdering(ParticleRenderType first, Identifier second) {
		registerOrdering(getId(first), second);
	}

	/**
	 * Registers a rendering order between two {@link ParticleRenderType}s.
	 *
	 * <p>The first texture sheet will be rendered before the second texture sheet.
	 *
	 * <p>Note that the rendering order of vanilla texture sheets is already defined by Minecraft,
	 * and you cannot change the order of vanilla texture sheets with this method.
	 *
	 * @param first  the texture sheet to render first
	 * @param second the texture sheet to render second
	 */
	public static void registerOrdering(ParticleRenderType first, ParticleRenderType second) {
		registerOrdering(getId(first), getId(second));
	}

	/**
	 * Registers a rendering order between two {@link ParticleRenderType}s.
	 *
	 * <p>The first texture sheet will be rendered before the second texture sheet.
	 *
	 * <p>Note that the rendering order of vanilla texture sheets is already defined by Minecraft,
	 * and you cannot change the order of vanilla texture sheets with this method.
	 *
	 * @param first  the texture sheet to render first
	 * @param second the texture sheet to render second
	 */
	public static void registerOrdering(Identifier first, ParticleRenderType second) {
		registerOrdering(first, getId(second));
	}

	/**
	 * Registers a rendering order between two {@link ParticleRenderType}s.
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
	 * Gets the {@link ParticleRenderType} registered with the given identifier.
	 *
	 * @param id the identifier of the texture sheet
	 * @return the texture sheet, or null if none is registered with the given identifier
	 */
	public static @Nullable ParticleRenderType getParticleTextureSheet(Identifier id) {
		return ParticleRendererRegistryImpl.INSTANCE.getParticleTextureSheet(id);
	}

	/**
	 * Gets the identifier for the given {@link ParticleRenderType}.
	 *
	 * @param textureSheet the texture sheet
	 * @return the identifier
	 */
	public static Identifier getId(ParticleRenderType textureSheet) {
		if (textureSheet == ParticleRenderType.SINGLE_QUADS
				|| textureSheet == ParticleRenderType.NO_RENDER
				|| textureSheet == ParticleRenderType.ELDER_GUARDIANS
				|| textureSheet == ParticleRenderType.ITEM_PICKUP) {
			return Identifier.withDefaultNamespace(textureSheet.name().toLowerCase(Locale.ROOT));
		}

		return Identifier.parse(textureSheet.name());
	}

	private ParticleRendererRegistry() {
	}
}
