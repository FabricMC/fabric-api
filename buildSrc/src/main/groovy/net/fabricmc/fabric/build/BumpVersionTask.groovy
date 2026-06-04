package net.fabricmc.fabric.build

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

abstract class BumpVersionTask extends DefaultTask {
	BumpVersionTask() {
		group = "publishing"
		outputs.upToDateWhen { false }
	}

	@TaskAction
	void runTask() {
		def scanner = new Scanner(System.in)
		def toUpdate = [:]

		while (true) {
			println "Enter module name to update, or done to continue"
			def input = scanner.nextLine()

			if (input == "done") {
				break
			}

			if (input == "allPatch") {
				project.getChildProjects().values().forEach {
					if (!FabricApiBuildUtils.isFabricModule(it)) {
						return
					}

					toUpdate.put(it, 2)
				}

				break
			}

			def subProject = project.childProjects[input] ?: project.childProjects["deprecated"].childProjects[input]

			if (!subProject) {
				println "Could not find project with name: $input"
				continue
			}

			while (true) {
				println "Bump version for ${subProject.name}:"
				println "0) Bump Major"
				println "1) Bump Minor"
				println "2) Bump Patch"

				input = scanner.nextLine()

				if (!(input in ["0", "1", "2"])) {
					println "Invalid input"
					continue
				}

				toUpdate.put(subProject, input as Integer)
				break
			}
		}

		while (true) {
			def temp = [:]

			toUpdate.keySet().forEach { p ->
				project.allprojects.each { cp ->
					if (!FabricApiBuildUtils.isFabricModule(cp) || cp == project) {
						return
					}

					def config = cp.configurations.findByName("api")
					config?.allDependencies?.forEach { dep ->
						if (dep.name == p.name && !toUpdate.containsKey(cp)) {
							println "Bumping patch of ${cp.name} as it depends on ${p.name}"
							temp.put(cp, 2)
						}
					}
				}
			}

			if (temp.isEmpty()) {
				break
			}

			toUpdate.putAll(temp)
		}

		def gpFile = project.file("gradle.properties")
		def props = project.properties
		def text = gpFile.text

		toUpdate.forEach { p, i ->
			def version = props."${p.name}-version"

			if (!version) {
				throw new NullPointerException("Could not find version for " + p.name)
			}

			def split = version.split("\\.")
			split[i] = (split[i] as Integer) + 1
			for (j in (i + 1)..<split.length) {
				split[j] = 0
			}
			def newVersion = split.join(".")

			println "${p.name}: $version -> $newVersion"

			text = text.replace(
					"${p.name}-version=$version",
					"${p.name}-version=$newVersion"
					)
		}

		gpFile.text = text
	}
}
