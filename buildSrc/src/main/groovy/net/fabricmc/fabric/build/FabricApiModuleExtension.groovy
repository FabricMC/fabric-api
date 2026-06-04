package net.fabricmc.fabric.build

import org.gradle.api.Project
import org.gradle.api.publish.maven.MavenPublication

class FabricApiModuleExtension {
	final Project project

	FabricApiModuleExtension(Project project) {
		this.project = project
	}

	void moduleDependencies(List<String> depNames) {
		def deps = depNames.collect { project.dependencies.project(path: FabricApiBuildUtils.projectPath(it)) }
		def depProjects = depNames.collect { project.rootProject.findProject(FabricApiBuildUtils.projectPath(it)) }
		def clientOutputs = depProjects.collect { it.sourceSets.client.output }

		deps.each {
			project.dependencies.add("api", it)
		}

		clientOutputs.each {
			project.dependencies.add("clientImplementation", it)
			project.sourceSets.client.compileClasspath += it
			project.sourceSets.client.runtimeClasspath += it
		}

		project.tasks.named("compileClientJava").configure {
			depProjects.each { dependsOn it.tasks.named("clientClasses") }
		}
		project.afterEvaluate {
			project.tasks.named("compileClientJava").configure {
				doFirst {
					setClasspath(getClasspath().plus(project.files(depProjects.collect {
						it.layout.buildDirectory.dir("classes/java/client")
					})))
				}
			}
			project.tasks.named("compileTestmodClientJava").configure {
				doFirst {
					setClasspath(getClasspath().plus(project.files(depProjects.collect {
						it.layout.buildDirectory.dir("classes/java/client")
					})))
				}
			}
		}

		def depNodes = depNames.collect {
			def depProject = project.rootProject.findProject(FabricApiBuildUtils.projectPath(it))
			[
				groupId: depProject.group,
				artifactId: depProject.name,
				version: FabricApiBuildUtils.moduleVersion(depProject),
				scope: "compile"
			]
		}

		project.publishing {
			publications {
				mavenJava(MavenPublication) {
					pom.withXml {
						FabricApiBuildUtils.appendPomDependencies(asNode(), depNodes)
					}
				}
			}
		}
	}

	void testDependencies(List<String> depNames) {
		def deps = depNames.collect { project.dependencies.project(path: FabricApiBuildUtils.projectPath(it)) }
		def depProjects = depNames.collect { project.rootProject.findProject(FabricApiBuildUtils.projectPath(it)) }
		def clientOutputs = depProjects.collect { it.sourceSets.client.output }

		deps.each {
			project.dependencies.add("testmodImplementation", it)
		}

		clientOutputs.each {
			project.dependencies.add("testmodClientImplementation", it)
			project.sourceSets.testmodClient.compileClasspath += it
			project.sourceSets.testmodClient.runtimeClasspath += it
		}

		project.tasks.named("compileTestmodClientJava").configure {
			depProjects.each { dependsOn it.tasks.named("clientClasses") }
		}
		project.afterEvaluate {
			project.tasks.named("compileTestmodClientJava").configure {
				doFirst {
					setClasspath(getClasspath().plus(project.files(depProjects.collect {
						it.layout.buildDirectory.dir("classes/java/client")
					})))
				}
			}
		}
	}
}
