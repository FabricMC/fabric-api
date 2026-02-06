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

package net.fabricmc.fabric.impl.client.renderer;

import java.util.ServiceLoader;

import net.fabricmc.fabric.api.client.renderer.v1.Renderer;
import net.fabricmc.fabric.api.client.renderer.v1.RendererProvider;

public final class RendererManager {
	private static RendererProvider chosenRendererProvider;
	private static Renderer activeRenderer;

	private RendererManager() {
	}

	public static Renderer getRenderer() {
		if (activeRenderer != null) {
			return activeRenderer;
		}

		activeRenderer = getOrLoadRendererProvider().getRenderer();
		return activeRenderer;
	}

	public static RendererProvider getOrLoadRendererProvider() {
		if (chosenRendererProvider != null) {
			return chosenRendererProvider;
		}

		// Because Stream is banned, we have to sort manually even though Stream#reduce would be so
		// much more readable.
		ServiceLoader<RendererProvider> serviceLoader = ServiceLoader.load(RendererProvider.class);
		int highestPriority = Integer.MIN_VALUE;
		RendererProvider rendererProvider = null;

		for (RendererProvider next : serviceLoader) {
			if (next.priority() > highestPriority) {
				rendererProvider = next;
				highestPriority = next.priority();
			}
		}

		if (rendererProvider != null) {
			chosenRendererProvider = rendererProvider;
			return rendererProvider;
		} else {
			throw new NullPointerException("A renderer plug-in has not been provided before Minecraft has loaded. This is unsupported.");
		}
	}
}
