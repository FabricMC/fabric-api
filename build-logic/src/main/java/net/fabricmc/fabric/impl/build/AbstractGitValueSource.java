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

import java.io.ByteArrayOutputStream;

import javax.inject.Inject;

import org.gradle.api.provider.ValueSource;
import org.gradle.api.provider.ValueSourceParameters;
import org.gradle.process.ExecOperations;
import org.gradle.process.ExecResult;

public abstract class AbstractGitValueSource<T, P extends ValueSourceParameters> implements ValueSource<T, P> {
	@Inject
	protected abstract ExecOperations getExecOperations();

	protected String git(String... args) {
		var outputStream = new ByteArrayOutputStream();
		ExecResult result = getExecOperations().exec(spec -> {
			spec.commandLine("git");
			spec.args((Object[]) args);
			spec.setStandardOutput(outputStream);
		});
		result.assertNormalExitValue();
		return outputStream.toString().trim();
	}
}
