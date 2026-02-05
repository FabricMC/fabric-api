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

import org.jspecify.annotations.Nullable;

import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.block.model.TextureSlots;
import net.minecraft.client.resources.model.UnbakedGeometry;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.resources.Identifier;

/**
 * A simple implementation of {@link UnbakedModel} that delegates all method calls to the {@link #wrapped} field.
 * Implementations must set the {@link #wrapped} field somehow.
 */
public abstract class WrapperUnbakedModel implements UnbakedModel {
	protected UnbakedModel wrapped;

	protected WrapperUnbakedModel() {
	}

	protected WrapperUnbakedModel(UnbakedModel wrapped) {
		this.wrapped = wrapped;
	}

	@Override
	@Nullable
	public Boolean ambientOcclusion() {
		return wrapped.ambientOcclusion();
	}

	@Override
	@Nullable
	public GuiLight guiLight() {
		return wrapped.guiLight();
	}

	@Override
	@Nullable
	public ItemTransforms transforms() {
		return wrapped.transforms();
	}

	@Override
	public TextureSlots.Data textureSlots() {
		return wrapped.textureSlots();
	}

	@Override
	@Nullable
	public UnbakedGeometry geometry() {
		return wrapped.geometry();
	}

	@Override
	@Nullable
	public Identifier parent() {
		return wrapped.parent();
	}
}
