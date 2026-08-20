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
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

import org.gradle.api.DefaultTask;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputDirectory;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.SkipWhenEmpty;
import org.gradle.api.tasks.TaskAction;

public abstract class GeneratePackageInfosTask extends DefaultTask {
	@InputFile
	public abstract RegularFileProperty getHeader();

	@Input
	public abstract Property<String> getProjectName();

	@SkipWhenEmpty
	@InputDirectory
	public abstract DirectoryProperty getSourceRoot();

	@OutputDirectory
	public abstract DirectoryProperty getOutputDir();

	public GeneratePackageInfosTask() {
		getProjectName().set(getProject().getName());
	}

	@TaskAction
	public void run() throws IOException {
		Path output = getOutputDir().get().getAsFile().toPath();
		deleteDirectory(output);
		String headerText = Files.readString(getHeader().get().getAsFile().toPath(), StandardCharsets.UTF_8).trim();
		Path root = getSourceRoot().get().getAsFile().toPath();

		Files.walkFileTree(root, new SimpleFileVisitor<>() {
			@Override
			public FileVisitResult preVisitDirectory(Path directory, BasicFileAttributes attrs) throws IOException {
				if (!containsJavaFile(directory)) {
					return FileVisitResult.CONTINUE;
				}

				Path relativePath = root.relativize(directory);
				String packageName = relativePath.toString().replace(java.io.File.separatorChar, '.');

				if (packageName.equals("net.fabricmc.fabric.api.util") && getProjectName().get().equals("fabric-content-registries-v0")) {
					return FileVisitResult.CONTINUE;
				}

				boolean isImpl = relativePath.toString().matches("^(net[/\\\\]fabricmc[/\\\\]fabric[/\\\\](impl|mixin)).*");
				Path existingPackageInfo = directory.resolve("package-info.java");

				if (Files.exists(existingPackageInfo)) {
					String text = Files.readString(existingPackageInfo, StandardCharsets.UTF_8);

					if (!text.contains("@NullMarked")) {
						throw new RuntimeException("package-info.java " + existingPackageInfo + " is missing @NullMarked annotation.");
					} else if (isImpl && !text.contains("@ApiStatus.Internal")) {
						throw new RuntimeException("Impl package-info.java " + existingPackageInfo + " is missing @ApiStatus.Internal annotation.");
					}

					return FileVisitResult.CONTINUE;
				}

				Path target = output.resolve(relativePath);
				Files.createDirectories(target);
				Files.writeString(target.resolve("package-info.java"), packageInfo(headerText, packageName, isImpl), StandardCharsets.UTF_8);
				return FileVisitResult.CONTINUE;
			}
		});
	}

	private static boolean containsJavaFile(Path directory) throws IOException {
		try (var stream = Files.list(directory)) {
			return stream.anyMatch(path -> Files.isRegularFile(path) && path.getFileName().toString().endsWith(".java"));
		}
	}

	private String packageInfo(String headerText, String packageName, boolean isImpl) {
		if (isImpl) {
			return """
					%s
					/**
					 * Implementation code for %s.
					 */
					@ApiStatus.Internal
					@NullMarked
					package %s;

					import org.jetbrains.annotations.ApiStatus;
					import org.jspecify.annotations.NullMarked;
					""".formatted(headerText, getProjectName().get(), packageName).stripIndent();
		}

		return """
				%s
				/**
				 * API code for %s.
				 */
				@NullMarked
				package %s;

				import org.jspecify.annotations.NullMarked;
				""".formatted(headerText, getProjectName().get(), packageName).stripIndent();
	}

	private static void deleteDirectory(Path directory) throws IOException {
		if (!Files.exists(directory)) {
			return;
		}

		Files.walkFileTree(directory, new SimpleFileVisitor<>() {
			@Override
			public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
				Files.delete(file);
				return FileVisitResult.CONTINUE;
			}

			@Override
			public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
				Files.delete(dir);
				return FileVisitResult.CONTINUE;
			}
		});
	}
}
