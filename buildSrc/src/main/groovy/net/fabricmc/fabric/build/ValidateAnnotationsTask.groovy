package net.fabricmc.fabric.build

import org.gradle.api.tasks.SourceTask
import org.gradle.api.tasks.TaskAction

abstract class ValidateAnnotationsTask extends SourceTask {
	private static final def API_STATUS_INTERNAL = ~/@ApiStatus\.Internal/
	private static final def ENVIRONMENT = ~/@Environment/

	@TaskAction
	void run() {
		for (def dir in ['api', 'impl', 'mixin', 'test']) {
			getSource().matching { include "net/fabricmc/fabric/$dir/" }.forEach {
				if (it.isDirectory()) {
					return
				}

				def contents = it.text

				if (ENVIRONMENT.matcher(contents).find()) {
					throw new RuntimeException("Found @Environment annotation in file: $it")
				}

				if (dir != "api" && API_STATUS_INTERNAL.matcher(contents).find()) {
					throw new RuntimeException("Found @ApiStatus.Internal in implementation file: " + it)
				}
			}
		}
	}
}
