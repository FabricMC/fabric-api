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

package net.fabricmc.fabric.api.resource;

import java.util.Collection;
import java.util.Collections;

import net.minecraft.resource.ResourceReloader;
import net.minecraft.util.Identifier;

/**
 * Interface for "identifiable" resource reload listeners.
 *
 * <p>"Identifiable" listeners have a unique identifier, which can be depended on,
 * and can provide dependencies that they would like to see executed before
 * themselves.
 *
 * @see net.fabricmc.fabric.api.resource.v1.reloader.ResourceReloaderKeys
 * @deprecated Use {@link net.fabricmc.fabric.api.resource.v1.ResourceLoader#registerReloader(Identifier, ResourceReloader)}
 * and {@link net.fabricmc.fabric.api.resource.v1.ResourceLoader#addReloaderOrdering(Identifier, Identifier)} instead.
 */
@Deprecated
public interface IdentifiableResourceReloadListener extends ResourceReloader {
	/**
	 * @return The unique identifier of this listener.
	 */
	Identifier getFabricId();

	/**
	 * @return The identifiers of listeners this listener expects to have been
	 * executed before itself. Please keep in mind that this only takes effect
	 * during the application stage!
	 */
	default Collection<Identifier> getFabricDependencies() {
		return Collections.emptyList();
	}
}
