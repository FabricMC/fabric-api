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

import java.util.List;

import net.minecraft.client.render.model.BlockModelPart;
import net.minecraft.client.render.model.BlockStateModel;
import net.minecraft.client.texture.Sprite;
import net.minecraft.util.math.random.Random;

// TODO: FRAPI overrides
/**
 * A simple implementation of {@link BlockStateModel} that delegates all method calls to the {@link #wrapped} field.
 * Implementations must set the {@link #wrapped} field somehow.
 */
public abstract class WrapperBlockStateModel implements BlockStateModel {
	protected BlockStateModel wrapped;

	protected WrapperBlockStateModel() {
	}

	protected WrapperBlockStateModel(BlockStateModel wrapped) {
		this.wrapped = wrapped;
	}

	@Override
	public void addParts(Random random, List<BlockModelPart> parts) {
		wrapped.addParts(random, parts);
	}

	@Override
	public List<BlockModelPart> getParts(Random random) {
		return wrapped.getParts(random);
	}

	@Override
	public Sprite particleSprite() {
		return wrapped.particleSprite();
	}
}
