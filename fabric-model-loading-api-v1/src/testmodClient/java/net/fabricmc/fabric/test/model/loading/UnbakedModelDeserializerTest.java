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

package net.fabricmc.fabric.test.model.loading;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import com.google.gson.JsonParseException;

import com.google.gson.JsonSyntaxException;
import com.mojang.math.Transformation;
import com.mojang.serialization.JsonOps;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.model.loading.v1.UnbakedModelDeserializer;

import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.resources.Identifier;

import net.minecraft.util.GsonHelper;

import org.jspecify.annotations.Nullable;

import java.util.Objects;

// FIXME: idk what this was for but it seems to have broken before the mojmap boundary
public class UnbakedModelDeserializerTest implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		UnbakedModelDeserializer.register(ModelTestModClient.id("transformed"), TransformedModelDeserializer.INSTANCE);
	}

	private static class TransformedModelDeserializer implements UnbakedModelDeserializer {
		public static final TransformedModelDeserializer INSTANCE = new TransformedModelDeserializer();

		@Override
		public UnbakedModel deserialize(JsonObject jsonObject, JsonDeserializationContext context) throws JsonParseException {
			JsonElement transformationElement = GsonHelper.getNonNull(jsonObject, "transformation");
			Transformation transformation = Transformation.EXTENDED_CODEC.parse(
					JsonOps.INSTANCE, transformationElement).getOrThrow();

			JsonElement parentElement = GsonHelper.getNonNull(jsonObject, "parent");

			if (GsonHelper.isStringValue(parentElement)) {
				Identifier parentId = Identifier.tryParse(parentElement.getAsString());
				Objects.requireNonNull(parentId, "invalid identifier " + parentElement.getAsString());
				return new TransformedUnbakedModel(transformation, parentId);
			} else {
				throw new JsonSyntaxException("parent must be string or object");
			}
		}
	}

	private static class TransformedUnbakedModel implements UnbakedModel {
		private final Transformation transformation;
		@Nullable
		private final Identifier parentId;

		private TransformedUnbakedModel(Transformation transformation, @Nullable Identifier parentId) {
			this.transformation = transformation;
			this.parentId = parentId;
		}

		@Override
		public @Nullable Identifier parent() {
			return this.parentId;
		}
	}
}
