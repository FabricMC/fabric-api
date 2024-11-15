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

package net.fabricmc.fabric.mixin.resource.loader.server;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonParseException;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import net.minecraft.util.Language;

import net.fabricmc.fabric.impl.resource.loader.ServerLanguageUtil;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;

@Mixin(Language.class)
class LanguageMixin {
	@Shadow
	@Final
	private static Logger LOGGER;

	@Redirect(method = "create", at = @At(value = "INVOKE", target = "Lcom/google/common/collect/ImmutableMap$Builder;build()Lcom/google/common/collect/ImmutableMap;", remap = false))
	private static ImmutableMap<String, String> create(ImmutableMap.Builder<String, String> cir) {
		Map<String, String> map = new HashMap<>(cir.buildOrThrow());

		for (Path path : ServerLanguageUtil.getModLanguageFiles()) {
			loadFromPath(path, map::put);
		}

		return ImmutableMap.copyOf(map);
	}

	@Redirect(method = "load(Ljava/util/function/BiConsumer;Ljava/lang/String;)V", at = @At(value = "INVOKE", target = "Ljava/lang/Class;getResourceAsStream(Ljava/lang/String;)Ljava/io/InputStream;"))
	private static InputStream readCorrectVanillaResource(Class instance, String path) throws IOException {
		ModContainer mod = FabricLoader.getInstance().getModContainer("minecraft").orElseThrow();
		Path langPath = mod.findPath(path).orElse(null);

		if (langPath == null) {
			throw new IOException("Could not read %s from minecraft ModContainer".formatted(path));
		} else {
			return Files.newInputStream(langPath);
		}
	}

	private static void loadFromPath(Path path, BiConsumer<String, String> entryConsumer) {
		try (InputStream stream = Files.newInputStream(path)) {
			LOGGER.debug("Loading translations from {}", path);
			load(stream, entryConsumer);
		} catch (JsonParseException | IOException e) {
			LOGGER.error("Couldn't read strings from {}", path, e);
		}
	}

	@Shadow
	public static void load(InputStream inputStream, BiConsumer<String, String> entryConsumer) {
	}
}
