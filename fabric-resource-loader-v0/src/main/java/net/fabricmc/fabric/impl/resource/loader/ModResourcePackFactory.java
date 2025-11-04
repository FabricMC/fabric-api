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

package net.fabricmc.fabric.impl.resource.loader;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.server.packs.CompositePackResources;
import net.minecraft.server.packs.PackLocationInfo;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.repository.Pack;

import net.fabricmc.fabric.api.resource.ModResourcePack;

public record ModResourcePackFactory(ModResourcePack pack) implements Pack.ResourcesSupplier {
	@Override
	public PackResources openPrimary(PackLocationInfo var1) {
		return pack;
	}

	@Override
	public PackResources openFull(PackLocationInfo var1, Pack.Metadata metadata) {
		if (metadata.overlays().isEmpty()) {
			return pack;
		} else {
			List<PackResources> overlays = new ArrayList<>(metadata.overlays().size());

			for (String overlay : metadata.overlays()) {
				overlays.add(pack.createOverlay(overlay));
			}

			return new CompositePackResources(pack, overlays);
		}
	}
}
