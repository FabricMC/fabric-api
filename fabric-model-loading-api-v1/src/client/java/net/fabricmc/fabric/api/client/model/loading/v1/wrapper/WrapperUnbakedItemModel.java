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

import com.mojang.serialization.MapCodec;

import net.minecraft.client.render.item.model.ItemModel;

/**
 * A simple implementation of {@link ItemModel.Unbaked} that delegates all method calls to the {@link #wrapped} field.
 * Implementations must set the {@link #wrapped} field somehow.
 */
public abstract class WrapperUnbakedItemModel implements ItemModel.Unbaked {
	protected ItemModel.Unbaked wrapped;

	protected WrapperUnbakedItemModel() {
	}

	protected WrapperUnbakedItemModel(ItemModel.Unbaked wrapped) {
		this.wrapped = wrapped;
	}

	@Override
	public void resolve(Resolver resolver) {
		wrapped.resolve(resolver);
	}

	@Override
	public MapCodec<? extends ItemModel.Unbaked> getCodec() {
		return wrapped.getCodec();
	}

	@Override
	public ItemModel bake(ItemModel.BakeContext context) {
		return wrapped.bake(context);
	}
}
