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

import java.util.function.BiFunction;

import net.minecraft.client.renderer.block.model.BlockStateModel;
import net.minecraft.client.renderer.block.model.SimpleModelWrapper;
import net.minecraft.client.renderer.block.model.SingleVariant;
import net.minecraft.client.renderer.block.model.TextureSlots;
import net.minecraft.client.resources.model.BlockModelRotation;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.client.resources.model.ResolvedModel;
import net.minecraft.resources.Identifier;

/**
 * A {@link UnbakedExtraModel} that loads a single model.
 *
 * @param <T> The type of the baked model, for instance {@link BlockStateModel}.
 */
public final class SimpleUnbakedExtraModel<T> implements UnbakedExtraModel<T> {
	private final Identifier model;
	private final BiFunction<ResolvedModel, ModelBaker, T> bake;

	/**
	 * @param model The location of the model to load.
	 * @param bake  A function to bake the model.
	 */
	public SimpleUnbakedExtraModel(Identifier model, BiFunction<ResolvedModel, ModelBaker, T> bake) {
		this.model = model;
		this.bake = bake;
	}

	/**
	 * Create a {@link SimpleUnbakedExtraModel} for a {@link BlockStateModel}.
	 *
	 * <h2>Example</h2>
	 * {@snippet :
	 * public static final Identifier MODEL_ID = Identifier.fromNamespaceAndPath("modid", "model_path");
	 * public static final ExtraModelKey<BlockStateModel> MODEL_KEY = ExtraModelKey.create(MODEL_ID::toString);
	 *
	 * public static void register() {
	 * 		ModelLoadingPlugin.register(pluginContext -> pluginContext.addModel(MODEL_KEY, SimpleUnbakedExtraModel.blockStateModel(MODEL_ID)));
	 * }
	 * }
	 *
	 * @param model The location of the model to load.
	 * @return The unbaked extra model.
	 */
	public static SimpleUnbakedExtraModel<BlockStateModel> blockStateModel(Identifier model) {
		return blockStateModel(model, BlockModelRotation.IDENTITY);
	}

	/**
	 * Create a {@link SimpleUnbakedExtraModel} for a {@link BlockStateModel}.
	 *
	 * @param model    The location of the model to load.
	 * @param settings The settings to bake the geometry with.
	 * @return The unbaked extra model.
	 */
	public static SimpleUnbakedExtraModel<BlockStateModel> blockStateModel(Identifier model, ModelState settings) {
		return new SimpleUnbakedExtraModel<>(model, (baked, baker) -> {
			TextureSlots textures = baked.getTopTextureSlots();
			return new SingleVariant(new SimpleModelWrapper(
					baked.bakeTopGeometry(textures, baker, settings),
					baked.getTopAmbientOcclusion(),
					baked.resolveParticleSprite(textures, baker)
			));
		});
	}

	@Override
	public void resolveDependencies(Resolver resolver) {
		resolver.markDependency(model);
	}

	@Override
	public T bake(ModelBaker baker) {
		return bake.apply(baker.getModel(model), baker);
	}
}
