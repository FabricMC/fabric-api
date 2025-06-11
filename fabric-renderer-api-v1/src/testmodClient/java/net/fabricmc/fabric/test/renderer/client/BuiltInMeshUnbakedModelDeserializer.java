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

import net.minecraft.client.render.model.Geometry;
import net.minecraft.client.render.model.ModelTextures;
import net.minecraft.client.render.model.UnbakedModel;
import net.minecraft.client.render.model.json.JsonUnbakedModel;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;

import net.fabricmc.fabric.api.client.model.loading.v1.UnbakedModelDeserializer;
import net.fabricmc.fabric.api.renderer.v1.mesh.ShadeMode;

public class BuiltInMeshUnbakedModelDeserializer implements UnbakedModelDeserializer {
	@Override
	public UnbakedModel deserialize(JsonObject jsonObject, JsonDeserializationContext context) {
		Geometry geometry = null;

		if (jsonObject.has("mesh")) {
			String meshId = JsonHelper.getString(jsonObject, "mesh");
			geometry = geometryFromMeshId(meshId);
		}

		UnbakedModel.GuiLight guiLight = null;

		if (jsonObject.has("gui_light")) {
			guiLight = UnbakedModel.GuiLight.byName(JsonHelper.getString(jsonObject, "gui_light"));
		}

		ModelTransformation transformation = null;

		if (jsonObject.has("display")) {
			JsonObject displayObj = JsonHelper.getObject(jsonObject, "display");
			transformation = context.deserialize(displayObj, ModelTransformation.class);
		}

		ModelTextures.Textures textures = texturesFromJson(jsonObject);
		String parentIdStr = parentFromJson(jsonObject);
		Identifier parentId = parentIdStr.isEmpty() ? null : Identifier.of(parentIdStr);
		return new JsonUnbakedModel(geometry, guiLight, true, transformation, textures, parentId);
	}

	private static Geometry geometryFromMeshId(String meshId) {
		return switch (meshId) {
		case "emissive_frame" -> new FrameGeometry(true);
		case "frame" -> new FrameGeometry(false);
		case "pillar" -> new PillarGeometry();
		case "octagonal_column_enhanced" -> new OctagonalColumnGeometry(ShadeMode.ENHANCED);
		case "octagonal_column_vanilla" -> new OctagonalColumnGeometry(ShadeMode.VANILLA);
		default -> throw new IllegalArgumentException("Invalid mesh ID: " + meshId);
		};
	}

	private static ModelTextures.Textures texturesFromJson(JsonObject object) {
		if (object.has("textures")) {
			JsonObject jsonObject = JsonHelper.getObject(object, "textures");
			return ModelTextures.fromJson(jsonObject, SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE);
		} else {
			return ModelTextures.Textures.EMPTY;
		}
	}

	private static String parentFromJson(JsonObject json) {
		return JsonHelper.getString(json, "parent", "");
	}
}
