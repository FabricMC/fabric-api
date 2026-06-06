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
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.gradle.api.DefaultTask;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.InputFiles;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.PathSensitive;
import org.gradle.api.tasks.PathSensitivity;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;

import net.fabricmc.loom.LoomGradleExtension;
import net.fabricmc.loom.api.mappings.layered.MappingsNamespace;

public abstract class AbstractGenerateClassTweakerTask extends DefaultTask {
	@InputFiles
	@PathSensitive(PathSensitivity.NONE)
	public abstract ConfigurableFileCollection getMinecraftJars();

	@InputFile
	@PathSensitive(PathSensitivity.RELATIVE)
	public abstract RegularFileProperty getTemplate();

	@OutputFile
	public abstract RegularFileProperty getOutputFile();

	public AbstractGenerateClassTweakerTask() {
		getMinecraftJars().from(getProject().provider(() -> LoomGradleExtension.get(getProject()).getMinecraftJars(MappingsNamespace.OFFICIAL)));
	}

	protected void addHeader(List<String> lines, String format) throws IOException {
		addHeader(lines, format, false);
	}

	protected void addHeader(List<String> lines, String format, boolean preserveTemplateTrailingNewline) throws IOException {
		lines.add(format);
		lines.add("");
		lines.add("# DO NOT EDIT BY HAND! This file is generated automatically.");
		lines.add("# Edit \"template.classtweaker\" instead then run \"gradlew generateClassTweaker\".");
		lines.add("");
		Path template = getTemplate().get().getAsFile().toPath();
		lines.addAll(Files.readAllLines(template, StandardCharsets.UTF_8));

		if (preserveTemplateTrailingNewline && Files.readString(template, StandardCharsets.UTF_8).endsWith("\n")) {
			lines.add("");
		}
	}

	protected void writeOutput(List<String> lines) throws IOException {
		File outputFile = getOutputFile().get().getAsFile();
		Files.createDirectories(outputFile.toPath().getParent());
		Files.writeString(outputFile.toPath(), String.join("\n", lines) + "\n", StandardCharsets.UTF_8);
	}

	protected Map<String, ClassNode> readClasses() throws IOException {
		Map<String, ClassNode> classes = new TreeMap<>();

		for (File input : getMinecraftJars().getFiles()) {
			readClasses(input, classes);
		}

		return classes;
	}

	protected ClassNode readClass(String name) throws IOException {
		return readClass(name, ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
	}

	protected List<ClassNode> readPackageClasses(String packageName) throws IOException {
		return getMinecraftJars().getFiles().stream()
				.flatMap(input -> {
					try {
						return readPackageClasses(input, packageName).stream();
					} catch (IOException e) {
						throw new RuntimeException("Failed to read package " + packageName + " from " + input, e);
					}
				})
				.toList();
	}

	private static void readClasses(File input, Map<String, ClassNode> classes) throws IOException {
		try (ZipFile zip = new ZipFile(input)) {
			for (Enumeration<? extends ZipEntry> entries = zip.entries(); entries.hasMoreElements(); ) {
				ZipEntry entry = entries.nextElement();

				if (!entry.getName().endsWith(".class")) {
					continue;
				}

				classes.put(entry.getName().replace(".class", ""), readClass(zip, entry.getName(), ClassReader.SKIP_CODE));
			}
		}
	}

	private ClassNode readClass(String name, int flags) throws IOException {
		String path = name + ".class";

		for (File input : getMinecraftJars().getFiles()) {
			try (ZipFile zip = new ZipFile(input)) {
				if (zip.getEntry(path) != null) {
					return readClass(zip, path, flags);
				}
			}
		}

		throw new IOException("Missing class " + path + " in " + getMinecraftJars().getFiles());
	}

	private static List<ClassNode> readPackageClasses(File input, String packageName) throws IOException {
		String prefix = packageName + "/";

		try (ZipFile zip = new ZipFile(input)) {
			return zip.stream()
					.filter(entry -> !entry.isDirectory())
					.filter(entry -> entry.getName().startsWith(prefix))
					.filter(entry -> entry.getName().endsWith(".class"))
					.filter(entry -> entry.getName().indexOf('/', prefix.length()) == -1)
					.map(entry -> {
						try {
							return readClass(zip, entry.getName(), ClassReader.SKIP_CODE);
						} catch (IOException e) {
							throw new RuntimeException("Failed to read " + entry.getName() + " from " + input, e);
						}
					})
					.toList();
		}
	}

	private static ClassNode readClass(ZipFile zip, String path, int flags) throws IOException {
		ZipEntry entry = zip.getEntry(path);

		if (entry == null) {
			throw new IOException("Missing class " + path + " in " + zip.getName());
		}

		try (InputStream inputStream = zip.getInputStream(entry)) {
			ClassReader reader = new ClassReader(inputStream);
			ClassNode classNode = new ClassNode();
			reader.accept(classNode, flags);
			return classNode;
		}
	}
}
