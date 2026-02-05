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

package net.fabricmc.fabric.api.client.renderer.v1;

import net.fabricmc.fabric.impl.client.renderer.RendererManager;

/**
 * An abstraction for registering {@link Renderer} implementations.
 *
 * @implSpec Before Minecraft is initialized, implementations of {@link RendererProvider} are
 * loaded via {@link java.util.ServiceLoader}, after which {@link #getRenderer()} is called.
 */
public interface RendererProvider {
	/**
	 * Gets the current, chosen {@link RendererProvider} or finds one if it has not yet been chosen.
	 *
	 * @return the current {@link RendererProvider}.
	 */
	static RendererProvider get() {
		return RendererManager.getOrLoadRendererProvider();
	}

	/**
	 * Get or instantiate an implementation of {@link Renderer}.
	 *
	 * @apiNote Do not call this method before the {@link RendererReadyEntrypoint} has been invoked.
	 *
	 * @return an instance of the currently registered {@link Renderer}.
	 * @implSpec This method should instantiate an implementation of {@link Renderer} the first time
	 * it is invoked and return that instance for any subsequent calls.
	 */
	Renderer getRenderer();

	/**
	 * @return this renderer's unique ID.
	 * @implSpec This should be the renderer's mod ID, but that is not guaranteed. Additionally,
	 * changing this string is considered a breaking change for users of {@link RendererProvider}.
	 */
	String id();

	/**
	 * The higher a renderer's priority is, the more likely it is to be loaded. So, the
	 * {@link RendererProvider} with the highest priority is loaded.
	 *
	 * @return this renderer's priority.
	 * @implSpec Implementations of {@link Renderer} should use priority {@code 1000} in most cases.
	 * However, they may choose any priority or even change priorities based on some conditions.
	 * Implementors should avoid priorities of {@code 0} or below as that is Indigo's priority.
	 */
	default int priority() {
		return 1000;
	}
}
