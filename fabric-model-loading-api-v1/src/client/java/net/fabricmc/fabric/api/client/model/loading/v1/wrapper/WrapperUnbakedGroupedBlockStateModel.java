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

package net.fabricmc.fabric.api.client.model.loading.v1.wrapper;

import net.minecraft.block.BlockState;
import net.minecraft.client.render.model.Baker;
import net.minecraft.client.render.model.BlockStateModel;

/**
 * A simple implementation of {@link BlockStateModel.UnbakedGrouped} that delegates all method calls to the
 * {@link #wrapped} field. Implementations must set the {@link #wrapped} field somehow.
 */
public abstract class WrapperUnbakedGroupedBlockStateModel implements BlockStateModel.UnbakedGrouped {
	protected BlockStateModel.UnbakedGrouped wrapped;

	protected WrapperUnbakedGroupedBlockStateModel() {
	}

	protected WrapperUnbakedGroupedBlockStateModel(BlockStateModel.UnbakedGrouped wrapped) {
		this.wrapped = wrapped;
	}

	@Override
	public BlockStateModel bake(BlockState state, Baker baker) {
		return wrapped.bake(state, baker);
	}

	@Override
	public Object getEqualityGroup(BlockState state) {
		return wrapped.getEqualityGroup(state);
	}

	@Override
	public void resolve(Resolver resolver) {
		wrapped.resolve(resolver);
	}
}
