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

package net.fabricmc.fabric.impl.client.gametest;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import org.apache.commons.io.file.PathUtils;
import org.jetbrains.annotations.Nullable;

import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.entrypoint.PreLaunchEntrypoint;

public class ClientGameTestPreLaunch implements PreLaunchEntrypoint {
	private static final String MARKER_FILE_NAME = ".client_gametests_rundir";
	private static final Set<String> CLEAN_DENY_LIST = Set.of(".fabric", "mods", "logs", "eula.txt");

	@Override
	public void onPreLaunch() {
		Boolean isClientGametestRunDir = detectClientGametestRunDir();
		boolean isClientGametestEnvironment = System.getProperty("fabric.client.gametest") != null;

		if (isClientGametestRunDir != null && isClientGametestEnvironment != isClientGametestRunDir) {
			if (isClientGametestEnvironment) {
				throw new RuntimeException("Running a client gametest in a non-client-gametest run directory. Please use separate run directories for client gametests");
			} else {
				throw new RuntimeException("Running a non-client-gametest in a client gametest run directory. Please use separate run directories for client gametests");
			}
		}

		if (isClientGametestEnvironment) {
			cleanRunDir();
		}
	}

	@Nullable
	private static Boolean detectClientGametestRunDir() {
		Path gameDir = FabricLoader.getInstance().getGameDir();

		if (Files.exists(gameDir.resolve(MARKER_FILE_NAME))) {
			return true;
		}

		try (Stream<Path> files = Files.list(FabricLoader.getInstance().getGameDir())) {
			boolean isNonGametestDir = files.anyMatch(file -> !CLEAN_DENY_LIST.contains(file.getFileName().toString()));
			return isNonGametestDir ? false : null;
		} catch (IOException e) {
			throw new RuntimeException("Error detecting client gametest directory", e);
		}
	}

	private static void cleanRunDir() {
		try {
			Path gameDir = FabricLoader.getInstance().getGameDir();
			List<Path> filesToDelete;

			try (Stream<Path> files = Files.list(gameDir)) {
				filesToDelete = files.filter(file -> !CLEAN_DENY_LIST.contains(file.getFileName().toString())).toList();
			}

			for (Path path : filesToDelete) {
				PathUtils.delete(path);
			}

			Files.createFile(gameDir.resolve(MARKER_FILE_NAME));
		} catch (IOException e) {
			throw new RuntimeException("Error cleaning gametest directory", e);
		}
	}
}
