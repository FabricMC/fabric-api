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
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import groovy.json.JsonSlurper;
import groovy.util.Node;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.MinimalExternalModuleDependency;
import org.gradle.api.artifacts.VersionCatalog;
import org.gradle.api.artifacts.VersionCatalogsExtension;
import org.gradle.api.artifacts.dsl.RepositoryHandler;
import org.gradle.api.attributes.Attribute;
import org.gradle.api.attributes.AttributeContainer;
import org.gradle.api.attributes.Category;
import org.gradle.api.attributes.DocsType;
import org.gradle.api.attributes.LibraryElements;
import org.gradle.api.attributes.Usage;
import org.gradle.api.provider.Provider;
import org.gradle.api.publish.maven.MavenPom;

public final class FabricApiBuildUtils {
	public static final Set<String> META_PROJECTS = Set.of(
			"deprecated",
			"fabric-api-bom",
			"fabric-api-catalog"
	);

	public static final List<String> DEV_ONLY_MODULES = List.of(
			"fabric-client-gametest-api-v1",
			"fabric-gametest-api-v1"
	);

	private static final Attribute<String> SOURCE_SET_ATTRIBUTE = Attribute.of("net.fabricmc.fabric-api.source-set", String.class);

	private FabricApiBuildUtils() {
	}

	public static boolean isMetaProject(Project project) {
		return META_PROJECTS.contains(project.getName());
	}

	public static boolean isFabricModule(Project project) {
		return !isMetaProject(project);
	}

	public static String moduleName(String notation) {
		return notation.startsWith(":") ? notation.substring(1) : notation;
	}

	public static String projectPath(String notation) {
		return notation.startsWith(":") ? notation : ":" + notation;
	}

	public static String rootVersion(Project project) {
		Provider<String> branchProvider = project.getProviders().of(GitBranchValueSource.class, spec -> { });
		String suffix = project.getProviders().environmentVariable("CI").isPresent()
				? branchProvider.get().replace("/", "_")
				: "local";
		return project.findProperty("version") + "+" + suffix;
	}

	public static String moduleVersion(Project project) {
		Object version = project.findProperty(project.getName() + "-version");

		if (version == null) {
			throw new NullPointerException("Could not find version for " + project.getName());
		}

		if (!project.getProviders().environmentVariable("CI").isPresent()) {
			return version + "+local";
		}

		Provider<String> hashProvider = project.getProviders().of(CommitHashValueSource.class, spec -> {
			spec.getParameters().getDirectory().set(project.getName());
		});

		return version + "+" + hashProvider.get().substring(0, 8) + sha256Hex(project.getRootProject().property("minecraft_version").toString()).substring(0, 2);
	}

	public static String version(Project project, String alias) {
		return libs(project).findVersion(alias)
				.orElseThrow(() -> new IllegalStateException("Missing version catalog entry: " + alias))
				.getRequiredVersion();
	}

	public static Provider<MinimalExternalModuleDependency> library(Project project, String alias) {
		return libs(project).findLibrary(alias)
				.orElseThrow(() -> new IllegalStateException("Missing version catalog entry: " + alias));
	}

	public static void configureInternalDocumentationSourcesVariant(Project project, Configuration configuration, String capabilityName) {
		configureInternalCapability(project, configuration, capabilityName);
		configureDocumentationSourcesVariant(configuration);
	}

	public static void configureInternalJavaApiClassesVariant(Project project, Configuration configuration, String capabilityName) {
		configureInternalCapability(project, configuration, capabilityName);
		configureJavaApiClassesVariant(configuration);
	}

	public static void configureInternalJavaRuntimeClassesVariant(Project project, Configuration configuration, String capabilityName, String sourceSetName) {
		configureInternalCapability(project, configuration, capabilityName);
		configureJavaRuntimeClassesVariant(configuration, sourceSetName);
	}

	public static void configureInternalJavaRuntimeJarVariant(Project project, Configuration configuration, String capabilityName) {
		configureInternalCapability(project, configuration, capabilityName);
		configureJavaRuntimeJarVariant(configuration);
	}

	public static void configureDocumentationSourcesVariant(Configuration configuration) {
		configureDocumentationSourcesAttributes(configuration.getAttributes());
	}

	public static void configureJavaApiClassesVariant(Configuration configuration) {
		configureJavaApiClassesAttributes(configuration.getAttributes());
	}

	public static void configureJavaRuntimeClassesVariant(Configuration configuration, String sourceSetName) {
		configureJavaRuntimeClassesAttributes(configuration.getAttributes(), sourceSetName);
	}

	public static void configureJavaRuntimeJarVariant(Configuration configuration) {
		configureJavaRuntimeJarAttributes(configuration.getAttributes());
	}

	private static void configureInternalCapability(Project project, Configuration configuration, String capabilityName) {
		configuration.getOutgoing().capability("%s:%s-fabric-api-%s:%s".formatted(project.getGroup(), project.getName(), capabilityName, project.getVersion()));
	}

	private static void configureDocumentationSourcesAttributes(AttributeContainer attributes) {
		attributes.attribute(Category.CATEGORY_ATTRIBUTE, attributes.named(Category.class, Category.DOCUMENTATION));
		attributes.attribute(DocsType.DOCS_TYPE_ATTRIBUTE, attributes.named(DocsType.class, DocsType.SOURCES));
		attributes.attribute(LibraryElements.LIBRARY_ELEMENTS_ATTRIBUTE, attributes.named(LibraryElements.class, "directory"));
	}

