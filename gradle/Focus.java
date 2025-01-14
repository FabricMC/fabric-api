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

import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.Files;
import java.util.HashSet;
import java.io.IOException;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A script to only enable subprojects that you are working on.
 *
 * Usage:
 * - Run "java gradle/Focus.java fabric-networking-api-v1" to focus on fabric-networking-api-v1
 * - Run "java gradle/Focus.java fabric-networking-api-v1 fabric-sound-api-v1" to focus on fabric-networking-api-v1 and fabric-sound-api-v1
 * - Run "java gradle/Focus.java" to reset focus
 *
 * After running the script, refresh the Gradle project in your IDE.
 */
public class Focus {
	// Matches the content of moduleDependencies and testDependencies
	private static final Pattern OUTER_PATTERN = Pattern.compile("(?:moduleDependencies|testDependencies)\\s*\\(.*?\\[\\s*([\\s\\S]*?)\\s*\\]\\s*\\)\n");
	// Matches the dependency string
	private static final Pattern INNER_PATTERN = Pattern.compile("['\\\"]([^'\\\"]+)['\\\"]");

	public static void main(String[] args) throws IOException {
		Path path = Paths.get("focus.txt");

		if (args.length == 0) {
			Files.deleteIfExists(path);
			System.out.println("Reset focus");
			return;
		}

		Set<String> dependencies = new HashSet<>();

		for (String arg : args) {
			readDependencies(arg, dependencies);
		}

		// All modules depend on the following modules:
		readDependencies("fabric-gametest-api-v1", dependencies);
		readDependencies("fabric-client-gametest-api-v1", dependencies);
		readDependencies("fabric-registry-sync-v0", dependencies);

		System.out.println("Focusing on:\n" + String.join("\n", dependencies));

		Files.writeString(path, String.join("\n", dependencies));
	}

	private static void readDependencies(String project, Set<String> dependencies) throws IOException {
		if (dependencies.contains(project)) {
			return;
		}

		dependencies.add(project);

		Path buildGradlePath = Paths.get(project, "build.gradle");

		if (Files.notExists(buildGradlePath)) {
			throw new RuntimeException("Project not found: " + project);
		}

		String content = Files.readString(buildGradlePath);
		Matcher outerMatcher = OUTER_PATTERN.matcher(content);

		while (outerMatcher.find()) {
			String outerMatch = outerMatcher.group(1);
			Matcher innerMatcher = INNER_PATTERN.matcher(outerMatch);
			while (innerMatcher.find()) {
				String dependency = innerMatcher.group(1).replace(":", "");
				readDependencies(dependency, dependencies);
			}
		}
	}
}
