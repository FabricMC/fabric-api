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

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Scanner;

import org.gradle.api.DefaultTask;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.MapProperty;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.PathSensitive;
import org.gradle.api.tasks.PathSensitivity;
import org.gradle.api.tasks.TaskAction;

public abstract class BumpVersionTask extends DefaultTask {
	@InputFile
	@PathSensitive(PathSensitivity.NONE)
	public abstract RegularFileProperty getGradlePropertiesFile();

	@Input
	public abstract ListProperty<String> getModuleNames();

	@Input
	public abstract MapProperty<String, String> getModuleVersions();

	@Input
	public abstract MapProperty<String, String> getApiDependencies();

	public BumpVersionTask() {
		setGroup("publishing");
		getOutputs().upToDateWhen(task -> false);
	}

	@TaskAction
	public void runTask() throws IOException {
		LinkedHashMap<String, Integer> toUpdate = new LinkedHashMap<>();
		readInteractiveUpdates(toUpdate);

		while (true) {
			LinkedHashMap<String, Integer> temp = new LinkedHashMap<>();

			for (String projectName : toUpdate.keySet()) {
				getApiDependencies().get().forEach((childProjectName, dependencies) -> {
					if (containsDependency(dependencies, projectName) && !toUpdate.containsKey(childProjectName)) {
						System.out.println("Bumping patch of " + childProjectName + " as it depends on " + projectName);
						temp.put(childProjectName, 2);
					}
				});
			}

			if (temp.isEmpty()) {
				break;
			}

			toUpdate.putAll(temp);
		}

		File gradlePropertiesFile = getGradlePropertiesFile().get().getAsFile();
		String text = java.nio.file.Files.readString(gradlePropertiesFile.toPath(), StandardCharsets.UTF_8);

		for (Map.Entry<String, Integer> entry : toUpdate.entrySet()) {
			String projectName = entry.getKey();
			int index = entry.getValue();
			String version = getModuleVersions().get().get(projectName);

			if (version == null) {
				throw new NullPointerException("Could not find version for " + projectName);
			}

			String[] split = version.split("\\.");
			split[index] = Integer.toString(Integer.parseInt(split[index]) + 1);

			for (int i = index + 1; i < split.length; i++) {
				split[i] = "0";
			}

			String newVersion = String.join(".", split);
			System.out.println(projectName + ": " + version + " -> " + newVersion);
			text = text.replace(projectName + "-version=" + version, projectName + "-version=" + newVersion);
		}

		java.nio.file.Files.writeString(gradlePropertiesFile.toPath(), text, StandardCharsets.UTF_8);
	}

	private void readInteractiveUpdates(LinkedHashMap<String, Integer> toUpdate) {
		Scanner scanner = new Scanner(System.in);

		while (true) {
			System.out.println("Enter module name to update, or done to continue");
			String input = scanner.nextLine();

			if (input.equals("done")) {
				break;
			}

			if (input.equals("allPatch")) {
				getModuleNames().get().forEach(moduleName -> toUpdate.put(moduleName, 2));
				break;
			}

			if (!getModuleNames().get().contains(input)) {
				System.out.println("Could not find project with name: " + input);
				continue;
			}

			while (true) {
				System.out.println("Bump version for " + input + ":");
				System.out.println("0) Bump Major");
				System.out.println("1) Bump Minor");
				System.out.println("2) Bump Patch");

				String bump = scanner.nextLine();

				if (!bump.equals("0") && !bump.equals("1") && !bump.equals("2")) {
					System.out.println("Invalid input");
					continue;
				}

				toUpdate.put(input, Integer.parseInt(bump));
				break;
			}
		}
	}

	private static boolean containsDependency(String dependencies, String projectName) {
		for (String dependency : dependencies.split(",")) {
			if (dependency.equals(projectName)) {
				return true;
			}
		}

		return false;
	}
}
