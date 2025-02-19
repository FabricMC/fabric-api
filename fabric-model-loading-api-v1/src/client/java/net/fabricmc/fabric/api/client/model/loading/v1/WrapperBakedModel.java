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

import org.jetbrains.annotations.Nullable;

import net.minecraft.block.BlockState;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.BakedQuad;
import net.minecraft.client.texture.Sprite;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;

// TODO: FRAPI overrides
/**
 * A simple implementation of {@link BakedModel} that delegates all method calls to the {@link #wrapped} field.
 * Implementations must set the {@link #wrapped} field somehow.
 */
public abstract class WrapperBakedModel implements BakedModel, UnwrappableBakedModel {
	protected BakedModel wrapped;

	protected WrapperBakedModel() {
	}

	protected WrapperBakedModel(BakedModel wrapped) {
		this.wrapped = wrapped;
	}

	@Override
	public List<BakedQuad> getQuads(BlockState state, @Nullable Direction face, Random random) {
		return wrapped.getQuads(state, face, random);
	}

	@Override
	public boolean useAmbientOcclusion() {
		return wrapped.useAmbientOcclusion();
	}

	@Override
	public Sprite particleSprite() {
		return wrapped.particleSprite();
	}

	@Override
	@Nullable
	public BakedModel getWrappedModel() {
		return wrapped;
	}
}
