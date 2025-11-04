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

package net.fabricmc.fabric.impl.gametest;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import net.minecraft.gametest.framework.JUnitLikeTestReporter;

/**
 * An extension of {@link JUnitLikeTestReporter} which creates the destination directory before saving
 * the report.
 */
final class SavingXmlReportingTestCompletionListener extends JUnitLikeTestReporter {
	SavingXmlReportingTestCompletionListener(File file) throws ParserConfigurationException {
		super(file);
	}

	@Override
	public void save(File file) throws TransformerException {
		try {
			Files.createDirectories(file.toPath().getParent());
		} catch (IOException e) {
			throw new TransformerException("Failed to create parent directory", e);
		}

		super.save(file);
	}
}
