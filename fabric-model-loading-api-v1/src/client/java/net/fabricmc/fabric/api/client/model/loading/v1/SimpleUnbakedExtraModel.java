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

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.mojang.logging.LogUtils;
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.apache.commons.lang3.mutable.MutableObject;
import org.slf4j.Logger;

import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.BlockModelPart;
import net.minecraft.client.renderer.block.model.BlockStateModel;
import net.minecraft.client.renderer.block.model.Material;
import net.minecraft.client.renderer.block.model.SimpleModelWrapper;
import net.minecraft.client.renderer.block.model.SingleVariant;
import net.minecraft.client.renderer.block.model.TextureSlots;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BlockModelRotation;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.client.resources.model.QuadCollection;
import net.minecraft.client.resources.model.ResolvedModel;
import net.minecraft.resources.Identifier;

import net.fabricmc.fabric.api.client.renderer.v1.mesh.QuadAtlas;
import net.fabricmc.fabric.api.client.renderer.v1.model.MeshQuadCollection;

/**
 * A {@link UnbakedExtraModel} that loads a single model.
 *
 * @param <T> The type of the baked model, for instance {@link BlockStateModel}.
 */
public final class SimpleUnbakedExtraModel<T> implements UnbakedExtraModel<T> {
	private static final Logger LOGGER = LogUtils.getLogger();

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
	 * <h4>Example</h4>
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
	 * @param model The location of the model to load.
	 * @param state The state to bake the geometry with.
	 * @return The unbaked extra model.
	 */
	public static SimpleUnbakedExtraModel<BlockStateModel> blockStateModel(Identifier model, ModelState state) {
		return new SimpleUnbakedExtraModel<>(model, (baked, baker) -> new SingleVariant(bakeResolved(baker, baked, state)));
	}

	// TODO: expose this as a public utility
	// Mirror of SimpleModelWrapper#bake (with FRAPI's mixin) that accepts a ResolvedModel instead of an Identifier
	private static BlockModelPart bakeResolved(final ModelBaker modelBakery, final ResolvedModel model, final ModelState state) {
		TextureSlots textureSlots = model.getTopTextureSlots();
		boolean hasAmbientOcclusion = model.getTopAmbientOcclusion();
		Material.Baked particleMaterial = model.resolveParticleMaterial(textureSlots, modelBakery);
		QuadCollection geometry = model.bakeTopGeometry(textureSlots, modelBakery, state);
		boolean hasTranslucency = false;
		Multimap<Identifier, Identifier> forbiddenSprites = null;

		if (geometry instanceof MeshQuadCollection meshQuadCollection) {
			MutableBoolean hasTranslucencyRef = new MutableBoolean(hasTranslucency);
			MutableObject<Multimap<Identifier, Identifier>> forbiddenSpritesRef = new MutableObject<>(forbiddenSprites);

			meshQuadCollection.getMesh().forEach(quad -> {
				if (quad.atlas() != QuadAtlas.BLOCK) {
					Multimap<Identifier, Identifier> forbiddenSprites1 = forbiddenSpritesRef.get();

					if (forbiddenSprites1 == null) {
						forbiddenSprites1 = HashMultimap.create();
						forbiddenSpritesRef.setValue(forbiddenSprites1);
					}

					TextureAtlasSprite sprite = modelBakery.materials().spriteFinder(quad.atlas()).find(quad);
					forbiddenSprites1.put(sprite.atlasLocation(), sprite.contents().name());
				}

				hasTranslucencyRef.setValue(hasTranslucencyRef.booleanValue() | quad.chunkLayer().translucent());
			});

			hasTranslucency = hasTranslucencyRef.booleanValue();
			forbiddenSprites = forbiddenSpritesRef.get();
		}

		for (BakedQuad bakedQuad : geometry.getAll()) {
			TextureAtlasSprite sprite = bakedQuad.spriteInfo().sprite();
			if (!sprite.atlasLocation().equals(TextureAtlas.LOCATION_BLOCKS)) {
				if (forbiddenSprites == null) {
					forbiddenSprites = HashMultimap.create();
				}

				forbiddenSprites.put(sprite.atlasLocation(), sprite.contents().name());
			}

			hasTranslucency |= bakedQuad.spriteInfo().layer().translucent();
		}

		if (forbiddenSprites != null) {
			LOGGER.warn("Rejecting block model {}, since it contains sprites from outside of supported atlas: {}", model.debugName(), forbiddenSprites);
			return modelBakery.missingBlockModelPart();
		} else {
			return new SimpleModelWrapper(geometry, hasAmbientOcclusion, particleMaterial, hasTranslucency);
		}
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
