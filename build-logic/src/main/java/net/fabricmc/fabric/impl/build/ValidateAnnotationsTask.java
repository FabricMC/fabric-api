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

package net.fabricmc.fabric.impl.build;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.regex.Pattern;

import org.gradle.api.tasks.SourceTask;
import org.gradle.api.tasks.TaskAction;

public abstract class ValidateAnnotationsTask extends SourceTask {
	private static final Pattern API_STATUS_INTERNAL = Pattern.compile("@ApiStatus\\.Internal");
	private static final Pattern ENVIRONMENT = Pattern.compile("@Environment");

	@TaskAction
	public void run() {
		for (String directory : List.of("api", "impl", "mixin", "test")) {
			getSource().matching(pattern -> pattern.include("net/fabricmc/fabric/" + directory + "/**/*.java")).forEach(file -> {
				if (file.isDirectory()) {
					return;
				}

				String contents;

				try {
					contents = java.nio.file.Files.readString(file.toPath(), StandardCharsets.UTF_8);
				} catch (IOException e) {
					throw new RuntimeException("Could not read file: " + file, e);
				}

				if (ENVIRONMENT.matcher(contents).find()) {
					throw new RuntimeException("Found @Environment annotation in file: " + file);
				}

				if (!directory.equals("api") && !file.getName().equals("package-info.java") && API_STATUS_INTERNAL.matcher(contents).find()) {
					throw new RuntimeException("Found @ApiStatus.Internal in non-package-info implementation file: " + file);
				}
			});
		}
	}
}
