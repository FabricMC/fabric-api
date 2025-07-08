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

package net.fabricmc.fabric.impl.client.gametest.world;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Properties;
import java.util.function.Consumer;

import com.google.common.base.Preconditions;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.world.CreateWorldScreen;
import net.minecraft.client.gui.screen.world.WorldCreator;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.dedicated.MinecraftDedicatedServer;
import net.minecraft.world.GameRules;
import net.minecraft.world.gen.WorldPreset;
import net.minecraft.world.gen.WorldPresets;

import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestDedicatedServerContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.fabricmc.fabric.api.client.gametest.v1.world.TestWorldBuilder;
import net.fabricmc.fabric.impl.client.gametest.context.TestDedicatedServerContextImpl;
import net.fabricmc.fabric.impl.client.gametest.context.TestSingleplayerContextImpl;
import net.fabricmc.fabric.impl.client.gametest.threading.ThreadingImpl;
import net.fabricmc.fabric.impl.client.gametest.util.ClientGameTestImpl;
import net.fabricmc.fabric.impl.client.gametest.util.DedicatedServerImplUtil;

public class TestWorldBuilderImpl implements TestWorldBuilder {
	private static final Logger LOGGER = LoggerFactory.getLogger("fabric-client-gametest-api-v1");
	private final ClientGameTestContext context;
	private boolean useConsistentSettings = true;

	private Consumer<WorldCreator> settingsAdjustor = creator -> {
	};

	public TestWorldBuilderImpl(ClientGameTestContext context) {
		this.context = context;
	}

	@Override
	public TestWorldBuilder setUseConsistentSettings(boolean useConsistentSettings) {
		this.useConsistentSettings = useConsistentSettings;
		return this;
	}

	@Override
	public TestWorldBuilder adjustSettings(Consumer<WorldCreator> settingsAdjuster) {
		Preconditions.checkNotNull(settingsAdjuster, "settingsAdjuster");

		this.settingsAdjustor = settingsAdjuster;
		return this;
	}

	@Override
	public TestSingleplayerContext create() {
		ThreadingImpl.checkOnGametestThread("create");
		Preconditions.checkState(!ThreadingImpl.isServerRunning, "Cannot create a world when a server is running");

		Path saveDirectory = navigateCreateWorldScreen();
		ClientGameTestImpl.waitForWorldLoad(context);

		MinecraftServer server = context.computeOnClient(MinecraftClient::getServer);
		return new TestSingleplayerContextImpl(context, new TestWorldSaveImpl(context, saveDirectory), server);
	}

	@Override
	public TestDedicatedServerContext createServer(Properties serverProperties) {
		ThreadingImpl.checkOnGametestThread("createServer");
		Preconditions.checkState(!ThreadingImpl.isServerRunning, "Cannot create a server when a server is running");

		DedicatedServerImplUtil.saveLevelDataTo = Path.of(serverProperties.getProperty("level-name", "world"));

		try {
			FileUtils.deleteDirectory(DedicatedServerImplUtil.saveLevelDataTo.toFile());
		} catch (IOException e) {
			LOGGER.error("Failed to clean up old dedicated server world", e);
		}

		try {
			navigateCreateWorldScreen();
		} finally {
			DedicatedServerImplUtil.saveLevelDataTo = null;
		}

		MinecraftDedicatedServer server = DedicatedServerImplUtil.start(context, serverProperties);
		return new TestDedicatedServerContextImpl(context, server);
	}

	private Path navigateCreateWorldScreen() {
		Path saveDirectory = context.computeOnClient(client -> {
			CreateWorldScreen.show(client, client.currentScreen);

			if (!(client.currentScreen instanceof CreateWorldScreen createWorldScreen)) {
				throw new AssertionError("CreateWorldScreen.show did not set the current screen");
			}

			WorldCreator creator = createWorldScreen.getWorldCreator();

			if (useConsistentSettings) {
				setConsistentSettings(creator);
			}

			settingsAdjustor.accept(creator);

			return client.getLevelStorage().getSavesDirectory().resolve(creator.getWorldDirectoryName());
		});

		context.clickScreenButton("selectWorld.create");

		return saveDirectory;
	}

	private static void setConsistentSettings(WorldCreator creator) {
		RegistryEntry<WorldPreset> flatPreset = creator.getGeneratorOptionsHolder().getCombinedRegistryManager().getOrThrow(RegistryKeys.WORLD_PRESET).getOrThrow(WorldPresets.FLAT);
		creator.setWorldType(new WorldCreator.WorldType(flatPreset));
		creator.setSeed("1");
		creator.setGenerateStructures(false);
		creator.getGameRules().get(GameRules.DO_DAYLIGHT_CYCLE).set(false, null);
		creator.getGameRules().get(GameRules.DO_WEATHER_CYCLE).set(false, null);
		creator.getGameRules().get(GameRules.DO_MOB_SPAWNING).set(false, null);
	}
}
