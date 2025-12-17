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

package net.fabricmc.fabric.mixin.renderer.client.block.model;

import java.util.function.Predicate;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.client.renderer.block.model.BlockModelPart;
import net.minecraft.client.renderer.block.model.SimpleModelWrapper;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.client.resources.model.QuadCollection;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;

import net.fabricmc.fabric.api.renderer.v1.mesh.QuadAtlas;
import net.fabricmc.fabric.api.renderer.v1.mesh.QuadEmitter;
import net.fabricmc.fabric.api.renderer.v1.model.MeshBakedGeometry;
import net.fabricmc.fabric.api.util.TriState;

@Mixin(SimpleModelWrapper.class)
abstract class SimpleModelWrapperMixin implements BlockModelPart {
	@Shadow
	@Final
	private QuadCollection quads;
	@Shadow
	@Final
	private boolean useAmbientOcclusion;

	@Inject(method = "bake(Lnet/minecraft/client/resources/model/ModelBaker;Lnet/minecraft/resources/Identifier;Lnet/minecraft/client/resources/model/ModelState;)Lnet/minecraft/client/renderer/block/model/BlockModelPart;", at = @At(value = "INVOKE", target = "net/minecraft/client/resources/model/QuadCollection.getAll()Ljava/util/List;"))
	private static void validateMeshAtlas(final ModelBaker modelBakery, final Identifier location, final ModelState state, CallbackInfoReturnable<BlockModelPart> cir, @Local QuadCollection geometry, @Local LocalRef<Multimap<Identifier, Identifier>> forbiddenSpritesRef) {
		if (geometry instanceof MeshBakedGeometry meshGeometry) {
			meshGeometry.getMesh().forEach(quad -> {
				if (quad.atlas() != QuadAtlas.BLOCK) {
					Multimap<Identifier, Identifier> forbiddenSprites = forbiddenSpritesRef.get();

					if (forbiddenSprites == null) {
						forbiddenSprites = HashMultimap.create();
						forbiddenSpritesRef.set(forbiddenSprites);
					}

					TextureAtlasSprite sprite = modelBakery.sprites().spriteFinder(quad.atlas()).find(quad);
					forbiddenSprites.put(sprite.atlasLocation(), sprite.contents().name());
				}
			});
		}
	}

	@Override
	public void emitQuads(QuadEmitter emitter, Predicate<@Nullable Direction> cullTest) {
		if (quads instanceof MeshBakedGeometry meshBakedGeometry) {
			if (useAmbientOcclusion) {
				meshBakedGeometry.getMesh().outputTo(emitter);
			} else {
				emitter.pushTransform(quad -> {
					if (quad.ambientOcclusion() == TriState.DEFAULT) {
						quad.ambientOcclusion(TriState.FALSE);
					}

					return true;
				});
				meshBakedGeometry.getMesh().outputTo(emitter);
				emitter.popTransform();
			}
		} else {
			BlockModelPart.super.emitQuads(emitter, cullTest);
		}
	}
}
