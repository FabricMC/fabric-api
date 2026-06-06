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
import java.util.Map;

import groovy.json.JsonSlurper;
import org.gradle.api.DefaultTask;
import org.gradle.api.GradleException;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.TaskAction;

public abstract class ValidateModuleTask extends DefaultTask {
	@InputFile
	public abstract RegularFileProperty getFmj();

	@Input
	public abstract Property<String> getProjectName();

	@Input
	public abstract Property<String> getProjectPath();

	@Input
	public abstract Property<String> getLoaderVersion();

	public ValidateModuleTask() {
		setGroup("verification");
		getOutputs().upToDateWhen(task -> true);

		File file = getProject().file("src/main/resources/fabric.mod.json");

		if (!file.exists()) {
			file = getProject().file("src/client/resources/fabric.mod.json");
		}

		getFmj().set(file);
		getProjectName().set(getProject().getName());
		getProjectPath().set(getProject().getPath());
		getLoaderVersion().set(FabricApiBuildUtils.version(getProject(), "fabric-loader"));
	}

	@TaskAction
	public void validate() {
		@SuppressWarnings("unchecked")
		Map<String, Object> json = (Map<String, Object>) new JsonSlurper().parse(getFmj().get().getAsFile());
		Object customObject = json.get("custom");

		if (customObject == null) {
			throw new GradleException("Module " + getProjectName().get() + " does not have a custom value containing module lifecycle!");
		}

		@SuppressWarnings("unchecked")
		Map<String, Object> custom = (Map<String, Object>) customObject;
		Object moduleLifecycle = custom.get("fabric-api:module-lifecycle");

		if (moduleLifecycle == null) {
			throw new GradleException("Module " + getProjectName().get() + " does not have module lifecycle in custom values!");
		}

		if (!(moduleLifecycle instanceof String)) {
			throw new GradleException("Module " + getProjectName().get() + " has an invalid module lifecycle value. The value must be a string but read a " + moduleLifecycle.getClass());
		}

		switch ((String) moduleLifecycle) {
		case "stable", "experimental" -> { }
		case "deprecated" -> {
			if (!getProjectPath().get().startsWith(":deprecated")) {
				throw new GradleException("Deprecated module " + getProjectName().get() + " must be in the deprecated sub directory.");
			}
		}
		default -> throw new GradleException("Module " + getProjectName().get() + " has an invalid module lifecycle " + moduleLifecycle);
		}

		Object dependsObject = json.get("depends");

		if (dependsObject == null) {
			throw new GradleException("Module " + getProjectName().get() + " does not have a depends value!");
		}

		@SuppressWarnings("unchecked")
		Map<String, Object> depends = (Map<String, Object>) dependsObject;
		String expectedLoaderVersion = ">=" + getLoaderVersion().get();

		if (!expectedLoaderVersion.equals(depends.get("fabricloader"))) {
			throw new GradleException("Module " + getProjectName().get() + " does not have a valid fabricloader value! Got \"" + depends.get("fabricloader") + "\" but expected \"" + expectedLoaderVersion + "\"");
		}
	}
}
