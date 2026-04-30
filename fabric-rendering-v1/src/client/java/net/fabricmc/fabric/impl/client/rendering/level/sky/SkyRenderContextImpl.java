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

package net.fabricmc.fabric.impl.client.rendering.level.sky;

import net.minecraft.client.renderer.SkyRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.state.level.SkyRenderState;

import net.fabricmc.fabric.api.client.rendering.v1.level.sky.CelestialRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.level.sky.SkyRenderContext;

public class SkyRenderContextImpl implements SkyRenderContext {
	private SkyRenderer skyRenderer;
	private SkyRenderState skyRenderState;
	private CameraRenderState cameraRenderState;

	public void prepare(final SkyRenderer skyRenderer, final SkyRenderState skyRenderState, final CameraRenderState cameraRenderState) {
		this.skyRenderer = skyRenderer;
		this.skyRenderState = skyRenderState;
		this.cameraRenderState = cameraRenderState;
	}

	@Override
	public SkyRenderer skyRenderer() {
		return this.skyRenderer;
	}

	@Override
	public SkyRenderState skyRenderState() {
		return this.skyRenderState;
	}

	@Override
	public CameraRenderState cameraRenderState() {
		return this.cameraRenderState;
	}

	public static class CelestialContextImpl extends SkyRenderContextImpl implements CelestialRenderContext {
		private Type type;

		public void setType(final Type type) {
			this.type = type;
		}

		@Override
		public Type type() {
			return this.type;
		}
	}
}
