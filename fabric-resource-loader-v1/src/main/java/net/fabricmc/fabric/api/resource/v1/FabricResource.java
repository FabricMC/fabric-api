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

package net.fabricmc.fabric.api.resource.v1;

import org.slf4j.LoggerFactory;

import net.minecraft.server.packs.repository.PackSource;

/**
 * Extensions to {@link net.minecraft.server.packs.resources.Resource}.
 * Automatically implemented there via a mixin.
 */
public interface FabricResource {
	/**
	 * Gets the resource pack source of this resource.
	 * The source is used to separate vanilla/mod resources from user resources in Fabric API.
	 *
	 * <p>Custom {@link net.minecraft.server.packs.resources.Resource} implementations should override this method.
	 *
	 * @return the resource pack source
	 */
	default PackSource getFabricPackSource() {
		LoggerFactory.getLogger(FabricResource.class).error("Unknown Resource implementation {}, returning PACK_SOURCE_NONE as the source", this.getClass().getName());
		return PackSource.DEFAULT;
	}
}
