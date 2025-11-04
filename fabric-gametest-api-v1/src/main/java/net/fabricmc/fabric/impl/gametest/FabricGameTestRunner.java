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

package net.fabricmc.fabric.impl.gametest;

import java.io.File;
import java.util.Optional;

import javax.xml.parsers.ParserConfigurationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.minecraft.gametest.framework.GameTestServer;
import net.minecraft.gametest.framework.GlobalTestReporter;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.world.level.storage.LevelStorageSource;

public final class FabricGameTestRunner {
	public static final boolean ENABLED = System.getProperty(GameTestSystemProperties.ENABLED) != null;

	private static final Logger LOGGER = LoggerFactory.getLogger(FabricGameTestRunner.class);
	private static final String GAMETEST_STRUCTURE_PATH = "gametest/structure";

	public static final FileToIdConverter GAMETEST_STRUCTURE_FINDER = new FileToIdConverter(GAMETEST_STRUCTURE_PATH, ".snbt");

	private FabricGameTestRunner() {
	}

	public static void runHeadlessServer(LevelStorageSource.LevelStorageAccess session, PackRepository resourcePackManager) {
		String reportPath = System.getProperty(GameTestSystemProperties.REPORT_FILE);

		if (reportPath != null) {
			try {
				GlobalTestReporter.replaceWith(new SavingXmlReportingTestCompletionListener(new File(reportPath)));
			} catch (ParserConfigurationException e) {
				throw new RuntimeException(e);
			}
		}

		LOGGER.info("Starting test server");

		Optional<String> filter = Optional.ofNullable(System.getProperty(GameTestSystemProperties.FILTER));
		boolean verify = Boolean.getBoolean(GameTestSystemProperties.VERIFY);
		MinecraftServer.spin((thread) -> GameTestServer.create(thread, session, resourcePackManager, filter, verify));
	}
}
