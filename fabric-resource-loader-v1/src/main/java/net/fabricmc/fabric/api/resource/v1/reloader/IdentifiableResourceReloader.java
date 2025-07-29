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

package net.fabricmc.fabric.api.resource.v1.reloader;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import org.jetbrains.annotations.NotNull;

import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceReloader;
import net.minecraft.util.Identifier;

/**
 * Interface for "identifiable" resource reloaders.
 *
 * <p>"Identifiable" resource reloaders have a unique identifier, which can be depended on,
 * and can provide dependencies that they would like to see executed before themselves.
 *
 * @see ResourceReloaderKeys
 */
public interface IdentifiableResourceReloader extends ResourceReloader {
	/**
	 * {@return the unique identifier of this resource reloader}
	 */
	Identifier getFabricId();

	@Override
	default String getName() {
		return this.getFabricId().toString();
	}

	/**
	 * Wraps the given resource reloader with an identifier.
	 *
	 * @param id the identifier of the resource reloader
	 * @param reloader the resource reloader to identify
	 * @return the identified wrapper of the given resource reloader
	 */
	static IdentifiableResourceReloader wrap(Identifier id, ResourceReloader reloader) {
		if (reloader instanceof IdentifiableResourceReloader identifiable) {
			if (!identifiable.getFabricId().equals(id)) {
				throw new IllegalArgumentException("IdentifiableResourceReloader#wrap method is not intended to rename an identified resource reloader!");
			}

			return identifiable;
		} else {
			return new IdentifiableResourceReloader() {
				@Override
				public @NotNull Identifier getFabricId() {
					return id;
				}

				@Override
				public CompletableFuture<Void> reload(Synchronizer synchronizer, ResourceManager manager, Executor prepareExecutor, Executor applyExecutor) {
					return reloader.reload(synchronizer, manager, prepareExecutor, applyExecutor);
				}
			};
		}
	}
}
