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

import java.util.ArrayList;
import java.util.List;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.jspecify.annotations.Nullable;

import net.minecraft.client.renderer.block.model.BlockElement;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.block.model.SimpleUnbakedGeometry;
import net.minecraft.client.renderer.block.model.TextureSlots;
import net.minecraft.client.resources.model.UnbakedGeometry;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.resources.Identifier;
import net.minecraft.util.GsonHelper;

import net.fabricmc.fabric.api.client.model.loading.v1.UnbakedModelDeserializer;

public class SimpleUnbakedModelDeserializer implements UnbakedModelDeserializer {
	@Override
	public UnbakedModel deserialize(JsonObject json, JsonDeserializationContext context) {
		JsonObject object = json.getAsJsonObject();
		UnbakedGeometry elements = getElements(context, object);

		UnbakedModel.GuiLight guiLight = null;

		if (object.has("gui_light")) {
			guiLight = UnbakedModel.GuiLight.getByName(GsonHelper.getAsString(object, "gui_light"));
		}

		Boolean hasAmbientOcclusion = getAmbientOcclusion(object);
		ItemTransforms transforms = null;

		if (object.has("display")) {
			JsonObject display = GsonHelper.getAsJsonObject(object, "display");
			transforms = context.deserialize(display, ItemTransforms.class);
		}

		TextureSlots.Data textureMap = getTextureMap(object);
		Identifier parentLocation = getParentLocation(object);
		return new BlockModel(elements, guiLight, hasAmbientOcclusion, transforms, textureMap, parentLocation);
	}

	protected @Nullable UnbakedGeometry getElements(final JsonDeserializationContext context, final JsonObject object) {
		if (!object.has("elements")) {
			return null;
		} else {
			List<BlockElement> elements = new ArrayList<>();

			for (JsonElement element : GsonHelper.getAsJsonArray(object, "elements")) {
				elements.add(context.deserialize(element, BlockElement.class));
			}

			return new SimpleUnbakedGeometry(elements);
		}
	}

	protected @Nullable Boolean getAmbientOcclusion(final JsonObject object) {
		return object.has("ambientocclusion") ? GsonHelper.getAsBoolean(object, "ambientocclusion") : null;
	}

	protected TextureSlots.Data getTextureMap(final JsonObject object) {
		if (object.has("textures")) {
			JsonObject texturesObject = GsonHelper.getAsJsonObject(object, "textures");
			return TextureSlots.parseTextureMap(texturesObject);
		} else {
			return TextureSlots.Data.EMPTY;
		}
	}

	protected Identifier getParentLocation(final JsonObject object) {
		String parentName = GsonHelper.getAsString(object, "parent", "");
		return parentName.isEmpty() ? null : Identifier.parse(parentName);
	}
}
