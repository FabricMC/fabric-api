package net.fabricmc.fabric.build

import java.nio.file.Files
import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.SkipWhenEmpty
import org.gradle.api.tasks.TaskAction

abstract class GeneratePackageInfosTask extends DefaultTask {
	@InputFile
	File header

	@Input
	abstract Property<String> getProjectName()

	@SkipWhenEmpty
	@InputDirectory
	final DirectoryProperty sourceRoot = project.objects.directoryProperty()

	@OutputDirectory
	final DirectoryProperty outputDir = project.objects.directoryProperty()

	GeneratePackageInfosTask() {
		projectName.set(project.name)
	}

	@TaskAction
	void run() {
		def output = outputDir.get().asFile.toPath()
		output.deleteDir()
		def headerText = header.readLines().join("\n")
		def root = sourceRoot.get().asFile.toPath()

		root.eachDirRecurse {
			def containsJava = Files.list(it).withCloseable { stream ->
				stream.anyMatch { path -> Files.isRegularFile(path) && path.fileName.toString().endsWith('.java') }
			}

			if (!containsJava) {
				return
			}

			def existingPackageInfo = it.resolve('package-info.java')
			if (Files.exists(existingPackageInfo)) {
				if (!existingPackageInfo.text.contains("@NullMarked")) {
					throw new RuntimeException("package-info.java ${existingPackageInfo} is missing @NullMarked annotation.")
				}

				return
			}

			def relativePath = root.relativize(it)
			def target = output.resolve(relativePath)
			Files.createDirectories(target)

			def packageName = relativePath.toString().replace(File.separator, '.')

			if (packageName == "net.fabricmc.fabric.api.util" && projectName.get() == "fabric-content-registries-v0") {
				return
			}

			def isImpl = relativePath.toString() =~ /^(net[\/\\]fabricmc[\/\\]fabric[\/\\](impl|mixin))/

			target.resolve('package-info.java').withWriter {
				if (isImpl) {
					it.write("""$headerText
						|/**
						| * Implementation code for ${projectName.get()}.
						| */
						|@ApiStatus.Internal
						|@NullMarked
						|package $packageName;
						|
						|import org.jetbrains.annotations.ApiStatus;
						|import org.jspecify.annotations.NullMarked;
						|""".stripMargin())
				} else {
					it.write("""$headerText
						|/**
						| * API code for ${projectName.get()}.
						| */
						|@NullMarked
						|package $packageName;
						|
						|import org.jspecify.annotations.NullMarked;
						|""".stripMargin())
				}
			}
		}
	}
}
