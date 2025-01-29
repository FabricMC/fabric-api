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

package net.fabricmc.fabric.impl.client.gametest.screenshot;

import java.nio.file.Path;

import com.google.common.base.Preconditions;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector2i;

import net.fabricmc.fabric.api.client.gametest.v1.screenshot.TestScreenshotCommonOptions;

public abstract class TestScreenshotCommonOptionsImpl<SELF extends TestScreenshotCommonOptions<SELF>> implements TestScreenshotCommonOptions<SELF> {
	public boolean counterPrefix = true;
	public float tickDelta = 1;
	@Nullable
	public Vector2i size;
	@Nullable
	public Path destinationDir;

	@Override
	public SELF disableCounterPrefix() {
		this.counterPrefix = false;
		return getThis();
	}

	@Override
	public SELF withTickDelta(float tickDelta) {
		Preconditions.checkArgument(tickDelta >= 0 && tickDelta <= 1, "tickDelta must be between 0 and 1");

		this.tickDelta = tickDelta;
		return getThis();
	}

	@Override
	public SELF withSize(int width, int height) {
		Preconditions.checkArgument(width > 0, "width must be positive");
		Preconditions.checkArgument(height > 0, "height must be positive");

		this.size = new Vector2i(width, height);
		return getThis();
	}

	@Override
	public SELF withDestinationDir(Path destinationDir) {
		Preconditions.checkNotNull(destinationDir, "destinationDir");

		this.destinationDir = destinationDir;
		return getThis();
	}

	@SuppressWarnings("unchecked")
	private SELF getThis() {
		return (SELF) this;
	}
}
