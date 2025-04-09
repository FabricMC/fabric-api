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

import java.util.List;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Unmodifiable;

import net.minecraft.client.render.item.model.CompositeItemModel;
import net.minecraft.client.render.model.BlockStateModel;

import net.fabricmc.fabric.impl.client.model.loading.CompositeBlockStateModelImpl;

/**
 * A custom block state model that is made of one or more other block state models. Analogous to
 * {@link CompositeItemModel}. Uses the first submodel to determine the particle sprite.
 */
@ApiStatus.NonExtendable
public interface CompositeBlockStateModel extends BlockStateModel {
	/**
	 * Creates a new composite model from the given non-empty list of submodels.
	 */
	static CompositeBlockStateModel of(List<BlockStateModel> models) {
		return CompositeBlockStateModelImpl.of(models);
	}

	/**
	 * Gets the models that make up this composite model. The returned list will contain at least one model.
	 */
	@Unmodifiable
	List<BlockStateModel> models();

	/**
	 * An unbaked composite model made of one or more other unbaked models.
	 *
	 * <p>The JSON format is as follows:
	 * <pre>{@code
	 * {
	 *     "fabric:type": "fabric:composite",
	 *     "models": [
	 *         // sub-model 1,
	 *         // sub-model 2,
	 *         // etc...
	 *     ]
	 * }
	 * }</pre>
	 */
	@ApiStatus.NonExtendable
	interface Unbaked extends CustomUnbakedBlockStateModel {
		/**
		 * Creates a new unbaked composite model from the given non-empty list of submodels.
		 */
		static Unbaked of(List<BlockStateModel.Unbaked> models) {
			return CompositeBlockStateModelImpl.Unbaked.of(models);
		}

		/**
		 * Gets the models that make up this unbaked composite model. The returned list will contain at least one model.
		 */
		@Unmodifiable
		List<BlockStateModel.Unbaked> models();
	}
}
