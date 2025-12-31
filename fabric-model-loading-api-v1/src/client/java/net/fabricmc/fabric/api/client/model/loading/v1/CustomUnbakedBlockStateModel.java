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

import com.mojang.serialization.MapCodec;

import net.minecraft.client.renderer.block.model.BlockStateModel;
import net.minecraft.resources.Identifier;

import net.fabricmc.fabric.impl.client.model.loading.CustomUnbakedBlockStateModelRegistry;

/// Allows defining custom unbaked block state model types which can be used within `blockstates/` files. **It is
/// not necessary to implement this interface when using a custom subclass of [BlockStateModel.Unbaked] at runtime
/// **, e.g. for [ModelModifier].
///
/// The format for custom unbaked block state models is as follows:
/// <pre>
/// `{"fabric:type": "<identifier of the type>",// extra model data, dependent on the type}`</pre>
///
/// The above JSON object may be used in a `blockstates/` file wherever a [net.minecraft.client.renderer.block.model.Variant] or
/// [net.minecraft.client.renderer.block.model.SingleVariant.Unbaked] is normally valid. Note that if the `"fabric:type"` key is present,
/// the object will never be parsed as a [net.minecraft.client.renderer.block.model.Variant], even if the custom type does not exist or is not able to
/// parse the object.
///
/// [BlockStateModel.Unbaked#CODEC] and [BlockStateModel.Unbaked#HARDCODED_WEIGHTED_CODEC] are automatically patched
/// to support custom models. Custom types are encouraged to use [BlockStateModel.Unbaked#CODEC] to
/// deserialize/serialize submodels.
///
/// All types must be registered using [#register] for deserialization/serialization to work.
public interface CustomUnbakedBlockStateModel extends BlockStateModel.Unbaked {
	/// Registers a custom block state model type.
	static void register(Identifier id, MapCodec<? extends CustomUnbakedBlockStateModel> codec) {
		CustomUnbakedBlockStateModelRegistry.register(id, codec);
	}

	/// Returns the codec which can be used to serialize this model. Must match the codec passed to [#register]
	/// which deserializes objects of this type.
	MapCodec<? extends CustomUnbakedBlockStateModel> codec();
}
