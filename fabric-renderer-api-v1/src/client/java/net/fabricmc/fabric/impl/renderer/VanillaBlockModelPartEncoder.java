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

package net.fabricmc.fabric.impl.renderer;

import java.util.List;
import java.util.function.Predicate;

import org.jetbrains.annotations.Nullable;

import net.minecraft.client.render.model.BakedQuad;
import net.minecraft.client.render.model.BlockModelPart;
import net.minecraft.util.math.Direction;

import net.fabricmc.fabric.api.renderer.v1.mesh.QuadEmitter;
import net.fabricmc.fabric.api.renderer.v1.mesh.ShadeMode;
import net.fabricmc.fabric.api.renderer.v1.model.ModelHelper;
import net.fabricmc.fabric.api.util.TriState;

/**
 * Routines for adaptation of vanilla {@link BlockModelPart}s to FRAPI pipelines.
 */
public class VanillaBlockModelPartEncoder {
	public static void emitQuads(BlockModelPart part, QuadEmitter emitter, Predicate<@Nullable Direction> cullTest) {
		// This does not exactly match vanilla, but doing so requires hiding state all over the FRAPI impl.
		final TriState ao = part.useAmbientOcclusion() ? TriState.DEFAULT : TriState.FALSE;

		for (int i = 0; i <= ModelHelper.NULL_FACE_ID; i++) {
			final Direction cullFace = ModelHelper.faceFromIndex(i);

			if (cullTest.test(cullFace)) {
				// Skip entire quad list if possible.
				continue;
			}

			final List<BakedQuad> quads = part.getQuads(cullFace);
			final int quadCount = quads.size();

			for (int j = 0; j < quadCount; j++) {
				final BakedQuad q = quads.get(j);
				emitter.cullFace(cullFace);
				emitter.fromBakedQuad(q);
				emitter.ambientOcclusion(ao);
				emitter.shadeMode(ShadeMode.VANILLA);
				emitter.emit();
			}
		}
	}
}