	private static void configureJavaApiClassesAttributes(AttributeContainer attributes) {
		attributes.attribute(Category.CATEGORY_ATTRIBUTE, attributes.named(Category.class, Category.LIBRARY));
		attributes.attribute(Usage.USAGE_ATTRIBUTE, attributes.named(Usage.class, Usage.JAVA_API));
		attributes.attribute(LibraryElements.LIBRARY_ELEMENTS_ATTRIBUTE, attributes.named(LibraryElements.class, LibraryElements.CLASSES));
	}

	private static void configureJavaRuntimeClassesAttributes(AttributeContainer attributes, String sourceSetName) {
		attributes.attribute(Category.CATEGORY_ATTRIBUTE, attributes.named(Category.class, Category.LIBRARY));
		attributes.attribute(Usage.USAGE_ATTRIBUTE, attributes.named(Usage.class, Usage.JAVA_RUNTIME));
		attributes.attribute(LibraryElements.LIBRARY_ELEMENTS_ATTRIBUTE, attributes.named(LibraryElements.class, LibraryElements.CLASSES));
		attributes.attribute(SOURCE_SET_ATTRIBUTE, sourceSetName);
	}

	private static void configureJavaRuntimeJarAttributes(AttributeContainer attributes) {
		attributes.attribute(Category.CATEGORY_ATTRIBUTE, attributes.named(Category.class, Category.LIBRARY));
		attributes.attribute(Usage.USAGE_ATTRIBUTE, attributes.named(Usage.class, Usage.JAVA_RUNTIME));
		attributes.attribute(LibraryElements.LIBRARY_ELEMENTS_ATTRIBUTE, attributes.named(LibraryElements.class, LibraryElements.JAR));
	}

	private static VersionCatalog libs(Project project) {
		return project.getExtensions().getByType(VersionCatalogsExtension.class).named("libs");
	}

	public static void setupRepositories(Project project, RepositoryHandler repositories) {
		if (project.getProviders().environmentVariable("MAVEN_URL").isPresent()) {
			repositories.maven(repository -> {
				repository.setUrl(project.getProviders().environmentVariable("MAVEN_URL"));
				repository.credentials(credentials -> {
					credentials.setUsername(project.getProviders().environmentVariable("MAVEN_USERNAME").get());
					credentials.setPassword(project.getProviders().environmentVariable("MAVEN_PASSWORD").get());
				});
			});
		}
	}

	public static boolean publishedArtifactExists(Project project, String projectName, String projectVersion) throws IOException, InterruptedException {
		if (!project.getProviders().environmentVariable("MAVEN_URL").isPresent()) {
			return false;
		}

		String artifactPath = "https://maven.fabricmc.net/net/fabricmc/fabric-api/%s/%s/%s-%s.pom".formatted(projectName, projectVersion, projectName, projectVersion);
		HttpRequest request = HttpRequest.newBuilder()
				.uri(URI.create(artifactPath))
				.method("HEAD", HttpRequest.BodyPublishers.noBody())
				.build();

		try (HttpClient client = HttpClient.newHttpClient()) {
			HttpResponse<Void> response = client.send(request, HttpResponse.BodyHandlers.discarding());
			return response.statusCode() == 200;
		}
	}

	public static void addPomMetadataInformation(Project project, MavenPom pom) {
		File modJsonFile = project.file("src/main/resources/fabric.mod.json");

		if (!modJsonFile.exists()) {
			modJsonFile = project.file("src/client/resources/fabric.mod.json");
		}

		@SuppressWarnings("unchecked")
		Map<String, Object> modJson = (Map<String, Object>) new JsonSlurper().parse(modJsonFile);
		pom.getName().set(modJson.get("name").toString());
		pom.getUrl().set("https://github.com/FabricMC/fabric/tree/HEAD/" + project.getRootDir().toPath().relativize(project.getProjectDir().toPath()));
		pom.getDescription().set(modJson.get("description").toString());
		pom.licenses(licenses -> licenses.license(license -> {
			license.getName().set("Apache-2.0");
			license.getUrl().set("https://github.com/FabricMC/fabric/blob/HEAD/LICENSE");
		}));
		pom.developers(developers -> developers.developer(developer -> {
			developer.getName().set("FabricMC");
			developer.getUrl().set("https://fabricmc.net/");
		}));
		pom.scm(scm -> {
			scm.getConnection().set("scm:git:https://github.com/FabricMC/fabric.git");
			scm.getUrl().set("https://github.com/FabricMC/fabric");
			scm.getDeveloperConnection().set("scm:git:git@github.com:FabricMC/fabric.git");
		});
		pom.issueManagement(issueManagement -> {
			issueManagement.getSystem().set("GitHub");
			issueManagement.getUrl().set("https://github.com/FabricMC/fabric/issues");
		});
	}

	public static void appendPomDependencies(Node pomNode, List<Map<String, String>> dependencies) {
		Node depsNode = pomNode.appendNode("dependencies");

		for (Map<String, String> dependency : dependencies) {
			Node depNode = depsNode.appendNode("dependency");

			for (Entry<String, String> entry : dependency.entrySet()) {
				depNode.appendNode(entry.getKey(), entry.getValue());
			}
		}
	}

	private static String sha256Hex(String input) {
		try {
			byte[] digest = MessageDigest.getInstance("SHA-256").digest(input.getBytes(StandardCharsets.UTF_8));
			StringBuilder builder = new StringBuilder(digest.length * 2);

			for (byte value : digest) {
				builder.append(String.format("%02x", value));
			}

			return builder.toString();
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException("Could not load SHA-256 digest", e);
		}
	}
}
