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
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Scanner;

import org.gradle.api.DefaultTask;
import org.gradle.api.Project;
import org.gradle.api.tasks.TaskAction;

public abstract class BumpVersionTask extends DefaultTask {
	public BumpVersionTask() {
		setGroup("publishing");
		getOutputs().upToDateWhen(task -> false);
	}

	@TaskAction
	public void runTask() throws IOException {
		var scanner = new Scanner(System.in);
		var toUpdate = new LinkedHashMap<Project, Integer>();

		while (true) {
			System.out.println("Enter module name to update, or done to continue");
			String input = scanner.nextLine();

			if (input.equals("done")) {
				break;
			}

			if (input.equals("allPatch")) {
				getProject().getChildProjects().values().forEach(project -> {
					if (FabricApiBuildUtils.isFabricModule(project)) {
						toUpdate.put(project, 2);
					}
				});
				break;
			}

			Project subProject = getProject().getChildProjects().get(input);
			Project deprecatedProject = getProject().getChildProjects().get("deprecated");

			if (subProject == null && deprecatedProject != null) {
				subProject = deprecatedProject.getChildProjects().get(input);
			}

			if (subProject == null) {
				System.out.println("Could not find project with name: " + input);
				continue;
			}

			while (true) {
				System.out.println("Bump version for " + subProject.getName() + ":");
				System.out.println("0) Bump Major");
				System.out.println("1) Bump Minor");
				System.out.println("2) Bump Patch");

				input = scanner.nextLine();

				if (!input.equals("0") && !input.equals("1") && !input.equals("2")) {
					System.out.println("Invalid input");
					continue;
				}

				toUpdate.put(subProject, Integer.parseInt(input));
				break;
			}
		}

		while (true) {
			var temp = new LinkedHashMap<Project, Integer>();

			for (Project project : toUpdate.keySet()) {
				getProject().allprojects(childProject -> {
					if (!FabricApiBuildUtils.isFabricModule(childProject) || childProject == getProject()) {
						return;
					}

					var configuration = childProject.getConfigurations().findByName("api");

					if (configuration != null) {
						configuration.getAllDependencies().forEach(dependency -> {
							if (dependency.getName().equals(project.getName()) && !toUpdate.containsKey(childProject)) {
								System.out.println("Bumping patch of " + childProject.getName() + " as it depends on " + project.getName());
								temp.put(childProject, 2);
							}
						});
					}
				});
			}

			if (temp.isEmpty()) {
				break;
			}

			toUpdate.putAll(temp);
		}

		var gradlePropertiesFile = getProject().file("gradle.properties");
		Map<String, ?> properties = getProject().getProperties();
		String text = java.nio.file.Files.readString(gradlePropertiesFile.toPath(), StandardCharsets.UTF_8);

		for (var entry : toUpdate.entrySet()) {
			Project project = entry.getKey();
			int index = entry.getValue();
			Object versionObject = properties.get(project.getName() + "-version");

			if (versionObject == null) {
				throw new NullPointerException("Could not find version for " + project.getName());
			}

			String version = versionObject.toString();
			String[] split = version.split("\\.");
			split[index] = Integer.toString(Integer.parseInt(split[index]) + 1);

			for (int i = index + 1; i < split.length; i++) {
				split[i] = "0";
			}

			String newVersion = String.join(".", split);
			System.out.println(project.getName() + ": " + version + " -> " + newVersion);
			text = text.replace(project.getName() + "-version=" + version, project.getName() + "-version=" + newVersion);
		}

		java.nio.file.Files.writeString(gradlePropertiesFile.toPath(), text, StandardCharsets.UTF_8);
	}
}
