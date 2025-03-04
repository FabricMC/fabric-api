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

import org.jetbrains.annotations.Nullable;

import net.minecraft.client.render.model.Geometry;
import net.minecraft.client.render.model.ModelTextures;
import net.minecraft.client.render.model.UnbakedModel;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.util.Identifier;

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
	public ModelTransformation transformations() {
		return wrapped.transformations();
	}

	@Override
	public ModelTextures.Textures textures() {
		return wrapped.textures();
	}

	@Override
	@Nullable
	public Geometry geometry() {
		return wrapped.geometry();
	}

	@Override
	@Nullable
	public Identifier parent() {
		return wrapped.parent();
	}
}
