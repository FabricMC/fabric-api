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

package net.fabricmc.fabric.impl.client.model.loading;

import java.lang.reflect.Type;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSyntaxException;

import net.minecraft.client.render.model.UnbakedModel;
import net.minecraft.client.render.model.json.JsonUnbakedModel;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;

import net.fabricmc.fabric.api.client.model.loading.v1.UnbakedModelDeserializer;

public class UnbakedModelJsonDeserializer implements JsonDeserializer<UnbakedModel> {
	private static final String TYPE_KEY = "fabric:type";
	private static final String TYPE_ID_KEY = "id";
	private static final String TYPE_OPTIONAL_KEY = "optional";

	@Override
	public UnbakedModel deserialize(JsonElement jsonElement, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
		JsonObject jsonObject = jsonElement.getAsJsonObject();

		if (jsonObject.has(TYPE_KEY)) {
			JsonElement typeElement = jsonObject.get(TYPE_KEY);
			String idStr;
			boolean optional;

			if (typeElement.isJsonPrimitive()) {
				idStr = typeElement.getAsString();
				optional = false;
			} else if (typeElement.isJsonObject()) {
				JsonObject typeObject = typeElement.getAsJsonObject();
				idStr = JsonHelper.getString(typeObject, TYPE_ID_KEY);
				optional = JsonHelper.getBoolean(typeObject, TYPE_OPTIONAL_KEY, false);
			} else {
				throw new JsonSyntaxException("Expected " + TYPE_KEY + " to be a string or object, was " + JsonHelper.getType(typeElement));
			}

			Identifier id = Identifier.of(idStr);
			UnbakedModelDeserializer deserializer = UnbakedModelDeserializer.get(id);

			if (deserializer != null) {
				return deserializer.deserialize(jsonObject, context);
			} else if (!optional) {
				throw new JsonParseException("Cannot deserialize custom unbaked model of unknown type '" + id + "'");
			}
		}

		return context.deserialize(jsonElement, JsonUnbakedModel.class);
	}
}
