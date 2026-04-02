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

package net.fabricmc.fabric.impl.client.indigo;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Properties;

import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.minecraft.client.renderer.MultiBufferSource;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.renderer.v1.Renderer;
import net.fabricmc.fabric.api.util.TriState;
import net.fabricmc.fabric.impl.client.indigo.renderer.IndigoRenderer;
import net.fabricmc.fabric.impl.client.indigo.renderer.aocalc.AoConfig;
import net.fabricmc.loader.api.FabricLoader;

public class Indigo implements ClientModInitializer {
	public static final AoConfig AMBIENT_OCCLUSION_MODE;
	/** Set true in dev env to confirm results match vanilla when they should. */
	public static final boolean DEBUG_COMPARE_LIGHTING;
	public static final boolean FIX_SMOOTH_LIGHTING_OFFSET;
	/** When true, requires {@link #FIX_SMOOTH_LIGHTING_OFFSET} to be true. */
	public static final boolean FIX_MEAN_LIGHT_CALCULATION;
	public static final boolean FIX_EXTERIOR_VERTEX_LIGHTING;
	public static final boolean FIX_LUMINOUS_AO_SHADE;

	private static final Logger LOGGER = LoggerFactory.getLogger(Indigo.class);
	/** If set the default config file will be generated on startup, restoring pre 26.1 behavior. */
	private static final boolean GENERATE_CONFIG_FILE = System.getProperty("fabric.indigo.generateConfigFile") != null;

	// A hack for Mixins, check usages
	public static final ScopedValue<MultiBufferSource> LEVEL_RENDERER_BUFFER_SOURCE = ScopedValue.newInstance();

	private static boolean asBoolean(@Nullable String property, boolean defValue) {
		return asTriState(property).orElse(defValue);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private static <T extends Enum> T asEnum(@Nullable String property, T defValue) {
		if (property != null && !property.isEmpty()) {
			for (Enum obj : defValue.getClass().getEnumConstants()) {
				if (property.equalsIgnoreCase(obj.name())) {
					//noinspection unchecked
					return (T) obj;
				}
			}
		}

		return defValue;
	}

	private static TriState asTriState(@Nullable String property) {
		if (property == null || property.isEmpty()) {
			return TriState.DEFAULT;
		}

		return switch (property.toLowerCase(Locale.ROOT)) {
		case "true" -> TriState.TRUE;
		case "false" -> TriState.FALSE;
		default -> TriState.DEFAULT;
		};
	}

	static {
		Path configDir = FabricLoader.getInstance().getConfigDir().resolve("fabric");
		Path configFile = configDir.resolve("indigo-renderer.properties");
		boolean configExists = Files.exists(configFile);
		Properties properties = new Properties();

		if (configExists) {
			try (InputStream stream = Files.newInputStream(configFile)) {
				properties.load(stream);
			} catch (IOException e) {
				LOGGER.warn("[Indigo] Could not read property file '{}'", configFile.toAbsolutePath(), e);
			}
		}

		AMBIENT_OCCLUSION_MODE = asEnum((String) properties.computeIfAbsent("ambient-occlusion-mode", _ -> "hybrid"), AoConfig.HYBRID);
		DEBUG_COMPARE_LIGHTING = asBoolean((String) properties.computeIfAbsent("debug-compare-lighting", _ -> "auto"), false);
		FIX_SMOOTH_LIGHTING_OFFSET = asBoolean((String) properties.computeIfAbsent("fix-smooth-lighting-offset", _ -> "auto"), true);
		boolean fixMeanLightCalculation = asBoolean((String) properties.computeIfAbsent("fix-mean-light-calculation", _ -> "auto"), true);
		FIX_EXTERIOR_VERTEX_LIGHTING = asBoolean((String) properties.computeIfAbsent("fix-exterior-vertex-lighting", _ -> "auto"), true);
		FIX_LUMINOUS_AO_SHADE = asBoolean((String) properties.computeIfAbsent("fix-luminous-block-ambient-occlusion", _ -> "auto"), false);

		if (fixMeanLightCalculation && !FIX_SMOOTH_LIGHTING_OFFSET) {
			fixMeanLightCalculation = false;
			LOGGER.warn("[Indigo] Config enabled 'fix-mean-light-calculation' but disabled 'fix-smooth-lighting-offset'; this is not supported! 'fix-mean-light-calculation' will be considered disabled.");
		}

		FIX_MEAN_LIGHT_CALCULATION = fixMeanLightCalculation;

		if (configExists || GENERATE_CONFIG_FILE) {
			if (!Files.exists(configDir)) {
				try {
					Files.createDirectories(configDir);
				} catch (IOException e) {
					LOGGER.warn("[Indigo] Could not create configuration directory: {}", configDir.toAbsolutePath(), e);
				}
			}

			try (OutputStream stream = Files.newOutputStream(configFile)) {
				properties.store(stream, "Fabric API Indigo properties file");
			} catch (IOException e) {
				LOGGER.warn("[Indigo] Could not store property file '{}'", configFile.toAbsolutePath(), e);
			}
		}
	}

	@Override
	public void onInitializeClient() {
		if (IndigoMixinConfigPlugin.shouldApplyIndigo()) {
			LOGGER.info("[Indigo] Registering Indigo renderer!");
			Renderer.register(IndigoRenderer.INSTANCE);
		} else {
			LOGGER.info("[Indigo] Different rendering plugin detected; not applying Indigo.");
		}
	}
}
