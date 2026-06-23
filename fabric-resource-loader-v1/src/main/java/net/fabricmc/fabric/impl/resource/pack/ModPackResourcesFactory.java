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

package net.fabricmc.fabric.impl.resource.pack;

import java.util.ArrayList;
import java.util.stream.Stream;

import net.minecraft.server.packs.OverlayedPackResources;
import net.minecraft.server.packs.PackLocationInfo;
import net.minecraft.server.packs.PackMetadataResources;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.repository.Pack;

import net.fabricmc.fabric.api.resource.v1.pack.ModPackResources;

public record ModPackResourcesFactory(ModPackResources pack) implements Pack.ResourcesSupplier {
	@Override
	public PackMetadataResources openMetadata(PackLocationInfo location) {
		return this.pack;
	}

	@Override
	public Stream<PackResources> openResources(PackLocationInfo location, Pack.Metadata metadata) {
		if (metadata.overlays().isEmpty()) {
			return Stream.of(this.pack);
		} else {
			var overlays = new ArrayList<PackResources>(metadata.overlays().size());

			for (String overlay : metadata.overlays()) {
				overlays.add(this.pack.createOverlay(overlay));
			}

			return Stream.of(new OverlayedPackResources(this.pack, overlays));
		}
	}
}
