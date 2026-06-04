package net.fabricmc.fabric.build

import groovy.json.JsonSlurper
import groovy.util.Node
import java.security.MessageDigest
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import net.fabricmc.fabric.impl.build.CommitHashValueSource
import net.fabricmc.fabric.impl.build.GitBranchValueSource
import org.gradle.api.Project
import org.gradle.api.artifacts.dsl.RepositoryHandler
import org.gradle.api.publish.maven.MavenPom

final class FabricApiBuildUtils {
	static final Set<String> META_PROJECTS = [
		'deprecated',
		'fabric-api-bom',
		'fabric-api-catalog'
	] as Set

	static final List<String> DEBUG_ARGS = [
		"-enableassertions",
		"-Dmixin.debug.verify=true",
		"-Dmixin.debug.countInjections=true",
		"-XX:+UseZGC",
		"-XX:+UseCompactObjectHeaders",
		"-XX:+AlwaysPreTouch",
		"-XX:+UseStringDeduplication"
	].asImmutable()

	static final List<String> DEV_ONLY_MODULES = [
		"fabric-client-gametest-api-v1",
		"fabric-gametest-api-v1"
	].asImmutable()

	private FabricApiBuildUtils() {
	}

	static boolean isMetaProject(Project project) {
		return META_PROJECTS.contains(project.name)
	}

	static boolean isFabricModule(Project project) {
		return !isMetaProject(project)
	}

	static String moduleName(String notation) {
		return notation.startsWith(":") ? notation.substring(1) : notation
	}

	static String projectPath(String notation) {
		return notation.startsWith(":") ? notation : ":${notation}"
	}

	static String rootVersion(Project project) {
		def branchProvider = project.providers.of(GitBranchValueSource.class) {}
		def suffix = project.providers.environmentVariable("CI").present
				? branchProvider.get().replace("/", "_")
				: "local"
		return "${project.findProperty('version')}+${suffix}"
	}

	static String moduleVersion(Project project) {
		def version = project.findProperty("${project.name}-version")

		if (!version) {
			throw new NullPointerException("Could not find version for " + project.name)
		}

		if (!project.providers.environmentVariable("CI").present) {
			return version + "+local"
		}

		def hashProvider = project.providers.of(CommitHashValueSource.class) {
			parameters.directory = project.name
		}

		return version + "+" + hashProvider.get().substring(0, 8) + sha256Hex(project.rootProject.minecraft_version).substring(0, 2)
	}

	static void setupRepositories(Project project, RepositoryHandler repositories) {
		if (project.providers.environmentVariable("MAVEN_URL").present) {
			repositories.maven {
				url = project.providers.environmentVariable("MAVEN_URL")
				credentials {
					username = project.providers.environmentVariable("MAVEN_USERNAME").get()
					password = project.providers.environmentVariable("MAVEN_PASSWORD").get()
				}
			}
		}
	}

	static boolean publishedArtifactExists(Project project, String projectName, String projectVersion) {
		if (!project.providers.environmentVariable("MAVEN_URL").present) {
			return false
		}

		def artifactPath = "https://maven.fabricmc.net/net/fabricmc/fabric-api/${projectName}/${projectVersion}/${projectName}-${projectVersion}.pom"

		return HttpClient.newHttpClient().withCloseable { client ->
			def request = HttpRequest.newBuilder()
					.uri(URI.create(artifactPath))
					.method("HEAD", HttpRequest.BodyPublishers.noBody())
					.build()

			def response = client.send(request, HttpResponse.BodyHandlers.discarding())
			response.statusCode() == 200
		}
	}

	static void addPomMetadataInformation(Project project, MavenPom pom) {
		def modJsonFile = project.file("src/main/resources/fabric.mod.json")

		if (!modJsonFile.exists()) {
			modJsonFile = project.file("src/client/resources/fabric.mod.json")
		}

		def modJson = new JsonSlurper().parse(modJsonFile)
		pom.name = modJson.name
		pom.url = "https://github.com/FabricMC/fabric/tree/HEAD/${project.rootDir.relativePath(project.projectDir)}"
		pom.description = modJson.description
		pom.licenses {
			license {
				name = "Apache-2.0"
				url = "https://github.com/FabricMC/fabric/blob/HEAD/LICENSE"
			}
		}
		pom.developers {
			developer {
				name = "FabricMC"
				url = "https://fabricmc.net/"
			}
		}
		pom.scm {
			connection = "scm:git:https://github.com/FabricMC/fabric.git"
			url = "https://github.com/FabricMC/fabric"
			developerConnection = "scm:git:git@github.com:FabricMC/fabric.git"
		}
		pom.issueManagement {
			system = "GitHub"
			url = "https://github.com/FabricMC/fabric/issues"
		}
	}

	static void appendPomDependencies(Node pomNode, List<Map<String, String>> dependencies) {
		def depsNode = pomNode.appendNode("dependencies")

		for (def dep in dependencies) {
			def depNode = depsNode.appendNode("dependency")

			for (def entry in dep) {
				depNode.appendNode(entry.key, entry.value)
			}
		}
	}

	private static String sha256Hex(String input) {
		def digest = MessageDigest.getInstance("SHA-256").digest(input.getBytes("UTF-8"))
		return digest.collect { String.format("%02x", it) }.join()
	}
}
