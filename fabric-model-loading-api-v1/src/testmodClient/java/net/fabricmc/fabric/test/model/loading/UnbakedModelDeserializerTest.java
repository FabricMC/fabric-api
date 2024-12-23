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
import com.mojang.serialization.JsonOps;
import org.jetbrains.annotations.Nullable;

import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.Baker;
import net.minecraft.client.render.model.ModelBakeSettings;
import net.minecraft.client.render.model.ModelTextures;
import net.minecraft.client.render.model.UnbakedModel;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.math.AffineTransformation;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.model.loading.v1.UnbakedModelDeserializer;

public class UnbakedModelDeserializerTest implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		UnbakedModelDeserializer.register(ModelTestModClient.id("transformed"), TransformedModelDeserializer.INSTANCE);
	}

	private static class TransformedModelDeserializer implements UnbakedModelDeserializer {
		public static final TransformedModelDeserializer INSTANCE = new TransformedModelDeserializer();

		@Override
		public UnbakedModel deserialize(JsonObject jsonObject, JsonDeserializationContext context) throws JsonParseException {
			JsonElement transformationElement = JsonHelper.getElement(jsonObject, "transformation");
			AffineTransformation transformation = AffineTransformation.ANY_CODEC.parse(JsonOps.INSTANCE, transformationElement).getOrThrow();

			JsonElement parentElement = JsonHelper.getElement(jsonObject, "parent");

			if (JsonHelper.isString(parentElement)) {
				Identifier parentId = Identifier.of(parentElement.getAsString());
				return new TransformedUnbakedModel(transformation, parentId);
			} else if (parentElement.isJsonObject()) {
				UnbakedModel parent = context.deserialize(parentElement, UnbakedModel.class);
				return new TransformedUnbakedModel(transformation, parent);
			} else {
				throw new JsonSyntaxException("parent must be string or object");
			}
		}
	}

	private static class TransformedUnbakedModel implements UnbakedModel {
		private final AffineTransformation transformation;
		@Nullable
		private final Identifier parentId;
		private UnbakedModel parent;

		private TransformedUnbakedModel(AffineTransformation transformation, Identifier parentId) {
			this.transformation = transformation;
			this.parentId = parentId;
		}

		private TransformedUnbakedModel(AffineTransformation transformation, UnbakedModel parent) {
			this.transformation = transformation;
			parentId = null;
			this.parent = parent;
		}

		@Override
		public void resolve(Resolver resolver) {
			if (parentId != null) {
				parent = resolver.resolve(parentId);
			}
		}

		@Override
		public UnbakedModel getParent() {
			return parent;
		}

		@Override
		public BakedModel bake(ModelTextures textures, Baker baker, ModelBakeSettings settings, boolean ambientOcclusion, boolean isSideLit, ModelTransformation transformation) {
			settings = new SimpleModelBakeSettings(settings.getRotation().multiply(this.transformation), settings.isUvLocked());
			return parent.bake(textures, baker, settings, ambientOcclusion, isSideLit, transformation);
		}
	}

	private record SimpleModelBakeSettings(AffineTransformation transformation, boolean uvLocked) implements ModelBakeSettings {
		@Override
		public AffineTransformation getRotation() {
			return transformation;
		}

		@Override
		public boolean isUvLocked() {
			return uvLocked;
		}
	}
}
