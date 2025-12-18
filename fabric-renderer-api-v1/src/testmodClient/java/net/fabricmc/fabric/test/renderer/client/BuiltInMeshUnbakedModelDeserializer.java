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

package net.fabricmc.fabric.test.renderer.client;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import org.jspecify.annotations.Nullable;

import net.minecraft.client.resources.model.UnbakedGeometry;
import net.minecraft.util.GsonHelper;

import net.fabricmc.fabric.api.renderer.v1.mesh.ShadeMode;

public class BuiltInMeshUnbakedModelDeserializer extends SimpleUnbakedModelDeserializer {
	@Override
	protected @Nullable UnbakedGeometry getElements(final JsonDeserializationContext context, final JsonObject object) {
		if (!object.has("mesh")) {
			return null;
		}

		String meshId = GsonHelper.getAsString(object, "mesh");

		return switch (meshId) {
		case "emissive_frame" -> new FrameGeometry(true);
		case "frame" -> new FrameGeometry(false);
		case "pillar" -> new PillarGeometry();
		case "octagonal_column_enhanced" -> new OctagonalColumnGeometry(ShadeMode.ENHANCED);
		case "octagonal_column_vanilla" -> new OctagonalColumnGeometry(ShadeMode.VANILLA);
		default -> throw new IllegalArgumentException("Invalid mesh ID: " + meshId);
		};
	}

	@Override
	protected @Nullable Boolean getAmbientOcclusion(final JsonObject object) {
		return true;
	}
}
