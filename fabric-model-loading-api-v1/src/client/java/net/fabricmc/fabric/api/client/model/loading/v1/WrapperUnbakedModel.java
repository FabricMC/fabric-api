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

import org.jetbrains.annotations.Nullable;

import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.Baker;
import net.minecraft.client.render.model.ModelBakeSettings;
import net.minecraft.client.render.model.ModelTextures;
import net.minecraft.client.render.model.UnbakedModel;
import net.minecraft.client.render.model.json.ModelTransformation;

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
	public void resolve(Resolver resolver) {
		wrapped.resolve(resolver);
	}

	@Override
	public BakedModel bake(ModelTextures textures, Baker baker, ModelBakeSettings settings, boolean ambientOcclusion, boolean isSideLit, ModelTransformation transformation) {
		return wrapped.bake(textures, baker, settings, ambientOcclusion, isSideLit, transformation);
	}

	@Override
	@Nullable
	public Boolean getAmbientOcclusion() {
		return wrapped.getAmbientOcclusion();
	}

	@Override
	@Nullable
	public GuiLight getGuiLight() {
		return wrapped.getGuiLight();
	}

	@Override
	@Nullable
	public ModelTransformation getTransformation() {
		return wrapped.getTransformation();
	}

	@Override
	public ModelTextures.Textures getTextures() {
		return wrapped.getTextures();
	}

	@Override
	@Nullable
	public UnbakedModel getParent() {
		return wrapped.getParent();
	}
}
