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

package net.fabricmc.fabric.api.resource.v1.pack;

import java.util.function.Function;

import net.minecraft.server.packs.PackLocationInfo;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.repository.Pack;

/// Provides the factory of a very simple implementation of [Pack.ResourcesSupplier] which only opens the given `packFactory`.
public final class SimplePackResourcesSupplier {
	private SimplePackResourcesSupplier() {
		throw new UnsupportedOperationException("SimplePackResourcesSupplier only contains static definitions.");
	}

	/// Provides a very simple implementation of [Pack.ResourcesSupplier] which only opens the given `packFactory`.
	///
	/// @param packFactory the factory of the pack resources
	public static Pack.ResourcesSupplier of(Function<PackLocationInfo, PackResources> packFactory) {
		return new Pack.ResourcesSupplier() {
			@Override
			public PackResources openPrimary(PackLocationInfo location) {
				return packFactory.apply(location);
			}

			@Override
			public PackResources openFull(PackLocationInfo location, Pack.Metadata metadata) {
				return packFactory.apply(location);
			}
		};
	}
}
