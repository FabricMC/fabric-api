package net.fabricmc.fabric.build

import groovy.json.JsonSlurper
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.TaskAction

abstract class ValidateModuleTask extends DefaultTask {
	@InputFile
	abstract RegularFileProperty getFmj()

	@Input
	abstract Property<String> getProjectName()

	@Input
	abstract Property<String> getProjectPath()

	@Input
	abstract Property<String> getLoaderVersion()

	ValidateModuleTask() {
		group = "verification"
		outputs.upToDateWhen { true }

		def file = project.file("src/main/resources/fabric.mod.json")

		if (!file.exists()) {
			file = project.file("src/client/resources/fabric.mod.json")
		}

		fmj.set(file)
		projectName.set(project.name)
		projectPath.set(project.path)
		loaderVersion.set(project.loader_version)
	}

	@TaskAction
	void validate() {
		def json = new JsonSlurper().parse(fmj.get().asFile)

		if (json.custom == null) {
			throw new GradleException("Module ${projectName.get()} does not have a custom value containing module lifecycle!")
		}

		def moduleLifecycle = json.custom.get("fabric-api:module-lifecycle")

		if (moduleLifecycle == null) {
			throw new GradleException("Module ${projectName.get()} does not have module lifecycle in custom values!")
		}

		if (!(moduleLifecycle instanceof String)) {
			throw new GradleException("Module ${projectName.get()} has an invalid module lifecycle value. The value must be a string but read a ${moduleLifecycle.class}")
		}

		switch (moduleLifecycle) {
			case "stable":
			case "experimental":
				break
			case "deprecated":
				if (!projectPath.get().startsWith(":deprecated")) {
					throw new GradleException("Deprecated module ${projectName.get()} must be in the deprecated sub directory.")
				}
				break
			default:
				throw new GradleException("Module ${projectName.get()} has an invalid module lifecycle ${moduleLifecycle}")
		}

		if (json.depends == null) {
			throw new GradleException("Module ${projectName.get()} does not have a depends value!")
		}

		if (json.depends.fabricloader != ">=${loaderVersion.get()}") {
			throw new GradleException("Module ${projectName.get()} does not have a valid fabricloader value! Got \"${json.depends.fabricloader}\" but expected \">=${loaderVersion.get()}\"")
		}
	}
}
