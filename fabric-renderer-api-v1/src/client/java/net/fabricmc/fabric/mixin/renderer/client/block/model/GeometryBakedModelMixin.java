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

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import net.minecraft.client.render.model.BakedGeometry;
import net.minecraft.client.render.model.BlockModelPart;
import net.minecraft.client.render.model.GeometryBakedModel;
import net.minecraft.util.math.Direction;

import net.fabricmc.fabric.api.renderer.v1.mesh.QuadEmitter;
import net.fabricmc.fabric.api.renderer.v1.model.MeshBakedGeometry;
import net.fabricmc.fabric.api.util.TriState;

@Mixin(GeometryBakedModel.class)
abstract class GeometryBakedModelMixin implements BlockModelPart {
	@Shadow
	@Final
	private BakedGeometry quads;
	@Shadow
	@Final
	private boolean useAmbientOcclusion;

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
