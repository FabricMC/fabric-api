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

import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.block.model.TextureSlots;
import net.minecraft.client.resources.model.UnbakedGeometry;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.resources.Identifier;
import net.minecraft.util.GsonHelper;

import net.fabricmc.fabric.api.client.model.loading.v1.UnbakedModelDeserializer;
import net.fabricmc.fabric.api.renderer.v1.mesh.ShadeMode;

// FIXME 1.21.11: Items that use this model render as empty because the custom geometry has no vanilla quads which
//  causes BlockModelWrapper to select a render type that uses the item atlas, which is then used because all quads in
//  the mesh use the default render type.
public class BuiltInMeshUnbakedModelDeserializer implements UnbakedModelDeserializer {
	@Override
	public UnbakedModel deserialize(JsonObject jsonObject, JsonDeserializationContext context) {
		UnbakedGeometry geometry = null;

		if (jsonObject.has("mesh")) {
			String meshId = GsonHelper.getAsString(jsonObject, "mesh");
			geometry = geometryFromMeshId(meshId);
		}

		UnbakedModel.GuiLight guiLight = null;

		if (jsonObject.has("gui_light")) {
			guiLight = UnbakedModel.GuiLight.getByName(GsonHelper.getAsString(jsonObject, "gui_light"));
		}

		ItemTransforms transformation = null;

		if (jsonObject.has("display")) {
			JsonObject displayObj = GsonHelper.getAsJsonObject(jsonObject, "display");
			transformation = context.deserialize(displayObj, ItemTransforms.class);
		}

		TextureSlots.Data textures = texturesFromJson(jsonObject);
		String parentIdStr = parentFromJson(jsonObject);
		Identifier parentId = parentIdStr.isEmpty() ? null : Identifier.parse(parentIdStr);
		return new BlockModel(geometry, guiLight, true, transformation, textures, parentId);
	}

	private static UnbakedGeometry geometryFromMeshId(String meshId) {
		return switch (meshId) {
		case "emissive_frame" -> new FrameGeometry(true);
		case "frame" -> new FrameGeometry(false);
		case "pillar" -> new PillarGeometry();
		case "octagonal_column_enhanced" -> new OctagonalColumnGeometry(ShadeMode.ENHANCED);
		case "octagonal_column_vanilla" -> new OctagonalColumnGeometry(ShadeMode.VANILLA);
		default -> throw new IllegalArgumentException("Invalid mesh ID: " + meshId);
		};
	}

	private static TextureSlots.Data texturesFromJson(JsonObject object) {
		if (object.has("textures")) {
			JsonObject jsonObject = GsonHelper.getAsJsonObject(object, "textures");
			return TextureSlots.parseTextureMap(jsonObject);
		} else {
			return TextureSlots.Data.EMPTY;
		}
	}

	private static String parentFromJson(JsonObject json) {
		return GsonHelper.getAsString(json, "parent", "");
	}
}
