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

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import groovy.util.Node;
import org.gradle.api.Project;
import org.gradle.api.file.FileCollection;
import org.gradle.api.plugins.JavaPluginExtension;
import org.gradle.api.publish.PublishingExtension;
import org.gradle.api.publish.maven.MavenPublication;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.SourceSetContainer;
import org.gradle.api.tasks.compile.JavaCompile;

public class FabricApiModuleExtension {
	private final Project project;

	public FabricApiModuleExtension(Project project) {
		this.project = project;
	}

	public void moduleDependencies(String... dependencyNames) {
		project.getPluginManager().withPlugin("java", plugin -> configureModuleDependencies(List.of(dependencyNames)));
	}

	public void testDependencies(String... dependencyNames) {
		project.getPluginManager().withPlugin("java", plugin -> configureTestDependencies(List.of(dependencyNames)));
	}

	private void configureModuleDependencies(List<String> dependencyNames) {
		List<Project> dependencyProjects = dependencyProjects(dependencyNames);
		SourceSet clientSourceSet = sourceSets().getByName("client");
		String dependencyNamesInput = String.join(",", dependencyNames.stream().map(FabricApiBuildUtils::moduleName).toList());

		for (String dependencyName : dependencyNames) {
			project.getDependencies().add("api", project.getDependencies().project(Map.of("path", FabricApiBuildUtils.projectPath(dependencyName))));
		}

		project.getRootProject().getTasks().withType(BumpVersionTask.class).configureEach(task -> {
			task.getApiDependencies().put(project.getName(), dependencyNamesInput);
		});

		addDependencyClientOutputs("clientImplementation", clientSourceSet, dependencyProjects);
		dependsOnDependencyClientClasses("compileClientJava", dependencyProjects);
		addClientClassDirsToCompileTask("compileClientJava", dependencyProjects);
		addClientClassDirsToCompileTask("compileTestmodClientJava", dependencyProjects);

		List<Map<String, String>> dependencyNodes = new ArrayList<>();

		for (String dependencyName : dependencyNames) {
			Project dependencyProject = project.getRootProject().findProject(FabricApiBuildUtils.projectPath(dependencyName));
			var dependencyNode = new LinkedHashMap<String, String>();
			dependencyNode.put("groupId", project.getGroup().toString());
			dependencyNode.put("artifactId", dependencyProject.getName());
			dependencyNode.put("version", FabricApiBuildUtils.moduleVersion(dependencyProject));
			dependencyNode.put("scope", "compile");
			dependencyNodes.add(dependencyNode);
		}

		project.getPluginManager().withPlugin("maven-publish", plugin -> project.getExtensions().configure(PublishingExtension.class, publishing -> {
			publishing.getPublications().named("mavenJava", MavenPublication.class).configure(publication -> {
				publication.getPom().withXml(xml -> FabricApiBuildUtils.appendPomDependencies((Node) xml.asNode(), dependencyNodes));
			});
		}));
	}

	private void configureTestDependencies(List<String> dependencyNames) {
		List<Project> dependencyProjects = dependencyProjects(dependencyNames);
		SourceSet testmodClientSourceSet = sourceSets().getByName("testmodClient");

		for (String dependencyName : dependencyNames) {
			project.getDependencies().add("testmodImplementation", project.getDependencies().project(Map.of("path", FabricApiBuildUtils.projectPath(dependencyName))));
		}

		addDependencyClientOutputs("testmodClientImplementation", testmodClientSourceSet, dependencyProjects);
		dependsOnDependencyClientClasses("compileTestmodClientJava", dependencyProjects);
		addClientClassDirsToCompileTask("compileTestmodClientJava", dependencyProjects);
	}

	private List<Project> dependencyProjects(List<String> dependencyNames) {
		return dependencyNames.stream()
				.map(dependencyName -> project.getRootProject().findProject(FabricApiBuildUtils.projectPath(dependencyName)))
				.toList();
	}

	private SourceSetContainer sourceSets() {
		return project.getExtensions().getByType(JavaPluginExtension.class).getSourceSets();
	}

	private void addDependencyClientOutputs(String configurationName, SourceSet sourceSet, List<Project> dependencyProjects) {
		for (Project dependencyProject : dependencyProjects) {
			FileCollection clientOutput = project.files(
					dependencyProject.getLayout().getBuildDirectory().dir("classes/java/client"),
					dependencyProject.getLayout().getBuildDirectory().dir("resources/client")
			);
			project.getDependencies().add(configurationName, clientOutput);
			sourceSet.setCompileClasspath(sourceSet.getCompileClasspath().plus(clientOutput));
			sourceSet.setRuntimeClasspath(sourceSet.getRuntimeClasspath().plus(clientOutput));
		}
	}

	private void dependsOnDependencyClientClasses(String taskName, List<Project> dependencyProjects) {
		project.getTasks().named(taskName, JavaCompile.class).configure(task -> {
			for (Project dependencyProject : dependencyProjects) {
				task.dependsOn(dependencyProject.getPath() + ":clientClasses");
			}
		});
	}

	private void addClientClassDirsToCompileTask(String taskName, List<Project> dependencyProjects) {
		List<?> clientClassDirs = dependencyProjects.stream()
				.map(dependencyProject -> dependencyProject.getLayout().getBuildDirectory().dir("classes/java/client"))
				.toList();
		FileCollection clientClasses = project.files(clientClassDirs);

		project.getTasks().named(taskName, JavaCompile.class).configure(task -> task.setClasspath(task.getClasspath().plus(clientClasses)));
	}
}
