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

package net.fabricmc.fabric.test.resource.loader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.minecraft.network.chat.Component;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.resource.v1.ResourceLoader;
import net.fabricmc.fabric.api.resource.v1.pack.PackActivationType;
import net.fabricmc.loader.api.FabricLoader;

public class BuiltinPackTestMod implements ModInitializer {
	public static final Logger LOGGER = LoggerFactory.getLogger(BuiltinPackTestMod.class);

	@Override
	public void onInitialize() {
		// Should always be present as it's **this** mod.
		FabricLoader.getInstance().getModContainer(Constants.NAMESPACE)
				.map(container -> ResourceLoader.registerBuiltinPack(Constants.id("test"),
						container, Component.literal("Fabric Resource Loader Test Pack"), PackActivationType.DEFAULT_ENABLED))
				.filter(success -> !success).ifPresent(success -> LOGGER.warn("Could not register built-in resource pack with custom name."));
		FabricLoader.getInstance().getModContainer(Constants.NAMESPACE)
				.map(container -> ResourceLoader.registerBuiltinPack(Constants.id("test2"),
						container, PackActivationType.NORMAL))
				.filter(success -> !success).ifPresent(success -> LOGGER.warn("Could not register built-in resource pack."));
	}
}
