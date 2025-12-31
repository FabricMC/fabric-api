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
import org.jspecify.annotations.Nullable;

import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.resources.Identifier;

import net.fabricmc.fabric.impl.client.model.loading.UnbakedModelDeserializerRegistry;

/// Allows creating custom unbaked models by overriding the parsing of JSON model files. **It is not necessary to
/// implement this interface when using a custom subclass of [UnbakedModel] at runtime**, e.g. for
/// [ModelModifier].
///
/// The format for custom unbaked models is as follows:
/// <pre>
/// `{"fabric:type": "<identifier of the deserializer>",// extra model data, dependent on the deserializer}`</pre>
///
/// Alternatively, `"fabric:type"` may be an object with the required string field `"id"`, specifying the
/// identifier of the deserializer, and the optional boolean field `"optional"` with default `false`,
/// specifying whether the model should fail loading (`false`) or continue loading as a vanilla model
/// (`true`) when the specified deserializer has not been registered.
///
/// All instances must be registered using [#register] for deserialization to work.
public interface UnbakedModelDeserializer {
	/// Registers a custom model deserializer.
	///
	/// @throws IllegalArgumentException if the deserializer is already registered
	static void register(Identifier id, UnbakedModelDeserializer deserializer) {
		UnbakedModelDeserializerRegistry.register(id, deserializer);
	}

	/// {@return the custom model deserializer registered with the given identifier, or {@code null} if there is no such
	///  deserializer}
	@Nullable
	static UnbakedModelDeserializer get(Identifier id) {
		return UnbakedModelDeserializerRegistry.get(id);
	}

	/// Deserializes an [UnbakedModel] from a [Reader], respecting custom deserializers. Prefer using this
	/// method to [net.minecraft.client.renderer.block.model.BlockModel#fromStream(Reader)].
	static UnbakedModel deserialize(Reader reader) throws JsonParseException {
		return UnbakedModelDeserializerRegistry.deserialize(reader);
	}

	/// Deserialize an [UnbakedModel] given a [JsonObject] representing the entire model file.
	///
	/// The provided deserialization context is able to deserialize objects of the following types:
	///
	///   - [UnbakedModel]
	///   - [net.minecraft.client.renderer.block.model.BlockElement]
	///   - [net.minecraft.client.renderer.block.model.BlockElementFace]
	///   - [net.minecraft.client.renderer.block.model.ItemTransform]
	///   - [net.minecraft.client.renderer.block.model.ItemTransforms]
	///
	///
	/// For example, to deserialize a nested [UnbakedModel], use
	/// `context.deserialize(nestedModelJson, UnbakedModel.class)`.
	///
	/// This method is allowed and encouraged to throw exceptions, as they will be caught and logged by the caller.
	///
	/// @param jsonObject the JSON object representing the entire model file
	/// @param context the deserialization context
	/// @return the unbaked model
	UnbakedModel deserialize(JsonObject jsonObject, JsonDeserializationContext context);
}
