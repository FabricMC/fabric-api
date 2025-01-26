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

package net.fabricmc.fabric.api.client.gametest.v1.screenshot;

import com.google.common.base.Preconditions;
import org.jetbrains.annotations.ApiStatus;

import net.fabricmc.fabric.impl.client.gametest.screenshot.TestScreenshotOptionsImpl;

/**
 * Options to customize a screenshot.
 */
@ApiStatus.NonExtendable
public interface TestScreenshotOptions extends TestScreenshotCommonOptions<TestScreenshotOptions> {
	/**
	 * Creates a {@link TestScreenshotOptions} with the given screenshot name.
	 *
	 * @param name The name of the screenshot
	 * @return The new screenshot options instance
	 */
	static TestScreenshotOptions of(String name) {
		Preconditions.checkNotNull(name, "name");
		return new TestScreenshotOptionsImpl(name);
	}
}
