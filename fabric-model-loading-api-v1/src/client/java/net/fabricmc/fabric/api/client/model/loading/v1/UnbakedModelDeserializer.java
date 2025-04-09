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

package net.fabricmc.fabric.api.client.model.loading.v1;

import java.io.Reader;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import org.jetbrains.annotations.Nullable;

import net.minecraft.client.render.model.UnbakedModel;
import net.minecraft.client.render.model.json.JsonUnbakedModel;
import net.minecraft.client.render.model.json.ModelElement;
import net.minecraft.client.render.model.json.ModelElementFace;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.client.render.model.json.Transformation;
import net.minecraft.util.Identifier;

import net.fabricmc.fabric.impl.client.model.loading.UnbakedModelDeserializerRegistry;

/**
 * Allows creating custom unbaked models by overriding the parsing of JSON model files. <b>It is not necessary to
 * implement this interface when using a custom subclass of {@link UnbakedModel} at runtime</b>, e.g. for
 * {@link ModelModifier}.
 *
 * <p>The format for custom unbaked models is as follows:
 * <pre>{@code
 * {
 *     "fabric:type": "<identifier of the deserializer>",
 *     // extra model data, dependent on the deserializer
 * }
 * }</pre>
 *
 * <p>Alternatively, {@code "fabric:type"} may be an object with the required string field {@code "id"}, specifying the
 * identifier of the deserializer, and the optional boolean field {@code "optional"} with default {@code false},
 * specifying whether the model should fail loading ({@code false}) or continue loading as a vanilla model
 * ({@code true}) when the specified deserializer has not been registered.
 *
 * <p>All instances must be registered using {@link #register} for deserialization to work.
 */
public interface UnbakedModelDeserializer {
	/**
	 * Registers a custom model deserializer.
	 *
	 * @throws IllegalArgumentException if the deserializer is already registered
	 */
	static void register(Identifier id, UnbakedModelDeserializer deserializer) {
		UnbakedModelDeserializerRegistry.register(id, deserializer);
	}

	/**
	 * {@return the custom model deserializer registered with the given identifier, or {@code null} if there is no such
	 * deserializer}
	 */
	@Nullable
	static UnbakedModelDeserializer get(Identifier id) {
		return UnbakedModelDeserializerRegistry.get(id);
	}

	/**
	 * Deserializes an {@link UnbakedModel} from a {@link Reader}, respecting custom deserializers. Prefer using this
	 * method to {@link JsonUnbakedModel#deserialize(Reader)}.
	 */
	static UnbakedModel deserialize(Reader reader) throws JsonParseException {
		return UnbakedModelDeserializerRegistry.deserialize(reader);
	}

	/**
	 * Deserialize an {@link UnbakedModel} given a {@link JsonObject} representing the entire model file.
	 *
	 * <p>The provided deserialization context is able to deserialize objects of the following types:
	 * <ul>
	 *     <li>{@link UnbakedModel}</li>
	 *     <li>{@link ModelElement}</li>
	 *     <li>{@link ModelElementFace}</li>
	 *     <li>{@link Transformation}</li>
	 *     <li>{@link ModelTransformation}</li>
	 * </ul>
	 *
	 * <p>For example, to deserialize a nested {@link UnbakedModel}, use
	 * {@code context.deserialize(nestedModelJson, UnbakedModel.class)}.
	 *
	 * <p>This method is allowed and encouraged to throw exceptions, as they will be caught and logged by the caller.
	 *
	 * @param jsonObject the JSON object representing the entire model file
	 * @param context the deserialization context
	 * @return the unbaked model
	 */
	UnbakedModel deserialize(JsonObject jsonObject, JsonDeserializationContext context);
}
